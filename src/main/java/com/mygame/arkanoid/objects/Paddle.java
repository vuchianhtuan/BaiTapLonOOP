package com.mygame.arkanoid.objects;
import com.mygame.arkanoid.objects.powerups.PowerUp;

public class Paddle extends MovableObject {
    private int speed;
    private PowerUp currentPowerUp;
    public void moveLeft() {}
    public void moveRight() {}
    public void applyPowerUp(PowerUp powerUp) {}

    @Override public void move() {}
    @Override public void update() {}
    @Override public void render() {}
}