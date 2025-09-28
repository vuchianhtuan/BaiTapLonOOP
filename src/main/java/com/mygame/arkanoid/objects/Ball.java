package com.mygame.arkanoid.objects;

public class Ball extends MovableObject {
    private int speed;
    private int directionX, directionY;
    private boolean stuckToPaddle = true;

    public void bounceOff(GameObject other) {}
    public boolean checkCollision(GameObject other) { return false; }
    public boolean isStuckToPaddle() { return stuckToPaddle; }

    @Override public void move() {}
    @Override public void update() {}
    @Override public void render() {}
}

