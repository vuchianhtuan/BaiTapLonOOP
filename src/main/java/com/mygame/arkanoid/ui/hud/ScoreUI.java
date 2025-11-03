package com.mygame.arkanoid.ui.hud;

import com.mygame.arkanoid.core.GameManager;
import com.mygame.arkanoid.systems.ScalingManager;
import java.awt.*;

/**
 * Quản lý logic hiển thị Điểm số (Score) của người chơi
 * trên giao diện HUD (Heads-Up Display).
 * <p>
 * Lớp này chịu trách nhiệm lấy điểm số (score) hiện tại từ
 * {@link GameManager} và vẽ (render) nó lên màn hình (Sidebar)
 * với font chữ và vị trí đã định sẵn.
 */
public class ScoreUI {
    GameManager gameManager;

    /**
     * Khởi tạo trình quản lý Score UI.
     *
     * @param gm Tham chiếu đến {@link GameManager} để lấy điểm số (score).
     */
    public ScoreUI(GameManager gm) {
        this.gameManager = gm;
    }

    /**
     * (Bỏ trống) Phương thức cập nhật (update) logic.
     * ScoreUI là một đối tượng bị động (passive), không cần
     * logic cập nhật mỗi frame.
     */
    public void update() {
        // Hiện chưa có logic gì để update
    }

    /**
     * Vẽ (render) tiêu đề "SCORE" và giá trị điểm số
     * lên màn hình (HUD/Sidebar).
     * <p>
     * Tự động co giãn (scale) font chữ và vị trí vẽ
     * dựa trên {@link ScalingManager}.
     *
     * @param g Đối tượng Graphics để vẽ.
     */
    public void draw(Graphics g) {
        ScalingManager sm = ScalingManager.getInstance();

        String scoreText = "SCORE";
        String scoreValue = String.valueOf(gameManager.getScore());

        // Tọa độ (logic) gốc
        int logicX = 980; // 960 (game area) + 20 (padding)
        int logicY_title = 100;
        int logicY_value = 130;

        // --- 1. Vẽ tiêu đề "SCORE" ---
        Font baseFont = new Font("Arial", Font.BOLD, 24);
        // Lấy font đã scale
        Font scaledFont = baseFont.deriveFont((float)(baseFont.getSize() * sm.getScale()));

        g.setFont(scaledFont);
        g.setColor(Color.WHITE);
        // Vẽ tại vị trí đã scale
        g.drawString(scoreText, sm.scaleX(logicX), sm.scaleY(logicY_title));

        // --- 2. Vẽ giá trị điểm số (ví dụ: "12345") ---
        baseFont = new Font("Arial", Font.PLAIN, 22);
        scaledFont = baseFont.deriveFont((float)(baseFont.getSize() * sm.getScale()));

        g.setFont(scaledFont);
        g.drawString(scoreValue, sm.scaleX(logicX), sm.scaleY(logicY_value));
    }
}