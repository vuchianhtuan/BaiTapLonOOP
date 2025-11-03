package com.mygame.arkanoid.ui.hud;
import com.mygame.arkanoid.core.GameManager;
import com.mygame.arkanoid.engine.AssetManager;
import com.mygame.arkanoid.systems.ScalingManager;

import java.awt.Graphics; // CHỈ DÙNG Graphics
import java.awt.image.BufferedImage;

/**
 * Lớp HeartUI quản lý hiển thị tim mạng trong giao diện người dùng.
 */
public class HeartUI {
    private GameManager gameManager;
    private String imageName;
    private int lastKnownLives;
    private static final int HEART_SIZE = 24;
    private int lifeLossBlinkTimer = 0;

    public HeartUI(GameManager gm) {
        this.gameManager = gm;
        this.lastKnownLives = gm.getLives();
        this.imageName = "heart";
    }

    /**
     * Cập nhật trạng thái hiển thị tim mạng, bao gồm logic nhấp nháy khi mất mạng.
     */
    public void update() {
        int currentLives = gameManager.getLives();
        if (currentLives < lastKnownLives) {
            lifeLossBlinkTimer = 180; // Bắt đầu 1 giây nhấp nháy (60 frames)
            lastKnownLives = currentLives;
        }

        // Cập nhật bộ đếm thời gian nhấp nháy
        if (lifeLossBlinkTimer > 0) {
            lifeLossBlinkTimer--;
        }
    }

    public void draw(Graphics g) {
        ScalingManager sm = ScalingManager.getInstance();

        int lives = gameManager.getLives();
        BufferedImage img = AssetManager.getInstance().getImage(this.imageName);
        if (img == null) return;

        // Xác định xem có nên vẽ tim hay không (logic nhấp nháy)
        boolean shouldDraw = true;
        if (lifeLossBlinkTimer > 0) {
            // Cứ 5 frame thì ẩn, 5 frame thì hiện
            if ((lifeLossBlinkTimer / 20) % 2 != 0) {
                shouldDraw = false;
            }
        }
        // Nếu lifeLossBlinkTimer <= 0, shouldDraw luôn là true (không nhấp nháy)

        // Chỉ vẽ nếu logic cho phép
        if (shouldDraw) {
            // Lặp qua số mạng để vẽ
            for (int i = 0; i < gameManager.getLives(); i++) {
                int logicBaseX = 980; // 960 (game) + 20 (padding)
                int logicBaseY = 150; // Vị trí Y (bạn có thể điều chỉnh)
                int heartPadding = 5;

                // Logic chia 3 tim mỗi hàng (từ yêu cầu trước)
                final int HEARTS_PER_ROW = 3;
                int heartSpacingX = HEART_SIZE + heartPadding;
                int heartSpacingY = HEART_SIZE + heartPadding;

                int col = i % HEARTS_PER_ROW; // Cột (0, 1, 2)
                int row = i / HEARTS_PER_ROW; // Hàng (0, 1, ...)

                int logicX = logicBaseX + (col * heartSpacingX);
                int logicY = logicBaseY + (row * heartSpacingY);

                // Sử dụng ScalingManager để "dịch" tọa độ và kích thước logic ra màn hình thật
                g.drawImage(img,
                        sm.scaleX(logicX),
                        sm.scaleY(logicY),
                        sm.scaleWidth(HEART_SIZE),
                        sm.scaleHeight(HEART_SIZE),
                        null);
            }
        }
    }
}