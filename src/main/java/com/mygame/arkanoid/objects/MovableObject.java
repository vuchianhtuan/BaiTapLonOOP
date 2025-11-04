package com.mygame.arkanoid.objects;

/**
 * Lớp trừu tượng đại diện cho một đối tượng có thể di chuyển trong game.
 */
public abstract class MovableObject extends GameObject {
    protected double dx, dy;
    public abstract void move();

    public MovableObject(int x, int y, int width, int height) {
        super(x, y, width, height);
        this.dx = 0;
        this.dy = 0;
    }

    public double getDx() {
        return dx;
    }

    public void setDx(int dx) {
        this.dx = dx;
    }

    public double getDy() {
        return dy;
    }

    public void setDy(int dy) {
        this.dy = dy;
    }
}
