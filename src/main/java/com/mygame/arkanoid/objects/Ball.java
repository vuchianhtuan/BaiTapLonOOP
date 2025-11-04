package com.mygame.arkanoid.objects;
import com.mygame.arkanoid.core.GameManager;
import com.mygame.arkanoid.engine.AssetManager;
import com.mygame.arkanoid.engine.InputHandler;
import com.mygame.arkanoid.engine.SoundManager;
import com.mygame.arkanoid.systems.ScalingManager;
import com.mygame.arkanoid.config.GameConstants;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.event.KeyEvent;

/**
 * Lớp Ball đại diện cho quả bóng trong game.
 * Quản lý trạng thái (dính, rơi), vật lý (di chuyển, nảy),
 * và các hiệu ứng hình ảnh (xoay, bốc cháy).
 */
public class Ball extends MovableObject {
    // --- Hằng số Vật lý ---
    /** Góc nảy tối thiểu (tính bằng độ) khi bóng chạm gần mép paddle. */
    private static final double MIN_REFLECT_ANGLE_DEG = 30.0;
    /** Góc nảy tối đa (tính bằng độ) khi bóng chạm xa tâm paddle. */
    private static final double MAX_REFLECT_ANGLE_DEG = 60.0;
    /** Vùng chết (dead zone) ở chính giữa paddle (tỷ lệ 0.0-1.0).
     * Nếu bóng chạm vào vùng này, nó sẽ nảy thẳng đứng (0 độ). */
    private static final double CENTER_EPS = 0.02;

    // --- Trạng thái bóng ---
    private double speed = GameConstants.BALL_SPEED;
    private final double originalSpeed; // Tốc độ gốc để reset
    private boolean stuckToPaddle = true;
    private String imageName; // Asset key cho skin
    /** Khoảng cách offset X so với mép trái paddle khi bị dính. */
    private int paddleOffsetX;

    // --- Thuộc tính hiệu ứng lửa (Fast Ball) ---
    private boolean isBurning = false;
    private int fireAnimationFrameIndex = 0;
    private long lastFireFrameTime = 0;
    private static final long FIRE_FRAME_DURATION = 60; // Tốc độ hoạt ảnh (ms)
    private static final int FIRE_FRAME_COUNT = 6;      // Tổng số khung hình lửa
    private static final int FRAMES_PER_ROW = 3;        // Số khung hình trên 1 hàng (spritesheet)
    private static final int NUM_ROWS = 2;              // Số hàng (spritesheet)

    // --- Thuộc tính hiệu ứng xoay (Visual) ---
    private double rotationAngle = 0; // Góc xoay (radian)
    private double rotationSpeed = 0.15; // Tốc độ xoay (radian mỗi frame)

    /**
     * Tính toán góc quay của bóng (hướng di chuyển)
     * dựa trên vận tốc (dx, dy).
     * @return Góc quay (hướng di chuyển) tính bằng radian.
     */
    private double getRotationAngle() {
        return Math.atan2(this.dy, this.dx);
    }

    /**
     * Gắn quả bóng vào thanh đỡ (paddle).
     * Tính toán và lưu lại vị trí offset (khoảng cách) theo chiều X
     * so với mép trái của paddle.
     *
     * @param paddle Thanh đỡ mà bóng sẽ dính vào.
     */
    public void stickToPaddle(Paddle paddle) {
        this.stuckToPaddle = true;
        this.paddleOffsetX = this.x - paddle.getX();
    }

    /**
     * Đặt lại vị trí của bóng về chính giữa thanh đỡ (paddle).
     * Thường dùng khi hồi sinh (respawn) hoặc bắt đầu level.
     *
     * @param paddle Thanh đỡ để căn giữa.
     */
    public void resetBallPosition(Paddle paddle) {
        this.stuckToPaddle = true;
        this.paddleOffsetX = (paddle.getWidth() - this.width) / 2;
        this.x = paddle.getX() + this.paddleOffsetX;
        this.y = paddle.getY() - this.height;
    }

