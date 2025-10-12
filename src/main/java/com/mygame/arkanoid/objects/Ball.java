package com.mygame.arkanoid.objects;
import com.mygame.arkanoid.engine.AssetManager;
import com.mygame.arkanoid.engine.InputHandler;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.event.KeyEvent;

public class Ball extends MovableObject {
    private double speed = 5;
    private final double originalSpeed;
    private boolean stuckToPaddle = true;
    private String imageName;
    private int paddleOffsetX;

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
        if (this.x <= 0 || this.x + this.width >= 800) {
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
    @Override public void render(Graphics g) {
        BufferedImage img = AssetManager.getInstance().getImage(this.imageName);
        if (img != null) {
            // Nếu có ảnh, vẽ ảnh
            g.drawImage(img, this.x, this.y, this.width, this.height, null);
        } else {
            // Nếu không tìm thấy ảnh, quay lại vẽ hình tròn màu trắng (phương án dự phòng)
            g.setColor(Color.WHITE);
            g.fillOval(this.x, this.y, this.width, this.height);
        }
    }

    public Ball(int x, int y, int width, int height) {
        super(x, y, width, height);
        dx = 1;
        dy = -1;
        this.imageName = "ball";
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

