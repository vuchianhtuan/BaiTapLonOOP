package com.mygame.arkanoid.objects;

import com.mygame.arkanoid.systems.ScalingManager;
import java.awt.*;

/**
 * Lớp trừu tượng đại diện cho một đối tượng trong game.
 */
public abstract class GameObject {
    protected int x, y;
    protected int width, height;

    public GameObject(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public abstract void update();
    public abstract void render(Graphics g, ScalingManager sm);

    public int getX() { return x; }
    public int getY() { return y; }
    public int getWidth() { return width; }
    public void setWidth(int width) { this.width = width; }
    public int getHeight() { return height; }
    public void setHeight(int height) { this.height = height; }
    public void setX(int x) { this.x = x; }
    public void setY(int y) { this.y = y; }
    public Rectangle getBounds() {
        return new Rectangle(x, y, width, height);
    }
}