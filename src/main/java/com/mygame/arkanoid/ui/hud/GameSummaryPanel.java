package com.mygame.arkanoid.ui.hud;

import com.mygame.arkanoid.systems.ScalingManager;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.FontMetrics;
import java.awt.GradientPaint; // Để vẽ gradient
import java.awt.Graphics2D;    // Để sử dụng GradientPaint và setRenderingHint
import java.awt.RenderingHints; // Để làm mịn các đường vẽ (anti-aliasing)
import java.awt.geom.RoundRectangle2D; // Để vẽ hình chữ nhật bo góc

/**
 * Quản lý và vẽ (render) bảng tóm tắt (summary panel)
 * được hiển thị khi kết thúc trò chơi (GAME_OVER hoặc GAME_WIN).
 * <p>
 * Lớp này chịu trách nhiệm vẽ một hộp thoại (dialog) bo góc,
 * có nền gradient, đổ bóng, và hiển thị các thông tin cuối cùng
 * như Tiêu đề (thắng/thua), Điểm số, và Thời gian chơi.
 */
public class GameSummaryPanel {

    // Kích thước và vị trí logic (native coordinates) của hộp thoại
    private int logicWidth, logicHeight;
    private int logicX, logicY;

    /** Kích thước (logic) của đường cong bo góc. */
    private final int ARC_SIZE = 30;

    // Các loại font chữ (gốc, chưa scale)
    private Font titleFont;
    private Font statLabelFont; // Font cho "Total Score:", "Total Time:"
    private Font statValueFont; // Font cho giá trị "12345", "05:30"
    private Font messageFont;   // Font cho thông báo nhỏ dưới cùng

    public GameSummaryPanel() {
        this.logicWidth = 600; // Chiều rộng logic
        this.logicHeight = 400; // Chiều cao logic

        // Căn giữa hộp thoại dựa trên kích thước logic (native) của game
        ScalingManager sm = ScalingManager.getInstance();
        this.logicX = (sm.NATIVE_WIDTH - this.logicWidth) / 2;
        this.logicY = (sm.NATIVE_HEIGHT - this.logicHeight) / 2;

        // Khởi tạo các đối tượng Font (sẽ được scale sau khi vẽ)
        this.titleFont = new Font("Arial", Font.BOLD, 52);
        this.statLabelFont = new Font("Arial", Font.PLAIN, 30);
        this.statValueFont = new Font("Arial", Font.BOLD, 30);
        this.messageFont = new Font("Arial", Font.ITALIC, 20);
    }

    /**
     * Chuyển đổi (format) tổng số mili-giây sang định dạng chuỗi "MM:SS".
     *
     * @param millis Tổng số mili-giây.
     * @return Chuỗi (String) đã định dạng (ví dụ: "05:01").
     */
    private String formatTime(long millis) {
        long totalSeconds = millis / 1000;
        long minutes = totalSeconds / 60;
        long seconds = totalSeconds % 60;
        // String.format("%02d", ...) đảm bảo số luôn có 2 chữ số (ví dụ: "05" thay vì "5")
        return String.format("%02d:%02d", minutes, seconds);
    }

