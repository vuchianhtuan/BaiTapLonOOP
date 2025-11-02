package com.mygame.arkanoid.objects;

// LỚP TRỪU TƯỢNG CHO CÁC ĐỐI TƯỢNG CÓ THỂ DI CHUYỂN TRONG GAME
public abstract class MovableObject extends GameObject {
    protected double dx, dy;
    public abstract void move();

    public MovableObject(int x, int y, int width, int height) {
        super(x, y, width, height);
        this.dx = 0; // Vận tốc ban đầu bằng 0
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