    /**
     * Xử lý logic nảy (vật lý) khi va chạm với một vật thể.
     *
     * @param other Vật thể mà bóng va chạm (Paddle, Brick, ...).
     */
    public void bounceOff(GameObject other) {
        if (other instanceof Paddle) {
            // --- Logic nảy phức tạp khi chạm PADDLE ---
            Paddle paddle = (Paddle) other;

            // 1. Tính toán vị trí va chạm tương đối
            double paddleCenterX = paddle.getX() + paddle.getWidth() / 2.0;
            double ballCenterX = this.x + this.width / 2.0;
            double relativeIntersectX = ballCenterX - paddleCenterX;

            // 2. Chuẩn hóa vị trí tương đối (từ -1.0 đến 1.0)
            double normalizedRelativeIntersectionX = relativeIntersectX / (paddle.getWidth() / 2.0);

            // 3. Áp dụng 'vùng chết' (dead zone) ở giữa
            if (Math.abs(normalizedRelativeIntersectionX) < CENTER_EPS) {
                normalizedRelativeIntersectionX = 0;
            }

            // 4. Ánh xạ (map) vị trí tương đối sang một góc nảy
            // (kết hợp góc tối thiểu và tối đa)
            double reflectAngleDeg = normalizedRelativeIntersectionX * (MAX_REFLECT_ANGLE_DEG - MIN_REFLECT_ANGLE_DEG);
            if (reflectAngleDeg > 0) {
                reflectAngleDeg += MIN_REFLECT_ANGLE_DEG; // Nảy sang phải
            } else if (reflectAngleDeg < 0) {
                reflectAngleDeg -= MIN_REFLECT_ANGLE_DEG; // Nảy sang trái
            }

            // 5. Chuyển góc sang radian và cập nhật dx, dy
            double reflectAngleRad = Math.toRadians(reflectAngleDeg);
            this.dx = Math.sin(reflectAngleRad);
            this.dy = -Math.cos(reflectAngleRad); // Luôn nảy LÊN

            // 6. --- Logic chống kẹt (Anti-Stuck) khi ở mép tường ---
            int gameAreaWidth = ScalingManager.getInstance().GAME_AREA_WIDTH;

            // Nếu bóng đang ở mép TRÁI (do bị kẹp từ hàm move())
            // VÀ logic nảy (ở trên) vô tình tính ra dx < 0 (muốn đi sang trái)
            if (this.x <= 0 && this.dx < 0) {
                // Ép nó nảy sang phải, bất kể logic tính toán góc
                this.dx = Math.abs(this.dx);
            }
            // Tương tự, nếu bóng ở mép PHẢI
            // VÀ logic nảy vô tình tính ra dx > 0 (muốn đi sang phải)
            else if (this.x + this.width >= gameAreaWidth && this.dx > 0) {
                // Ép nó nảy sang trái
                this.dx = -Math.abs(this.dx);
            }
        } else {
            // --- Logic nảy đơn giản khi chạm VẬT THỂ KHÁC (Gạch) ---
            // Kiểm tra va chạm với từng cạnh (trên, dưới, trái, phải)
            // và đảo ngược vận tốc tương ứng.
            if (this.getBounds().intersects(new Rectangle(other.x, other.y, other.width, 1))) {
                // Chạm cạnh trên của vật thể
                this.dy = -Math.abs(this.dy);
            } else if (this.getBounds().intersects(new Rectangle(other.x, other.y + other.height - 1, other.width, 1))) {
                // Chạm cạnh dưới
                this.dy = Math.abs(this.dy);
            } else if (this.getBounds().intersects(new Rectangle(other.x, other.y, 1, other.height))) {
                // Chạm cạnh trái
                this.dx = -Math.abs(this.dx);
            } else if (this.getBounds().intersects(new Rectangle(other.x + other.width - 1, other.y, 1, other.height))) {
                // Chạm cạnh phải
                this.dx = Math.abs(this.dx);
            } else {
                // Trường hợp chạm góc hoặc không xác định (hiếm)
                this.dx = -this.dx;
                this.dy = -this.dy;
            }
        }
    }

    /**
     * Kiểm tra va chạm hình chữ nhật (bounds) với một vật thể khác.
     * Chỉ trả về true nếu bóng *không* đang dính vào paddle (`!isStuckToPaddle()`).
     *
     * @param other Đối tượng để kiểm tra va chạm.
     * @return true nếu có va chạm, false nếu không.
     */
    public boolean checkCollision(GameObject other) {
        return this.getBounds().intersects(other.getBounds()) && !this.isStuckToPaddle();
    }

