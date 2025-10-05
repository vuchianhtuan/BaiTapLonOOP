package com.mygame.arkanoid.objects;

public abstract class MovableObject extends GameObject {
    protected int dx, dy;
    public abstract void move();

    public MovableObject(int x, int y, int width, int height) {
        super(x, y, width, height);
        this.dx = 0; // Vận tốc ban đầu bằng 0
        this.dy = 0;
    }

    public int getDx() {
        return dx;
    }

    public void setDx(int dx) {
        this.dx = dx;
    }

    public int getDy() {
        return dy;
    }

    public void setDy(int dy) {
        this.dy = dy;
    }
}
