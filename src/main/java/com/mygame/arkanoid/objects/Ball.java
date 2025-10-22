package com.mygame.arkanoid.objects;
import com.mygame.arkanoid.core.GamePanel;
import com.mygame.arkanoid.engine.AssetManager;
import com.mygame.arkanoid.engine.InputHandler;
import com.mygame.arkanoid.systems.ScalingManager;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.event.KeyEvent;

public class Ball extends MovableObject {
    private double speed = 7;
    private final double originalSpeed;
    private boolean stuckToPaddle = true;
    private String imageName;
    private int paddleOffsetX;

    // Thuộc tính lửa.
    private boolean isBurning = false;
    private int fireAnimationFrameIndex = 0;
    private long lastFireFrameTime = 0;
    private static final long FIRE_FRAME_DURATION = 60; // Tốc độ hoạt ảnh (ms)
    private static final int FIRE_FRAME_COUNT = 6;      // Số khung hình lửa
    private static final int FRAMES_PER_ROW = 3;
    private static final int NUM_ROWS = 2;             // <--- Hằng số mới

    private double rotationAngle = 0;
    private double rotationSpeed = 0.15; // radian mỗi frame (có thể chỉnh để xoay nhanh/chậm hơn)

    // Phương thức tính toán góc quay của bóng
    private double getRotationAngle() {
        return Math.atan2(this.dy, this.dx);
    }

    public void setBurning(boolean burning) {
        this.isBurning = burning;
        if (!burning) {
            this.fireAnimationFrameIndex = 0;
        }
    }

    public void stickToPaddle(Paddle paddle) {
        this.stuckToPaddle = true;
        this.paddleOffsetX = this.x - paddle.getX();
    }

    public void resetBallPosition(Paddle paddle) {
        this.stuckToPaddle = true;
        this.paddleOffsetX = (paddle.getWidth() - this.width) / 2;
        this.x = paddle.getX() + this.paddleOffsetX;
        this.y = paddle.getY() - this.height;
    }

    public void bounceOff(GameObject other) {
        if (this.getBounds().intersects(new Rectangle(other.x, other.y, other.width, 1))) {
            // Chạm cạnh trên
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
            // Trường hợp chạm góc hoặc không xác định
            this.dx = -this.dx;
            this.dy = -this.dy;
        }
    }

    public boolean isStuckToPaddle() { return stuckToPaddle; }

    public boolean checkCollision(GameObject other) {
        return this.getBounds().intersects(other.getBounds()) && !this.isStuckToPaddle();
    }

    @Override public void move() {
        this.x += this.dx * speed;
        this.y += this.dy * speed;
        int gameAreaWidth = ScalingManager.getInstance().GAME_AREA_WIDTH;
        if (this.x <= 0 || this.x + this.width >= gameAreaWidth) {
            dx = - dx; // Đổi hướng khi chạm tường trái hoặc phải
        }
        if (this.y <= 0) {
            dy = - dy; // Đổi hướng khi chạm tường trên
        }
    }
    // thêm phương thức update cho Ball với InputHandler, Paddle
    public void update(InputHandler inputHandler, Paddle paddle) {
        if(!stuckToPaddle) {
            move();
        } else {
            this.dx = 0;
            this.dy = 0;
            this.x = paddle.getX() + this.paddleOffsetX;
            this.y = paddle.getY() - this.height;
            if (inputHandler.isKeyDown(KeyEvent.VK_SPACE)) {
                stuckToPaddle = false;
                this.dx = 1;
                this.dy = -1;
            }
        }

    }
    @Override public void update () {};

    @Override
    public void render(Graphics g, ScalingManager sm) {

        Graphics2D g2d = (Graphics2D) g;
        AffineTransform oldTransform = g2d.getTransform();

        BufferedImage img = AssetManager.getInstance().getImage(this.imageName);
        if (img != null) {
            int drawX = sm.scaleX(this.x);
            int drawY = sm.scaleY(this.y);
            int drawW = sm.scaleWidth(this.width);
            int drawH = sm.scaleHeight(this.height);

            if (!stuckToPaddle && !isBurning) {
                rotationAngle += rotationSpeed;
                if (rotationAngle > Math.PI * 2) rotationAngle -= Math.PI * 2;
            }

            // Xoay quanh tâm quả bóng
            g2d.translate(drawX + drawW / 2.0, drawY + drawH / 2.0);
            g2d.rotate(rotationAngle);
            g2d.drawImage(img, -drawW / 2, -drawH / 2, drawW, drawH, null);
            g2d.setTransform(oldTransform);
        } else {
            g2d.setColor(Color.WHITE);
            g2d.fillOval(sm.scaleX(this.x), sm.scaleY(this.y),
                    sm.scaleWidth(this.width), sm.scaleHeight(this.height));
        }

        // 2. Vẽ hiệu ứng lửa nếu đang cháy
        if (isBurning) {
            if (System.currentTimeMillis() - lastFireFrameTime > FIRE_FRAME_DURATION) {
                fireAnimationFrameIndex = (fireAnimationFrameIndex + 1) % FIRE_FRAME_COUNT;
                lastFireFrameTime = System.currentTimeMillis();
            }

            BufferedImage spriteSheet = AssetManager.getInstance().getImage("fire_ball_animation");

            if (spriteSheet != null) {
                int frameWidth = spriteSheet.getWidth() / FRAMES_PER_ROW;
                int frameHeight = spriteSheet.getHeight() / NUM_ROWS;

                int frameRow = fireAnimationFrameIndex / FRAMES_PER_ROW;
                int frameCol = fireAnimationFrameIndex % FRAMES_PER_ROW;

                int srcX = frameCol * frameWidth;
                int srcY = frameRow * frameHeight;

                BufferedImage fireFrame = spriteSheet.getSubimage(srcX, srcY, frameWidth, frameHeight);

                int fireW = sm.scaleWidth(frameWidth) / 5;
                int fireH = sm.scaleHeight(frameHeight) / 5;

                double ballCenterX = sm.scaleX((int) (this.x + this.width / 2.0));
                double ballCenterY = sm.scaleY((int) (this.y + this.height / 2.0));
                double radius = sm.scaleWidth(this.width) / 2.0;

                double angle = getRotationAngle(); // hướng bay, radian

                double distance = (fireW / 2.0) - radius;

                double fireCenterX = ballCenterX + distance * Math.cos(angle + Math.PI);
                double fireCenterY = ballCenterY + distance * Math.sin(angle + Math.PI);

                g2d.translate(fireCenterX, fireCenterY);
                g2d.rotate(angle + Math.PI);

                int drawX = -fireW / 2;
                int drawY = -fireH / 2;
                g2d.drawImage(fireFrame, drawX, drawY, fireW, fireH, null);

                g2d.setTransform(oldTransform);
            } else {
                g2d.setTransform(oldTransform);
            }
        }
    }

    public Ball(int x, int y, int width, int height) {
        super(x, y, width, height);
        dx = 1;
        dy = -1;
        this.imageName = "test_ball";
        this.originalSpeed = this.speed;
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
}

