package com.example.smartcampus.service;

import com.example.smartcampus.exception.BusinessException;
import com.example.smartcampus.exception.ErrorCode;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class CaptchaService {

    private static class CaptchaItem {
        String code;
        long expireAt; // epoch ms
    }

    private final Map<String, CaptchaItem> cache = new ConcurrentHashMap<>();
    private static final long EXPIRE_MS = 2 * 60 * 1000; // 2分钟

    public record CaptchaData(String captchaId, String imageBase64) {}

    public CaptchaData generate() {
        String code = randomCode(4);
        String captchaId = UUID.randomUUID().toString();

        CaptchaItem item = new CaptchaItem();
        item.code = code.toLowerCase();
        item.expireAt = Instant.now().toEpochMilli() + EXPIRE_MS;
        cache.put(captchaId, item);

        return new CaptchaData(captchaId, drawBase64(code));
    }

    public void verifyAndConsume(String captchaId, String inputCode) {
        CaptchaItem item = cache.remove(captchaId); // 用一次即失效
        if (item == null) {
            throw new BusinessException(ErrorCode.CAPTCHA_EXPIRED, "验证码已失效，请刷新后重试");
        }
        if (Instant.now().toEpochMilli() > item.expireAt) {
            throw new BusinessException(ErrorCode.CAPTCHA_EXPIRED, "验证码已过期，请刷新后重试");
        }
        if (inputCode == null || !item.code.equals(inputCode.trim().toLowerCase())) {
            throw new BusinessException(ErrorCode.CAPTCHA_INVALID, "验证码错误");
        }
    }

    private String randomCode(int len) {
        String seed = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
        StringBuilder sb = new StringBuilder(len);
        for (int i = 0; i < len; i++) sb.append(seed.charAt((int) (Math.random() * seed.length())));
        return sb.toString();
    }

    private String drawBase64(String code) {
        try {
            int w = 110, h = 40;
            BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
            Graphics2D g = img.createGraphics();
            g.setColor(Color.WHITE);
            g.fillRect(0, 0, w, h);

            for (int i = 0; i < 8; i++) {
                g.setColor(new Color((int)(Math.random()*255),(int)(Math.random()*255),(int)(Math.random()*255)));
                g.drawLine((int)(Math.random()*w), (int)(Math.random()*h), (int)(Math.random()*w), (int)(Math.random()*h));
            }

            g.setFont(new Font("Arial", Font.BOLD, 24));
            for (int i = 0; i < code.length(); i++) {
                g.setColor(new Color((int)(Math.random()*120),(int)(Math.random()*120),(int)(Math.random()*120)));
                g.drawString(String.valueOf(code.charAt(i)), 20 + i * 20, 28 + (int)(Math.random()*6 - 3));
            }
            g.dispose();

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            ImageIO.write(img, "png", out);
            String b64 = Base64.getEncoder().encodeToString(out.toByteArray());
            return "data:image/png;base64," + b64;
        } catch (Exception e) {
            throw new RuntimeException("生成验证码失败", e);
        }
    }
}