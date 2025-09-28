package com.mygame.arkanoid.objects.powerups;
import com.mygame.arkanoid.objects.GameObject;
import com.mygame.arkanoid.objects.Paddle;

public abstract class PowerUp extends GameObject {
    protected String type;
    protected int duration;

    public abstract void applyEffect(Paddle paddle);
    public abstract void removeEffect(Paddle paddle);
}

