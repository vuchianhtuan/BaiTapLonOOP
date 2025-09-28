package com.mygame.arkanoid.effects;

public abstract class Effect {
    protected int duration;
    public abstract void update();
    public abstract void render();
    public boolean isFinished() { return false; }
}