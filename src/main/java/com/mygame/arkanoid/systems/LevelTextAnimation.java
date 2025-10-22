// Đặt trong com.mygame.arkanoid.systems
package com.mygame.arkanoid.systems;

import com.mygame.arkanoid.core.GamePanel;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;

public class LevelTextAnimation {
    // Trạng thái của hoạt ảnh
    private enum AnimState {
        IDLE,
        SLIDING_IN,
        STAYING,
        FADING_OUT
    }

    private AnimState currentState = AnimState.IDLE;
    private String levelString;
    private int timer;
    private float alpha;
    private int yPos;

    // Thời gian cho mỗi pha (tính bằng frame, giả sử 60FPS)
    private static final int DURATION_SLIDE_IN = 60; // 1 giây
    private static final int DURATION_STAY = 90;     // 1.5 giây
    private static final int DURATION_FADE_OUT = 60; // 1 giây

    // Vị trí
    private static final int Y_START = 150;
    private static final int Y_FINAL = 300;

    private Font levelFont;

    public LevelTextAnimation() {
        // Tùy chỉnh Font chữ của bạn ở đây
        this.levelFont = new Font("Arial", Font.BOLD, 48);
        this.currentState = AnimState.IDLE;
    }

    /**
     * Bắt đầu hoạt ảnh cho một level cụ thể.
     */
    public void start(int levelNumber) {
        this.levelString = "LEVEL " + levelNumber;
        this.currentState = AnimState.SLIDING_IN;
        this.timer = 0;
        this.alpha = 0.0f;
        this.yPos = Y_START;
    }

    public void update() {
        if (currentState == AnimState.IDLE) return;

        timer++;

        switch (currentState) {
            case SLIDING_IN:
                float progress = (float) timer / DURATION_SLIDE_IN;
                this.alpha = progress; // Mờ dần vào
                this.yPos = (int) (Y_START + (Y_FINAL - Y_START) * progress); // Trượt xuống

                if (timer >= DURATION_SLIDE_IN) {
                    this.alpha = 1.0f;
                    this.yPos = Y_FINAL;
                    this.currentState = AnimState.STAYING;
                    this.timer = 0;
                }
                break;

            case STAYING:
                // Giữ nguyên alpha = 1.0, yPos = Y_FINAL
                if (timer >= DURATION_STAY) {
                    this.currentState = AnimState.FADING_OUT;
                    this.timer = 0;
                }
                break;

            case FADING_OUT:
                // Giữ nguyên yPos = Y_FINAL
                this.alpha = 1.0f - ((float) timer / DURATION_FADE_OUT); // Mờ dần ra

                if (timer >= DURATION_FADE_OUT) {
                    this.alpha = 0.0f;
                    this.currentState = AnimState.IDLE; // Hoàn thành
                    this.timer = 0;
                }
                break;
        }

        // Đảm bảo giá trị alpha luôn nằm trong khoảng [0, 1]
        if (this.alpha < 0.0f) this.alpha = 0.0f;
        if (this.alpha > 1.0f) this.alpha = 1.0f;
    }

    public void render(Graphics g) {
        if (currentState == AnimState.IDLE || this.alpha <= 0.0f) {
            return;
        }

        Graphics2D g2d = (Graphics2D) g;

        // Bật chế độ trong suốt (alpha)
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, this.alpha));

        g2d.setFont(levelFont);
        g2d.setColor(Color.WHITE);

        // Canh giữa chữ
        FontMetrics fm = g2d.getFontMetrics(levelFont);
        int stringWidth = fm.stringWidth(levelString);
        int x = (GamePanel.WIDTH - stringWidth) / 2;

        // Vẽ chữ tại vị trí yPos đã được update
        g2d.drawString(levelString, x, yPos);

        // Trả lại Composite mặc định
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
    }
}