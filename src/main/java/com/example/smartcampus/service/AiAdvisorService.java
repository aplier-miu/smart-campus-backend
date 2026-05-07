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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
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

        // 2) 学习相关开放问题：LLM回答（自然风格 + 基于数据）
        if ("llm".equalsIgnoreCase(aiProperties.getMode())) {
            try {
                String systemPrompt = """
                        你是“智慧校园学业助手”。
                        回答风格：自然、友好、可执行，不要过度模板化。
                        要求：
                        - 优先依据给定数据回答，不编造课程和分数；
                        - 如果用户问题是学习相关，就结合数据给建议；
                        - 输出可稍微灵活，不强制固定格式；
                        - 最后尽量给出2-3条可执行建议。
                        """;

                String userPrompt = """
                        用户问题：%s

                        学业数据（JSON）：
                        {
                          "avg": %.1f,
                          "weakCourse": {"name":"%s","score":%.1f},
                          "strongCourse": {"name":"%s","score":%.1f},
                          "enrollCount": %d,
                          "scores": %s
                        }
                        """.formatted(
                        q,
                        avg,
                        weakClass, score(min),
                        strongClass, score(max),
                        enrollments.size(),
                        toSimpleScores(grades)
                );

                String llmAnswer = llmClient.chat(systemPrompt, userPrompt);
                if (llmAnswer != null && !llmAnswer.isBlank()) {
                    return new AiChatResponse(llmAnswer.trim(), evidence, actionPlan);
                }
            } catch (Exception ignored) {
                // 忽略异常，走兜底
            }
        }

        // 3) 兜底回答
        String fallback = """
                从你目前数据看，平均分 %.1f，薄弱课程是 %s（%.1f分）��
                建议你本周优先补弱、再做巩固：
                - 先把 %s 安排2次针对性复习（每次45分钟）；
                - 每天结束前用10分钟做错题回顾；
                - 保持优势课程 %s（%.1f分）的低频巩固。
                """.formatted(avg, weakClass, score(min), weakClass, strongClass, score(max));

        return new AiChatResponse(fallback.trim(), evidence, actionPlan);
    }

    // ---------- 随机委婉拒绝文案 ----------
    private String buildGuidedRefusal() {
        List<String> templates = List.of(
                """
                这个问题和学习关联不大，我先不误导你啦🙂
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
                这个问题比较生活化，我的强项是学习支持😉
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
        return grades.stream()
                .map(g -> "{\"course\":\"" + className(g.getTeachingClass()) + "\",\"score\":" + score(g) + "}")
                .collect(Collectors.joining(",", "[", "]"));
    }

    private List<String> defaultActionPlan(String weakClass) {
        List<String> actionPlan = new ArrayList<>();
        actionPlan.add("本周先复习《" + weakClass + "》2次，每次45分钟，并完成1套小测。");
        actionPlan.add("将错题分类复盘（概念/计算/审题）。");
        actionPlan.add("每日结束前10分钟回顾当天知识点。");
        return actionPlan;
    }

    // ---------- 分类逻辑 ----------
    private boolean isLowRelevanceQuestion(String q) {
        String[] lowRel = {
                "吃什么", "午饭", "晚饭", "早餐", "点外卖", "奶茶",
                "天气", "电影", "音乐", "游戏", "旅游", "穿什么", "星座", "八卦", "恋爱"
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