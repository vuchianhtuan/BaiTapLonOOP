package com.mygame.arkanoid.objects.bricks;

import java.awt.*;

public class MovingBrick extends Brick {
    public MovingBrick(int x, int y, int width, int height, int hitPoints, String type, String imageName) {
        super(x, y, width, height, hitPoints, type, imageName);
    }

    @Override public void takeHit() {}
    @Override public boolean isDestroyed() { return false; }
    @Override public void update() {}
    @Override public void render(Graphics g) {}
}