    /**
     * Vẽ toàn bộ bảng tóm tắt lên màn hình.
     * <p>
     * Phương thức này thực hiện nhiều bước vẽ phức tạp:
     * 1. Vẽ bóng (drop shadow) mờ.
     * 2. Vẽ nền (background) gradient bo góc.
     * 3. Vẽ viền (border) bo góc.
     * 4. Vẽ văn bản (Tiêu đề, Điểm, Thời gian, Thông báo)
     * đã được co giãn (scale) và căn chỉnh.
     *
     * @param g Đối tượng Graphics (sẽ được cast sang Graphics2D).
     * @param title Tiêu đề ("YOU WIN!" hoặc "GAME OVER").
     * @param score Điểm số cuối cùng.
     * @param playtimeMillis Thời gian chơi cuối cùng (tính bằng mili giây).
     */
    public void draw(Graphics g, String title, int score, long playtimeMillis) {
        ScalingManager sm = ScalingManager.getInstance();
        Graphics2D g2d = (Graphics2D) g;

        // Bật anti-aliasing (khử răng cưa) để đường vẽ và chữ mượt mà hơn
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Chuyển đổi tọa độ logic sang tọa độ màn hình (đã scale)
        int screenX = sm.scaleX(logicX);
        int screenY = sm.scaleY(logicY);
        int screenWidth = sm.scaleWidth(logicWidth);
        int screenHeight = sm.scaleHeight(logicHeight);
        int screenArcSize = sm.scaleWidth(ARC_SIZE); // Scale kích thước bo góc

        // --- 1. Vẽ hiệu ứng đổ bóng (Drop Shadow) ---
        // Vẽ một hình chữ nhật bo góc mờ (alpha=100)
        // lệch xuống dưới và sang phải (offset)
        g2d.setColor(new Color(0, 0, 0, 100)); // Màu đen, mờ
        int shadowOffset = sm.scaleWidth(10); // Độ lệch (offset) của bóng
        g2d.fill(new RoundRectangle2D.Double(screenX + shadowOffset, screenY + shadowOffset,
                screenWidth, screenHeight, screenArcSize, screenArcSize));

        // --- 2. Vẽ nền hộp (Gradient Background) ---
        Color startColor = new Color(50, 50, 50, 230); // Xám tối, hơi trong suốt
        Color endColor = new Color(10, 10, 10, 230);   // Đen, hơi trong suốt
        // Tạo hiệu ứng gradient (chuyển màu) từ trên xuống dưới
        GradientPaint gp = new GradientPaint(screenX, screenY, startColor,
                screenX, screenY + screenHeight, endColor);
        g2d.setPaint(gp);
        g2d.fill(new RoundRectangle2D.Double(screenX, screenY, screenWidth, screenHeight, screenArcSize, screenArcSize));

        // --- 3. Vẽ viền hộp (Border) ---
        g2d.setColor(new Color(180, 180, 180, 255)); // Màu trắng xám
        g2d.setStroke(new java.awt.BasicStroke(sm.scaleWidth(2))); // Độ dày viền 2px (đã scale)
        g2d.draw(new RoundRectangle2D.Double(screenX, screenY, screenWidth, screenHeight, screenArcSize, screenArcSize));

        // --- 4. Vẽ Tiêu đề (GAME OVER / YOU WIN) ---
        // Lấy font đã scale
        Font scaledTitleFont = titleFont.deriveFont((float)(titleFont.getSize() * sm.getScale()));
        g2d.setFont(scaledTitleFont);
        FontMetrics fmTitle = g2d.getFontMetrics();
        int titleWidth = fmTitle.stringWidth(title); // Lấy chiều rộng chuỗi (đã scale)

        // Căn giữa tiêu đề theo chiều ngang
        int titleDrawX = screenX + (screenWidth - titleWidth) / 2;
        int titleDrawY = screenY + sm.scaleY(70); // Vị trí Y (cách lề trên 70px logic)

        // Đặt màu dựa trên thắng/thua
        g2d.setColor(title.equals("YOU WIN!") ? new Color(100, 255, 100) : new Color(255, 80, 80));
        g2d.drawString(title, titleDrawX, titleDrawY);

        // --- 5. Vẽ Thống kê (Điểm và Thời gian) ---
        Font scaledStatLabelFont = statLabelFont.deriveFont((float)(statLabelFont.getSize() * sm.getScale()));
        Font scaledStatValueFont = statValueFont.deriveFont((float)(statValueFont.getSize() * sm.getScale()));

        // Vị trí cố định cho nhãn (căn lề trái)
        int labelDrawX = screenX + sm.scaleWidth(50);

        // Vị trí tham chiếu (căn lề phải) cho giá trị
        int valueRefX = screenX + screenWidth - sm.scaleWidth(50);

        // --- Vẽ điểm (Score) ---
        g2d.setFont(scaledStatLabelFont);
        g2d.setColor(Color.LIGHT_GRAY);
        g2d.drawString("Total Score:", labelDrawX, screenY + sm.scaleY(160));

        g2d.setFont(scaledStatValueFont);
        g2d.setColor(Color.WHITE);
        String scoreText = String.valueOf(score);
        int scoreWidth = g2d.getFontMetrics().stringWidth(scoreText);
        // Căn phải: Vị trí tham chiếu - chiều rộng chuỗi
        g2d.drawString(scoreText, valueRefX - scoreWidth, screenY + sm.scaleY(160));

        // --- Vẽ thời gian (Time) ---
        g2d.setFont(scaledStatLabelFont);
        g2d.setColor(Color.LIGHT_GRAY);
        g2d.drawString("Total Time:", labelDrawX, screenY + sm.scaleY(220));

        g2d.setFont(scaledStatValueFont);
        g2d.setColor(Color.WHITE);
        String timeText = formatTime(playtimeMillis); // Chuyển đổi (ví dụ: "05:30")
        int timeWidth = g2d.getFontMetrics().stringWidth(timeText);
        // Căn phải
        g2d.drawString(timeText, valueRefX - timeWidth, screenY + sm.scaleY(220));

        // --- 6. Vẽ thông báo quay lại menu ---
        Font scaledMessageFont = messageFont.deriveFont((float)(messageFont.getSize() * sm.getScale()));
        g2d.setFont(scaledMessageFont);
        g2d.setColor(new Color(200, 200, 255)); // Màu xanh nhạt
        String message = "Returning to menu in a moment...";
        FontMetrics fmMessage = g2d.getFontMetrics();
        int messageWidth = fmMessage.stringWidth(message);

        // Căn giữa
        int messageDrawX = screenX + (screenWidth - messageWidth) / 2;
        int messageDrawY = screenY + screenHeight - sm.scaleY(30); // Cách lề dưới 30px (logic)
        g2d.drawString(message, messageDrawX, messageDrawY);
    }
}