    /**
     * Cập nhật vị trí `x`, `y` của bóng dựa trên `dx`, `dy` và `speed`.
     * Xử lý va chạm với 3 bức tường (trên, trái, phải).
     * (Va chạm tường dưới được xử lý trong GameManager -> mất mạng).
     */
    @Override public void move() {
        this.x += this.dx * speed;
        this.y += this.dy * speed;

        int gameAreaWidth = ScalingManager.getInstance().GAME_AREA_WIDTH;

        SoundManager sm = GameManager.getInstance().getSoundManager();

        // Xử lý tường trái
        if (this.x <= 0) {
            this.x = 0; // KẸT (clamp) bóng lại ở mép tường
            this.dx = Math.abs(this.dx); // Luôn nảy sang phải
            if (sm != null) {
                sm.playSound(sm.SFX_PADDLE_HIT);
            }
        }
        // Xử lý tường phải
        else if (this.x + this.width >= gameAreaWidth) {
            this.x = gameAreaWidth - this.width; // KẸT bóng lại ở mép tường
            this.dx = -Math.abs(this.dx); // Luôn nảy sang trái
            if (sm != null) {
                sm.playSound(sm.SFX_PADDLE_HIT);
            }
        }

        // Xử lý tường trên
        if (this.y <= 0) {
            this.y = 0; // KẸT bóng lại
            this.dy = Math.abs(this.dy); // Luôn nảy xuống
            if (sm != null) {
                sm.playSound(sm.SFX_PADDLE_HIT);
            }
        }
    }

    /**
     * Cập nhật trạng thái của bóng (mỗi frame).
     * Quản lý logic khi bóng dính vào paddle (và chờ phóng)
     * hoặc khi bóng đang di chuyển tự do.
     *
     * @param inputHandler Trình xử lý đầu vào (để kiểm tra phím SPACE).
     * @param paddle Thanh đỡ (để cập nhật vị trí khi dính).
     */
    public void update(InputHandler inputHandler, Paddle paddle) {
        if(!stuckToPaddle) {
            // Nếu không dính -> di chuyển tự do
            move();
        } else {
            // Nếu đang dính -> Cập nhật vị trí theo paddle
            this.dx = 0;
            this.dy = 0;
            this.x = paddle.getX() + this.paddleOffsetX;
            this.y = paddle.getY() - this.height;

            // Kiểm tra nếu người chơi nhấn SPACE để phóng bóng
            if (inputHandler.isKeyDown(KeyEvent.VK_SPACE)) {
                stuckToPaddle = false;
                // Phóng bóng (mặc định ban đầu)
                this.dx = 1;
                this.dy = -1;
            }
        }

    }

    /**
     * Ghi đè (override) phương thức `update()` rỗng từ `MovableObject`.
     * Logic cập nhật thực tế nằm trong {@link #update(InputHandler, Paddle)}
     * để có thể truy cập paddle và input.
     */
    @Override public void update () {};

