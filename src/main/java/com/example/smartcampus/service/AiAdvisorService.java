package com.example.smartcampus.service;

import com.example.smartcampus.config.AiProperties;
import com.example.smartcampus.domain.Grade;
import com.example.smartcampus.domain.TeachingClass;
import com.example.smartcampus.dto.ai.AiCardItem;
import com.example.smartcampus.dto.ai.AiChatResponse;
import com.example.smartcampus.exception.BusinessException;
import com.example.smartcampus.repository.EnrollmentRepository;
import com.example.smartcampus.repository.GradeRepository;
import com.example.smartcampus.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AiAdvisorService {

    private final UserRepository userRepository;
    private final GradeRepository gradeRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final AiProperties aiProperties;
    private final LlmClient llmClient;

    private static final Random RANDOM = new Random();
    private static final ObjectMapper MAPPER = new ObjectMapper();

    // 用于解析制度模型输出的 JSON
    private static class PolicyJson {
        public String answer;
        public List<String> evidence;
        public List<String> actionPlan;
    }

    public List<AiCardItem> buildCards(Long studentId) {
        userRepository.findById(studentId).orElseThrow(() -> new BusinessException("用户不存在"));
        var grades = gradeRepository.findByStudent_Id(studentId);
        var enrollments = enrollmentRepository.findByStudent_Id(studentId);

        List<AiCardItem> cards = new ArrayList<>();
        if (grades.isEmpty()) {
            cards.add(new AiCardItem(
                    "advice",
                    "暂无成绩数据",
                    "你还没有可分析的成绩，建议先完成一次测验/作业后再查看学业建议。",
                    "low"
            ));
            return cards;
        }

        double avg = grades.stream().mapToDouble(this::score).average().orElse(0);
        Grade min = grades.stream().min(Comparator.comparingDouble(this::score)).orElse(null);

        if (min != null) {
            String cls = className(min.getTeachingClass());
            double minScore = score(min);
            cards.add(new AiCardItem(
                    "risk",
                    "风险课程预警",
                    String.format("当前最低分课程：%s（%.1f分），建议优先复习。", cls, minScore),
                    minScore < 60 ? "high" : (minScore < 75 ? "medium" : "low")
            ));
        }

        cards.add(new AiCardItem(
                "advice",
                "成绩总览",
                String.format("当前平均分 %.1f，建议学习时间按60%%薄弱课程+40%%巩固课程分配。", avg),
                avg < 70 ? "high" : (avg < 80 ? "medium" : "low")
        ));

        cards.add(new AiCardItem(
                "advice",
                "学习负载提醒",
                "你当前在读 " + enrollments.size() + " 门课程，建议固定每周复盘时间。",
                enrollments.size() >= 6 ? "high" : "low"
        ));

        return cards;
    }

    public AiChatResponse chat(Long studentId, String question) {
        userRepository.findById(studentId).orElseThrow(() -> new BusinessException("用户不存在"));

        var grades = gradeRepository.findByStudent_Id(studentId);
        var enrollments = enrollmentRepository.findByStudent_Id(studentId);

        if (grades.isEmpty()) {
            return new AiChatResponse(
                    "你当前暂无成绩数据，建议先完成课程考核后再进行学业分析。",
                    List.of("成绩数据为空"),
                    List.of("先完成一次作业或测验", "完成后返回AI学业助手查看建议")
            );
        }

        double avg = grades.stream().mapToDouble(this::score).average().orElse(0);
        Grade min = grades.stream().min(Comparator.comparingDouble(this::score)).orElseThrow();
        Grade max = grades.stream().max(Comparator.comparingDouble(this::score)).orElseThrow();

        String weakClass = className(min.getTeachingClass());
        String strongClass = className(max.getTeachingClass());

        List<String> evidence = grades.stream()
                .sorted(Comparator.comparingDouble(this::score))
                .limit(6)
                .map(g -> className(g.getTeachingClass()) + "：" + score(g))
                .collect(Collectors.toList());

        List<String> actionPlan = defaultActionPlan(weakClass);

        String q = question == null ? "" : question.trim();
        if (q.isBlank()) {
            return new AiChatResponse("请输入你的问题（例如：本周我该如何安排复习？）。", evidence, actionPlan);
        }

        // 0) 低相关问题：委婉拒绝 + 引导学习（随机文案）
        if (isLowRelevanceQuestion(q)) {
            return new AiChatResponse(buildGuidedRefusal(), evidence, actionPlan);
        }

        // 1) 明确查询类：规则优先（保证准确）
        if (isWeakCourseQuestion(q)) {
            return new AiChatResponse(
                    "你的弱势科目是：" + weakClass + "（" + score(min) + "分）。建议本周优先投入复习时间。",
                    evidence,
                    actionPlan
            );
        }

        if (isAverageQuestion(q)) {
            return new AiChatResponse(
                    "你当前平均分是：" + String.format("%.1f", avg) + "分。建议围绕薄弱科目拉升整体均分。",
                    evidence,
                    actionPlan
            );
        }

        if (isStrongCourseQuestion(q)) {
            return new AiChatResponse(
                    "你的优势科目是：" + strongClass + "（" + score(max) + "分）。建议保持低频巩固即可。",
                    evidence,
                    actionPlan
            );
        }

        if (isCourseLoadQuestion(q)) {
            return new AiChatResponse(
                    "你当前在读课程数为：" + enrollments.size() + "门。建议按“先弱后强”安排时间。",
                    evidence,
                    actionPlan
            );
        }

        // 2) 开放问题：LLM回答
        if ("llm".equalsIgnoreCase(aiProperties.getMode())) {
            try {
                // 2.1 制度/流程类：严格 JSON 输出 + 后端解析回填
                if (isPolicyQuestion(q)) {
                    String systemPrompt = """
                            你是“智慧校园教务制度助手”。
                            你的任务：回答学生关于补考/缓考/重修/挂科处理等制度流程问题。
                            核心要求（非常重要）：
                            - 直接给出“通用办理流程”，不要向用户反问任何问题、不要追问补充信息；
                            - 不要说“需要更多信息/无法判断/请补充xxx”，最多用一句“以学院/教务处通知为准”处理差异；
                            - 不要输出任何多余文字、不要输出 Markdown、不要输出 system/user/assistant；
                            - 必须只输出严格 JSON，格式固定为：
                              {"answer":"...","evidence":["..."],"actionPlan":["..."]}；
                            - evidence 与 actionPlan 必须是一维字符串数组；
                            - evidence 给2-4条“依据/注意点”，actionPlan 给3-6条“可执行步骤”。
                            """;

                    String userPrompt = "问题：" + q;

                    String llmText = llmClient.chat(systemPrompt, userPrompt);
                    PolicyJson pj = tryParseLastPolicyJson(llmText);

                    if (pj != null && pj.answer != null && !pj.answer.isBlank()) {
                        return new AiChatResponse(
                                pj.answer.trim(),
                                pj.evidence == null ? List.of() : pj.evidence,
                                pj.actionPlan == null ? List.of() : pj.actionPlan
                        );
                    }

                    // 解析失败时：兜底也禁止反问，直接给通用流程
                    return new AiChatResponse(
                            "挂科后一般有三类处理路径：补考、缓考（符合条件时）、重修。具体安排与资格以学院/教务处通知为准。",
                            List.of(
                                    "不同学院/课程的补考与重修规则可能不同，以校内通知为准。",
                                    "成绩相关事项通常需在教务系统或学院规定渠道办理。"
                            ),
                            List.of(
                                    "先在教务系统或成绩查询处确认不及格课程与学期。",
                                    "查看学院/教务处发布的补考/重修通知与时间安排。",
                                    "按通知要求在教务系统提交补考/重修申请（如需材料按要求上传/提交）。",
                                    "按时参加补考或完成重修课程学习与考核。",
                                    "考核结束后在系统核对成绩与学分认定情况。"
                            )
                    );
                }

                // 2.2 学业建议类：不再给“scores JSON”，改成纯文本，杜绝模型复述 { }
                String systemPrompt = """
                        你是“智慧校园学业助手”。
                        回答风格：自然、友好、可执行，不要过度模板化。
                        要求：
                        - 优先依据给定数据回答，不编造课程和分数；
                        - 绝对不要输出任何花括号 JSON（不要出现 { 或 }）；
                        - 用自然语言给出建议，最后给出2-3条可执行建议。
                        """;

                String userPrompt = """
                        用户问题：%s

                        学业数据（仅供你内部推理，不要复述输出）：
                        - 平均分：%.1f
                        - 弱势课程：%s（%.1f）
                        - 优势课程：%s（%.1f）
                        - 在读课程数：%d
                        - 最近成绩列表：%s
                        """.formatted(
                        q,
                        avg,
                        weakClass, score(min),
                        strongClass, score(max),
                        enrollments.size(),
                        toScoresText(grades)
                );

                String llmAnswer = llmClient.chat(systemPrompt, userPrompt);
                if (llmAnswer != null && !llmAnswer.isBlank()) {
                    // 再加一道“硬过滤”：如果模型还是吐了 { }，直接走兜底，避免 UI 出现 JSON
                    if (llmAnswer.contains("{") || llmAnswer.contains("}")) {
                        return new AiChatResponse(
                                fallbackStudyAnswer(avg, weakClass, score(min), strongClass, score(max)),
                                evidence,
                                actionPlan
                        );
                    }
                    return new AiChatResponse(llmAnswer.trim(), evidence, actionPlan);
                }
            } catch (Exception ignored) {
                // 忽略异常，走兜底
            }
        }

        // 3) 兜底回答
        return new AiChatResponse(
                fallbackStudyAnswer(avg, weakClass, score(min), strongClass, score(max)),
                evidence,
                actionPlan
        );
    }

    // ---------- 制度类问题识别 ----------
    private boolean isPolicyQuestion(String q) {
        if (q == null) return false;
        String s = q.trim();
        String[] keys = {
                "挂科", "不及格", "补考", "缓考", "重修", "退课", "成绩复核", "申诉",
                "违纪", "处分", "考试", "补测", "补修", "补报名"
        };
        for (String k : keys) {
            if (s.contains(k)) return true;
        }
        return false;
    }

    // ---------- 解析最后一个可用 JSON（比 lastIndexOf 更稳） ----------
    private PolicyJson tryParseLastPolicyJson(String text) {
        if (text == null) return null;

        // 清理控制字符
        String cleaned = text.replaceAll("[\\x00-\\x1F\\x7F]", "");

        // 抓取所有 { ... } 片段（非完美，但足够应对“前面夹杂文本/模板 JSON”的情况）
        Pattern p = Pattern.compile("\\{.*?\\}", Pattern.DOTALL);
        Matcher m = p.matcher(cleaned);

        List<String> cands = new ArrayList<>();
        while (m.find()) {
            cands.add(m.group());
        }

        // 从后往前尝试解析，取最后一个能解析的 JSON
        for (int i = cands.size() - 1; i >= 0; i--) {
            String cand = cands.get(i);
            try {
                PolicyJson pj = MAPPER.readValue(cand, PolicyJson.class);
                if (pj != null && pj.answer != null) return pj;
            } catch (Exception ignored) {
            }
        }
        return null;
    }

    // ---------- 随机委婉拒绝文案 ----------
    private String buildGuidedRefusal() {
        List<String> templates = List.of(
                """
                这个问题和学习关联不大，我先不误导你啦～
                不过我可以立刻帮你把学习安排清楚，你可以试试问：
                1）我的弱势科目是什么？
                2）我这周应该优先复习什么？
                3）请给我一份本周学习计划
                """,
                """
                我主要负责学业分析，这个话题我就先不展开啦～
                要不要我基于你当前成绩，给你一个可执行的提升方案？
                你可以问：
                1）我的平均分是多少？
                2）我的优势和弱势科目分别是什么？
                3）接下来7天怎么复习更高效？
                """,
                """
                这个问题比较生活化，我的强项是学习支持～
                如果你愿意，我可以马上给你“薄弱科目优先”的复习建议。
                示例问题：
                1）我现在最该补哪门课？
                2）如何把平均分提高到75+？
                3）请按我的数据生成本周学习计划
                """
        );
        return templates.get(RANDOM.nextInt(templates.size())).trim();
    }

    private String toSimpleScores(List<Grade> grades) {
        // 仍保留（你其他地方可能用到），但学业 LLM 不再用它
        return grades.stream()
                .map(g -> "{\"course\":\"" + className(g.getTeachingClass()) + "\",\"score\":" + score(g) + "}")
                .collect(Collectors.joining(",", "[", "]"));
    }

    private String toScoresText(List<Grade> grades) {
        return grades.stream()
                .sorted(Comparator.comparingDouble(this::score))
                .limit(10)
                .map(g -> className(g.getTeachingClass()) + ":" + score(g))
                .collect(Collectors.joining("; "));
    }

    private List<String> defaultActionPlan(String weakClass) {
        List<String> actionPlan = new ArrayList<>();
        actionPlan.add("本周先复习《" + weakClass + "》2次，每次45分钟，并完成1套小测。");
        actionPlan.add("将错题分类复盘（概念/计算/审题）。");
        actionPlan.add("每日结束前10分钟回顾当天知识点。");
        return actionPlan;
    }

    private String fallbackStudyAnswer(double avg, String weakClass, double weakScore, String strongClass, double strongScore) {
        return """
                从你目前数据看，平均分 %.1f，薄弱课程是 %s（%.1f分）。
                建议你本周优先补弱、再做巩固：
                - 先把 %s 安排2次针对性复习（每次45分钟）；
                - 每天结束前用10分钟做错题回顾；
                - 保持优势课程 %s（%.1f分）的低频巩固。
                """.formatted(avg, weakClass, weakScore, weakClass, strongClass, strongScore).trim();
    }

    // ---------- 分类逻辑 ----------
    private boolean isLowRelevanceQuestion(String q) {
        String[] lowRel = {
                "吃什么", "午饭", "晚饭", "早餐", "点外卖", "奶茶",
                "天气", "电影", "音乐", "游戏", "旅游", "穿什么", "星座", "八卦", "恋爱",
                // 情绪/运动/闲聊
                "好累", "很累", "累了", "压力", "焦虑", "难过", "烦", "抑郁", "失眠", "想哭",
                "游泳", "跑步", "健身", "运动"
        };
        for (String k : lowRel) {
            if (q.contains(k)) return true;
        }
        return false;
    }

    private boolean isWeakCourseQuestion(String q) {
        return containsAny(q, "弱势", "薄弱", "最差", "最低分", "弱项");
    }

    private boolean isAverageQuestion(String q) {
        return containsAny(q, "平均分", "均分");
    }

    private boolean isStrongCourseQuestion(String q) {
        return containsAny(q, "优势", "最好", "最高分", "强项");
    }

    private boolean isCourseLoadQuestion(String q) {
        return containsAny(q, "在读", "几门课", "课程数", "负载");
    }

    private boolean containsAny(String q, String... keys) {
        for (String k : keys) {
            if (q.contains(k)) return true;
        }
        return false;
    }

    private double score(Grade g) {
        return g.getScore() == null ? 0 : g.getScore();
    }

    private String className(TeachingClass tc) {
        if (tc == null) return "未知课程";
        if (tc.getClassCode() != null && !tc.getClassCode().isBlank()) return tc.getClassCode();
        return "课程#" + tc.getId();
    }
}