package com.mygame.arkanoid.objects.bricks;

import com.mygame.arkanoid.objects.GameObject;

public abstract class Brick extends GameObject {
    protected int hitPoints;
    protected String type;

    public abstract void takeHit();
    public abstract boolean isDestroyed();
}