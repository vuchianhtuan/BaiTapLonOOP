package com.mygame.arkanoid.ui.hud;
import com.mygame.arkanoid.core.GameManager;
import com.mygame.arkanoid.engine.AssetManager;
import com.mygame.arkanoid.systems.ScalingManager;

import java.awt.Graphics; // CHỈ DÙNG Graphics
import java.awt.image.BufferedImage;

/**
 * Quản lý logic hiển thị số mạng (lives) của người chơi (biểu tượng trái tim)
 * trên giao diện HUD (Heads-Up Display).
 * <p>
 * Lớp này chịu trách nhiệm vẽ (render) đúng số lượng trái tim
 * và xử lý hiệu ứng "nhấp nháy" (blink) khi người chơi bị mất mạng.
 */
public class HeartUI {
    private GameManager gameManager;
    private String imageName;
    /** Lưu lại số mạng của frame trước để phát hiện khi nào bị mất mạng. */
    private int lastKnownLives;
    private static final int HEART_SIZE = 24;
    /** Bộ đếm (timer) điều khiển hiệu ứng nhấp nháy (tính bằng frame). */
    private int lifeLossBlinkTimer = 0;

    /**
     * Khởi tạo trình quản lý Heart UI.
     *
     * @param gm Tham chiếu đến {@link GameManager} để lấy số mạng (lives).
     */
    public HeartUI(GameManager gm) {
        this.gameManager = gm;
        this.lastKnownLives = gm.getLives(); // Khởi tạo số mạng ban đầu
        this.imageName = "heart";
    }

    /**
     * Cập nhật logic của Heart UI (được gọi mỗi frame).
     * <p>
     * Kiểm tra xem người chơi có bị mất mạng hay không (so sánh
     * {@code currentLives} với {@code lastKnownLives}).
     * Nếu có, kích hoạt {@code lifeLossBlinkTimer} để bắt đầu hiệu ứng nhấp nháy.
     * <p>
     * Đồng thời, đếm ngược {@code lifeLossBlinkTimer} nếu nó đang hoạt động.
     */
    public void update() {
        int currentLives = gameManager.getLives();
        // Phát hiện thời điểm vừa bị mất mạng
        if (currentLives < lastKnownLives) {
            lifeLossBlinkTimer = 180; // Kích hoạt nhấp nháy (ví dụ: 180 frame = 3 giây)
            lastKnownLives = currentLives; // Cập nhật số mạng
        }

        // Cập nhật (đếm ngược) bộ đếm thời gian nhấp nháy
        if (lifeLossBlinkTimer > 0) {
            lifeLossBlinkTimer--;
        }
    }

    /**
     * Vẽ (render) các biểu tượng trái tim lên màn hình (HUD/Sidebar).
     * <p>
     * <b>Logic nhấp nháy:</b> Nếu {@code lifeLossBlinkTimer > 0},
     * cờ {@code shouldDraw} sẽ được tính toán. Nó sẽ đảo trạng thái
     * (true/false) mỗi 20 frame, khiến cho các trái tim bị "ẩn" đi
     * một cách có chu kỳ, tạo hiệu ứng nhấp nháy.
     * <p>
     * <b>Logic xếp hàng:</b> Vẽ các trái tim theo một lưới (grid)
     * với {@code HEARTS_PER_ROW} (ví dụ: 3 trái tim mỗi hàng).
     *
     * @param g Đối tượng Graphics để vẽ.
     */
    public void draw(Graphics g) {
        ScalingManager sm = ScalingManager.getInstance();

        int lives = gameManager.getLives();
        BufferedImage img = AssetManager.getInstance().getImage(this.imageName);
        if (img == null) return; // Không vẽ nếu không tải được ảnh

        // --- Logic nhấp nháy ---
        boolean shouldDraw = true; // Mặc định là vẽ
        if (lifeLossBlinkTimer > 0) {
            // Tính toán logic nhấp nháy:
            // (timer / 20) % 2 != 0
            // Ví dụ: timer 180 -> 161: (9) % 2 != 0 -> true -> shouldDraw = false (ẨN)
            // Ví dụ: timer 160 -> 141: (8) % 2 != 0 -> false -> shouldDraw = true (HIỆN)
            // (Chu kỳ 20 frame ẨN, 20 frame HIỆN)
            if ((lifeLossBlinkTimer / 20) % 2 != 0) {
                shouldDraw = false;
            }
        }
        // Nếu lifeLossBlinkTimer <= 0, shouldDraw luôn là true

        // Chỉ vẽ nếu logic cho phép (không đang trong chu kỳ "ẩn")
        if (shouldDraw) {
            // --- Logic xếp hàng (Grid Layout) ---
            for (int i = 0; i < gameManager.getLives(); i++) {
                // Tọa độ (logic) gốc của trái tim đầu tiên (trên Sidebar)
                int logicBaseX = 980; // 960 (game area) + 20 (padding)
                int logicBaseY = 150;
                int heartPadding = 5;

                // Xếp 3 trái tim mỗi hàng
                final int HEARTS_PER_ROW = 3;
                int heartSpacingX = HEART_SIZE + heartPadding; // Tổng khoảng cách X
                int heartSpacingY = HEART_SIZE + heartPadding; // Tổng khoảng cách Y

                int col = i % HEARTS_PER_ROW; // Cột (0, 1, 2)
                int row = i / HEARTS_PER_ROW; // Hàng (0, 1, ...)

                // Tính tọa độ (logic) cuối cùng của trái tim này
                int logicX = logicBaseX + (col * heartSpacingX);
                int logicY = logicBaseY + (row * heartSpacingY);

                // Sử dụng ScalingManager để "dịch" tọa độ logic
                // sang tọa độ màn hình (vật lý) và vẽ
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