    /**
     * Vẽ (render) quả bóng và các hiệu ứng liên quan (xoay, lửa).
     *
     * @param g Đối tượng Graphics để vẽ.
     * @param sm Trình quản lý co giãn (ScalingManager).
     */
    @Override
    public void render(Graphics g, ScalingManager sm) {

        Graphics2D g2d = (Graphics2D) g;
        // Lưu lại phép biến đổi (transform) cũ
        AffineTransform oldTransform = g2d.getTransform();

        // --- 1. Vẽ quả bóng (có xoay) ---
        BufferedImage img = AssetManager.getInstance().getImage(this.imageName);
        if (img != null) {
            int drawX = sm.scaleX(this.x);
            int drawY = sm.scaleY(this.y);
            int drawW = sm.scaleWidth(this.width);
            int drawH = sm.scaleHeight(this.height);

            // Nếu không dính và không cháy, cập nhật góc xoay (hiệu ứng xoay tự nhiên)
            if (!stuckToPaddle && !isBurning) {
                rotationAngle += rotationSpeed;
                if (rotationAngle > Math.PI * 2) rotationAngle -= Math.PI * 2;
            }

            // Dịch (translate) context về tâm bóng
            g2d.translate(drawX + drawW / 2.0, drawY + drawH / 2.0);
            g2d.rotate(rotationAngle); // Xoay context
            // Vẽ ảnh tại (-w/2, -h/2) để căn giữa
            g2d.drawImage(img, -drawW / 2, -drawH / 2, drawW, drawH, null);
            // Khôi phục transform cũ
            g2d.setTransform(oldTransform);
        } else {
            // Fallback: Vẽ hình tròn trắng nếu không có skin
            g2d.setColor(Color.WHITE);
            g2d.fillOval(sm.scaleX(this.x), sm.scaleY(this.y),
                    sm.scaleWidth(this.width), sm.scaleHeight(this.height));
        }

        // --- 2. Vẽ hiệu ứng lửa (nếu isBurning) ---
        if (isBurning) {
            // Cập nhật khung hình (frame) của hoạt ảnh lửa
            if (System.currentTimeMillis() - lastFireFrameTime > FIRE_FRAME_DURATION) {
                fireAnimationFrameIndex = (fireAnimationFrameIndex + 1) % FIRE_FRAME_COUNT;
                lastFireFrameTime = System.currentTimeMillis();
            }

            BufferedImage spriteSheet = AssetManager.getInstance().getImage("fire_ball_animation");

            if (spriteSheet != null) {
                // Tính toán để lấy đúng sub-image (khung hình)
                int frameWidth = spriteSheet.getWidth() / FRAMES_PER_ROW;
                int frameHeight = spriteSheet.getHeight() / NUM_ROWS;
                int frameRow = fireAnimationFrameIndex / FRAMES_PER_ROW;
                int frameCol = fireAnimationFrameIndex % FRAMES_PER_ROW;
                int srcX = frameCol * frameWidth;
                int srcY = frameRow * frameHeight;

                BufferedImage fireFrame = spriteSheet.getSubimage(srcX, srcY, frameWidth, frameHeight);

                int fireW = sm.scaleWidth(frameWidth) / 5; // Scale kích thước lửa
                int fireH = sm.scaleHeight(frameHeight) / 5;

                // --- Logic phức tạp: Vẽ lửa phía sau bóng ---
                double ballCenterX = sm.scaleX((int) (this.x + this.width / 2.0));
                double ballCenterY = sm.scaleY((int) (this.y + this.height / 2.0));
                double radius = sm.scaleWidth(this.width) / 2.0;

                // Lấy hướng bay của bóng (getRotationAngle())
                double angle = getRotationAngle(); // hướng bay, radian

                // Tính toán vị trí tâm của lửa (phía sau bóng)
                double distance = (fireW / 2.0) - radius;
                // (angle + Math.PI) là góc ngược 180 độ so với hướng bay
                double fireCenterX = ballCenterX + distance * Math.cos(angle + Math.PI);
                double fireCenterY = ballCenterY + distance * Math.sin(angle + Math.PI);

                // Dịch (translate) context đến tâm lửa
                g2d.translate(fireCenterX, fireCenterY);
                // Xoay context của lửa để khớp với hướng bay
                g2d.rotate(angle + Math.PI);

                // Vẽ frame lửa (căn giữa tại (0,0) của context mới)
                int drawX = -fireW / 2;
                int drawY = -fireH / 2;
                g2d.drawImage(fireFrame, drawX, drawY, fireW, fireH, null);

                // Khôi phục transform (rất quan trọng)
                g2d.setTransform(oldTransform);
            } else {
                g2d.setTransform(oldTransform);
            }
        }
    }

    /**
     * Khởi tạo một quả bóng mới.
     *
     * @param x Vị trí X (logic) ban đầu.
     * @param y Vị trí Y (logic) ban đầu.
     * @param width Chiều rộng (logic).
     * @param height Chiều cao (logic).
     * @param skinKey Key (mã) của skin (ví dụ: "skin_ball_1").
     */
    public Ball(int x, int y, int width, int height, String skinKey) {
        super(x, y, width, height);
        dx = 1; // Hướng ban đầu
        dy = -1;
        this.imageName = skinKey;
        this.originalSpeed = this.speed; // Lưu lại tốc độ gốc
    }

    public void setSpeed(double newSpeed) {
        this.speed = newSpeed;
    }
    public void resetSpeed() {
        this.speed = this.originalSpeed;
    }
    public double getSpeed() {
        return speed;
    }
    public void setStuckToPaddle(boolean stuckToPaddle) {
        this.stuckToPaddle = stuckToPaddle;
    }
    public String getImageName() {
        return imageName;
    }
    public void setImageName(String imageName) {
        this.imageName = imageName;
    }
    @Override
    public int getX() {
        return this.x;
    }
    @Override
    public int getY() {
        return this.y;
    }
    public void setDx(int dx) {
        this.dx = dx;
    }
    public void setDy(int dy) {
        this.dy = dy;
    }
    public double getOriginalSpeed() {
        return originalSpeed;
    }
    public void setBurning(boolean burning) {
        this.isBurning = burning;
        if (!burning) {
            this.fireAnimationFrameIndex = 0;
        }
    }
    public boolean isStuckToPaddle() { return stuckToPaddle; }
}