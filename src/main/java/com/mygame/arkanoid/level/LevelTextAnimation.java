package com.mygame.arkanoid.level;

import com.mygame.arkanoid.systems.ScalingManager;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;

/**
 * Quản lý máy trạng thái (state machine) cho hoạt ảnh văn bản "LEVEL X".
 * Hoạt ảnh bao gồm 3 pha: trượt vào (slide in), đứng yên (stay), và mờ dần (fade out).
 */
public class LevelTextAnimation {
    /** Các trạng thái (pha) của hoạt ảnh. */
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

    // Vị trí logic (trong tọa độ gốc 1120x720)
    private static final int Y_START = 150;
    private static final int Y_FINAL = 300;

    private Font levelFont;

    /**
     * Khởi tạo hoạt ảnh.
     * Tải font chữ và đặt trạng thái ban đầu là IDLE.
     */
    public LevelTextAnimation() {
        // Tùy chỉnh Font chữ của bạn ở đây
        this.levelFont = new Font("Arial", Font.BOLD, 48);
        this.currentState = AnimState.IDLE;
    }

    /**
     * Bắt đầu hoặc kích hoạt (trigger) hoạt ảnh cho một level cụ thể.
     * Đặt lại tất cả các biến và chuyển trạng thái sang `SLIDING_IN`.
     *
     * @param levelNumber Số của level (ví dụ: 1, 2) để hiển thị "LEVEL 1".
     */
    public void start(int levelNumber) {
        this.levelString = "LEVEL " + levelNumber;
        this.currentState = AnimState.SLIDING_IN;
        this.timer = 0; // Reset bộ đếm frame
        this.alpha = 0.0f; // Bắt đầu với alpha = 0
        this.yPos = Y_START; // Đặt lại vị trí Y
    }

    /**
     * Cập nhật logic của hoạt ảnh (được gọi mỗi frame).
     * Xử lý việc chuyển đổi giữa các trạng thái (`SLIDING_IN`, `STAYING`, `FADING_OUT`)
     * dựa trên thời gian (timer).
     */
    public void update() {
        if (currentState == AnimState.IDLE) return;

        timer++;

        switch (currentState) {
            case SLIDING_IN:
                // Tính toán tiến trình (từ 0.0 đến 1.0)
                float progress = (float) timer / DURATION_SLIDE_IN;
                this.alpha = progress; // Alpha tăng dần (fade-in)
                // Y_Pos di chuyển (trượt xuống) theo tiến trình (Nội suy tuyến tính - Lerp)
                this.yPos = (int) (Y_START + (Y_FINAL - Y_START) * progress);

                /**
                 * Kết thúc pha trượt vào: Chuyển sang STAYING
                 */
                if (timer >= DURATION_SLIDE_IN) {
                    this.alpha = 1.0f; // Đảm bảo giá trị cuối chính xác
                    this.yPos = Y_FINAL;
                    this.currentState = AnimState.STAYING;
                    this.timer = 0; // Reset timer cho pha mới
                }
                break;

            case STAYING:
                // Giữ nguyên alpha = 1.0, yPos = Y_FINAL
                // Hết thời gian chờ, chuyển sang FADING_OUT
                if (timer >= DURATION_STAY) {
                    this.currentState = AnimState.FADING_OUT;
                    this.timer = 0;
                }
                break;

            case FADING_OUT:
                // Giữ nguyên yPos = Y_FINAL
                // Alpha giảm dần (fade-out)
                this.alpha = 1.0f - ((float) timer / DURATION_FADE_OUT);

                // Hoàn thành, quay về IDLE
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

    /**
     * Vẽ hoạt ảnh chữ LEVEL lên màn hình.
     * Tự động xử lý co giãn (scaling) và độ mờ (alpha).
     *
     * @param g Đối tượng Graphics để vẽ.
     */
    public void render(Graphics g) {
        // Không vẽ gì nếu đang IDLE hoặc đã mờ hẳn
        if (currentState == AnimState.IDLE || this.alpha <= 0.0f) {
            return;
        }

        Graphics2D g2d = (Graphics2D) g;
        // Lấy ScalingManager để xử lý co giãn cửa sổ
        ScalingManager sm = ScalingManager.getInstance();

        // Áp dụng độ mờ (alpha) chung
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, this.alpha));

        // Tính toán kích thước font chữ mới dựa trên tỷ lệ co giãn
        Font scaledFont = levelFont.deriveFont((float)(levelFont.getSize() * sm.getScale()));
        g2d.setFont(scaledFont);
        g2d.setColor(Color.WHITE);

        // --- Căn giữa văn bản theo chiều ngang (trong khu vực GAME_AREA) ---
        FontMetrics fm = g2d.getFontMetrics(scaledFont);
        int stringWidth_screen = fm.stringWidth(levelString); // Chiều rộng (đã scale)
        // Tìm tọa độ X (đã scale) của TÂM khu vực chơi
        int game_area_center_screen_x = sm.scaleX(sm.GAME_AREA_WIDTH / 2);
        // Căn giữa văn bản
        int drawX_screen = game_area_center_screen_x - (stringWidth_screen / 2);

        // Chuyển đổi Y logic sang Y màn hình (đã scale)
        int drawY_screen = sm.scaleY(yPos);

        g2d.drawString(levelString, drawX_screen, drawY_screen);

        // QUAN TRỌNG: Reset alpha về 1.0f (mặc định)
        // để không ảnh hưởng đến các thao tác vẽ khác sau này.
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
    }
}