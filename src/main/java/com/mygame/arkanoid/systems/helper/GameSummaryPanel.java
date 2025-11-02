package com.mygame.arkanoid.systems.helper;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.FontMetrics;
import java.awt.GradientPaint; // Để vẽ gradient
import java.awt.Graphics2D;    // Để sử dụng GradientPaint và setRenderingHint
import java.awt.RenderingHints; // Để làm mịn các đường vẽ (anti-aliasing)
import java.awt.geom.RoundRectangle2D; // Để vẽ hình chữ nhật bo góc

public class GameSummaryPanel {

    private int logicWidth, logicHeight;
    private int logicX, logicY;

    private final int ARC_SIZE = 30; // Bo tròn 30px

    private Font titleFont;
    private Font statLabelFont; // Font cho "Total Score:", "Total Time:"
    private Font statValueFont; // Font cho giá trị "12345", "05:30"
    private Font messageFont;   // Font cho thông báo nhỏ dưới cùng

    public GameSummaryPanel() {
        this.logicWidth = 600; // Chiều rộng hộp
        this.logicHeight = 400; // Chiều cao hộp

        // Căn giữa hộp thống kê
        ScalingManager sm = ScalingManager.getInstance();
        this.logicX = (sm.NATIVE_WIDTH - this.logicWidth) / 2;
        this.logicY = (sm.NATIVE_HEIGHT - this.logicHeight) / 2;

        // Chuẩn bị font
        this.titleFont = new Font("Arial", Font.BOLD, 52);
        this.statLabelFont = new Font("Arial", Font.PLAIN, 30);
        this.statValueFont = new Font("Arial", Font.BOLD, 30);
        this.messageFont = new Font("Arial", Font.ITALIC, 20);
    }

    /**
     * Chuyển đổi mili-giây sang định dạng MM:SS
     */
    private String formatTime(long millis) {
        long totalSeconds = millis / 1000;
        long minutes = totalSeconds / 60;
        long seconds = totalSeconds % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }

    /**
     * Vẽ hộp thống kê
     * @param g Graphics context
     * @param title "YOU WIN!" hoặc "GAME OVER"
     * @param score Điểm số cuối cùng
     * @param playtimeMillis Thời gian chơi cuối cùng
     */
    public void draw(Graphics g, String title, int score, long playtimeMillis) {
        ScalingManager sm = ScalingManager.getInstance();
        Graphics2D g2d = (Graphics2D) g;

        // Bật anti-aliasing để đường vẽ và chữ mượt mà hơn
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Chuyển đổi tọa độ logic sang tọa độ màn hình đã scale
        int screenX = sm.scaleX(logicX);
        int screenY = sm.scaleY(logicY);
        int screenWidth = sm.scaleWidth(logicWidth);
        int screenHeight = sm.scaleHeight(logicHeight);
        int screenArcSize = sm.scaleWidth(ARC_SIZE); // Scale bo góc

        // --- 1. Vẽ hiệu ứng đổ bóng ---
        g2d.setColor(new Color(0, 0, 0, 100)); // Màu đen, mờ
        int shadowOffset = sm.scaleWidth(10); // Độ lệch của bóng
        g2d.fill(new RoundRectangle2D.Double(screenX + shadowOffset, screenY + shadowOffset,
                screenWidth, screenHeight, screenArcSize, screenArcSize));

        // --- 2. Vẽ nền hộp (Gradient) ---
        Color startColor = new Color(50, 50, 50, 230); // Xám tối, hơi trong suốt
        Color endColor = new Color(10, 10, 10, 230);   // Đen, hơi trong suốt
        GradientPaint gp = new GradientPaint(screenX, screenY, startColor,
                screenX, screenY + screenHeight, endColor);
        g2d.setPaint(gp);
        g2d.fill(new RoundRectangle2D.Double(screenX, screenY, screenWidth, screenHeight, screenArcSize, screenArcSize));

        // --- 3. Vẽ viền hộp ---
        g2d.setColor(new Color(180, 180, 180, 255)); // Màu trắng xám
        g2d.setStroke(new java.awt.BasicStroke(sm.scaleWidth(2))); // Độ dày viền 2px
        g2d.draw(new RoundRectangle2D.Double(screenX, screenY, screenWidth, screenHeight, screenArcSize, screenArcSize));

        // --- 4. Vẽ Tiêu đề (GAME OVER / YOU WIN) ---
        Font scaledTitleFont = titleFont.deriveFont((float)(titleFont.getSize() * sm.getScale()));
        g2d.setFont(scaledTitleFont);
        FontMetrics fmTitle = g2d.getFontMetrics();
        int titleWidth = fmTitle.stringWidth(title);

        int titleDrawX = screenX + (screenWidth - titleWidth) / 2;
        int titleDrawY = screenY + sm.scaleY(70); // Cách lề trên 70px

        g2d.setColor(title.equals("YOU WIN!") ? new Color(100, 255, 100) : new Color(255, 80, 80)); // Màu sắc nổi bật hơn
        g2d.drawString(title, titleDrawX, titleDrawY);

        // --- 5. Vẽ Thống kê (Điểm và Thời gian) ---
        Font scaledStatLabelFont = statLabelFont.deriveFont((float)(statLabelFont.getSize() * sm.getScale()));
        Font scaledStatValueFont = statValueFont.deriveFont((float)(statValueFont.getSize() * sm.getScale()));

        // Vị trí cố định cho nhãn (căn trái)
        int labelDrawX = screenX + sm.scaleX(50);

        // Vị trí cho giá trị (căn phải so với một điểm tham chiếu)
        int valueRefX = screenX + screenWidth - sm.scaleX(50); // Điểm tham chiếu bên phải

        // --- Score ---
        g2d.setFont(scaledStatLabelFont);
        g2d.setColor(Color.LIGHT_GRAY);
        g2d.drawString("Total Score:", labelDrawX, screenY + sm.scaleY(160));

        g2d.setFont(scaledStatValueFont);
        g2d.setColor(Color.WHITE);
        String scoreText = String.valueOf(score);
        int scoreWidth = g2d.getFontMetrics().stringWidth(scoreText);
        g2d.drawString(scoreText, valueRefX - scoreWidth, screenY + sm.scaleY(160));

        // --- Time ---
        g2d.setFont(scaledStatLabelFont);
        g2d.setColor(Color.LIGHT_GRAY);
        g2d.drawString("Total Time:", labelDrawX, screenY + sm.scaleY(220));

        g2d.setFont(scaledStatValueFont);
        g2d.setColor(Color.WHITE);
        String timeText = formatTime(playtimeMillis);
        int timeWidth = g2d.getFontMetrics().stringWidth(timeText);
        g2d.drawString(timeText, valueRefX - timeWidth, screenY + sm.scaleY(220));

        // --- 6. Vẽ thông báo quay lại menu ---
        Font scaledMessageFont = messageFont.deriveFont((float)(messageFont.getSize() * sm.getScale()));
        g2d.setFont(scaledMessageFont);
        g2d.setColor(new Color(200, 200, 255)); // Màu xanh nhạt
        String message = "Returning to menu in a moment...";
        FontMetrics fmMessage = g2d.getFontMetrics();
        int messageWidth = fmMessage.stringWidth(message);
        int messageDrawX = screenX + (screenWidth - messageWidth) / 2;
        int messageDrawY = screenY + screenHeight - sm.scaleY(30); // Cách lề dưới 30px
        g2d.drawString(message, messageDrawX, messageDrawY);
    }
}