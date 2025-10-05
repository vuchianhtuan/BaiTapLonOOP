package com.mygame.arkanoid.objects;
import com.mygame.arkanoid.engine.AssetManager;
import com.mygame.arkanoid.engine.InputHandler;

import java.awt.Graphics;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.awt.event.KeyEvent; // Thêm import cho KeyEvent

public class Ball extends MovableObject {
    private int speed = 5;
    //Không cần thiết vì đã có dx dy
    //private int directionX = 1, directionY = -1;
    private boolean stuckToPaddle = true;
    private String imageName;

    public void bounceOff(GameObject other) {}
    public boolean checkCollision(GameObject other) { return false; }
    public boolean isStuckToPaddle() { return stuckToPaddle; }

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
            this.x = paddle.getX() + (paddle.getWidth() - this.width) / 2;
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
}

