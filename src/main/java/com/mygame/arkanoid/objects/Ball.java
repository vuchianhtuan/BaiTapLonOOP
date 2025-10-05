package com.mygame.arkanoid.objects;
import com.mygame.arkanoid.engine.AssetManager;

import java.awt.Graphics;
import java.awt.Color;
import java.awt.image.BufferedImage;

public class Ball extends MovableObject {
    private int speed;
    private int directionX, directionY;
    private boolean stuckToPaddle = true;
    private String imageName;

    public void bounceOff(GameObject other) {}
    public boolean checkCollision(GameObject other) { return false; }
    public boolean isStuckToPaddle() { return stuckToPaddle; }

    @Override public void move() {
        this.x += this.dx;
        this.y += this.dy;
    }
    @Override public void update() {}
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

