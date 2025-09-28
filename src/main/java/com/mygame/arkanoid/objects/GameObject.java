package com.mygame.arkanoid.objects;

public abstract class GameObject {
    protected int x, y;
    protected int width, height;

    public abstract void update();
    public abstract void render();
}