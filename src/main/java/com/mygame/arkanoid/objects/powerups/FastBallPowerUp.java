package com.mygame.arkanoid.objects.powerups;
import com.mygame.arkanoid.core.GameManager;
import com.mygame.arkanoid.engine.AssetManager;
import com.mygame.arkanoid.objects.Ball;
import com.mygame.arkanoid.systems.ScalingManager;
import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * Lớp đại diện cho vật phẩm tăng sức mạnh (PowerUp) "Tăng Tốc Bóng" (Fast Ball).
 * <p>
 * Khi được thu thập, PowerUp này sẽ tăng tốc độ của bóng ({@link Ball})
 * lên một hệ số ({@code FAST_SPEED_FACTOR}) và kích hoạt trạng thái
 * "bốc cháy" (burning) của bóng (để có hiệu ứng hình ảnh).
 * <p>
 * Khi rơi, vật phẩm này có hiệu ứng "vệt mờ" (motion trail)
 * để trông sinh động hơn.
 */
public class FastBallPowerUp extends PowerUp {
    private String imageName = "fastBallPowerUp";
    /** Hệ số nhân tốc độ (ví dụ: 1.5 = tăng 50%). */
    private static final double FAST_SPEED_FACTOR = 1.5;
    /** Số lượng "ảnh mờ" (after-image) dùng để vẽ vệt. */
    private static final int TRAIL_SEGMENTS = 9;
    /** Khoảng cách (logic) giữa các vệt. */
    private static final int TRAIL_SPACING = 2;

    /**
     * Khởi tạo PowerUp "Tăng Tốc Bóng".
     *
     * @param x Vị trí X (logic).
     * @param y Vị trí Y (logic).
     * @param width Chiều rộng (logic).
     * @param height Chiều cao (logic).
     */
    public FastBallPowerUp(int x, int y, int width, int height) {
        // Gọi constructor lớp cha với loại "fast_ball" và thời gian 300 frame (5s)
        super(x, y, width, height, "fast_ball", 300);
    }

    /**
     * Áp dụng hiệu ứng: Tăng tốc độ bóng và đặt trạng thái "bốc cháy".
     * <p>
     * Tốc độ mới được tính dựa trên tốc độ *gốc* của bóng
     * ({@code originalSpeed}) để tránh cộng dồn/nhân dồn lỗi.
     *
     * @param gameManager Tham chiếu đến GameManager.
     */
    @Override public void applyEffect(GameManager gameManager) {
        Ball targetBall = gameManager.getBall();
        double originalSpeed = gameManager.getBall().getOriginalSpeed();
        // Đặt tốc độ mới = tốc độ gốc * hệ số
        gameManager.getBall().setSpeed(originalSpeed * FAST_SPEED_FACTOR);
        // Kích hoạt hiệu ứng hình ảnh (nếu có)
        targetBall.setBurning(true);
    }

    /**
     * Gỡ bỏ hiệu ứng: Đặt lại tốc độ bóng về gốc và tắt trạng thái "bốc cháy".
     *
     * @param gameManager Tham chiếu đến GameManager.
     */
    @Override public void removeEffect(GameManager gameManager) {
        Ball targetBall = gameManager.getBall();
        gameManager.getBall().resetSpeed(); // Đặt lại tốc độ về gốc
        targetBall.setBurning(false); // Tắt hiệu ứng hình ảnh
    }

    /**
     * Cập nhật logic: Xử lý việc rơi xuống.
     */
    @Override public void update() {
        this.y += fallSpeed;
    }


    /**
     * Vẽ (render) PowerUp lên màn hình, bao gồm cả hiệu ứng vệt mờ (motion trail).
     * <p>
     * <b>Logic vẽ:</b>
     * 1. Vẽ một loạt các "ảnh mờ" ({@code TRAIL_SEGMENTS}) phía trên vật phẩm.
     * 2. Mỗi ảnh mờ có độ trong suốt (alpha) giảm dần (càng xa càng mờ).
     * 3. Cuối cùng, vẽ vật phẩm chính (rõ nét) đè lên trên cùng.
     *
     * @param g Đối tượng Graphics để vẽ.
     * @param sm Trình quản lý co giãn (ScalingManager).
     */
    @Override
    public void render(Graphics g, ScalingManager sm) {

        Graphics2D g2d = (Graphics2D) g;

        // Tính toán tọa độ và kích thước đã co giãn (scale)
        int scaledX = sm.scaleX(this.x);
        int scaledY = sm.scaleY(this.y);
        int scaledWidth = sm.scaleWidth(this.width);
        int scaledHeight = sm.scaleHeight(this.height);

        // --- BẮT ĐẦU HIỆU ỨNG VỆT SÁNG ---

        // 1. Vẽ các vệt sáng (ảnh mờ)
        BufferedImage img = AssetManager.getInstance().getImage(this.imageName);
        Color baseColor = Color.ORANGE;

        for (int i = 1; i <= TRAIL_SEGMENTS; i++) {
            // Tính độ trong suốt giảm dần (vệt càng xa càng mờ)
            float alphaFactor = 1.0f - (i / (float) TRAIL_SEGMENTS); // Giảm từ 1.0 xuống gần 0

            // Tính toán vị trí Y (đã scale) của vệt (phía trên vật phẩm)
            int trailY = sm.scaleY(this.y - (i * TRAIL_SPACING));

            // Thiết lập độ trong suốt (ví dụ: alpha * 0.4f để vệt mờ hơn)
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alphaFactor * 0.4f));

            // Vẽ vệt
            if (img != null) {
                // Vẽ ảnh mờ
                g2d.drawImage(img, scaledX, trailY, scaledWidth, scaledHeight, null);
            } else {
                // Fallback: Vẽ hình chữ nhật màu cam mờ
                g2d.setColor(baseColor);
                g2d.fillRect(scaledX, trailY, scaledWidth, scaledHeight);
            }
        }

        // 2. Phục hồi độ trong suốt về mặc định (RẤT QUAN TRỌNG)
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));

        // --- KẾT THÚC VỆT SÁNG ---

        // 3. Vẽ vật phẩm chính (rõ nét, không mờ)
        if (img != null) {
            g2d.drawImage(img, scaledX, scaledY, scaledWidth, scaledHeight, null);
        } else {
            // Fallback: Vẽ hình chữ nhật chính
            g2d.setColor(Color.ORANGE);
            g2d.fillRect(scaledX, scaledY, scaledWidth, scaledHeight);
        }
    }
}