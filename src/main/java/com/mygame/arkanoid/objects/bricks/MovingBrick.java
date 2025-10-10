package com.mygame.arkanoid.objects.bricks;

import com.mygame.arkanoid.engine.AssetManager;

import java.awt.*;
import java.awt.image.BufferedImage;

public class MovingBrick extends Brick {
    private int speedX;
    private int originalX;
    private int moveRange;

    public MovingBrick(int x, int y, int width, int height, int speedX, int moveRange) {
        super(x, y, width, height, 2, "moving", "normalBrick");
        this.speedX = speedX;
        this.originalX = x;
        this.moveRange = moveRange;
    }

    //@Override public void takeHit() {}
    //@Override public boolean isDestroyed() { return false;

    // cập nhật vị trí sau mỗi loop
    @Override public void update() {
        this.x += speedX;
        // di chuyen trong 1 khoảng.
        if (x > originalX + moveRange || x < originalX - moveRange) {
            speedX = -speedX;
        }
    }

    @Override public void render(Graphics g) {
        BufferedImage img = AssetManager.getInstance().getImage(this.imageName);
        if (img != null) {
            g.drawImage(img, this.x, this.y, this.width, this.height, null);
        }
    }
}
