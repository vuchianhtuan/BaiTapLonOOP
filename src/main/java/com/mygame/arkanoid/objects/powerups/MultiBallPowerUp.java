package com.mygame.arkanoid.objects.powerups;

import com.mygame.arkanoid.objects.Paddle;

import java.awt.*;

public class MultiBallPowerUp extends PowerUp {
    public MultiBallPowerUp(int x, int y, int width, int height, String type, int duration) {
        super(x, y, width, height, type, duration);
    }

    @Override public void applyEffect(Paddle paddle) {}
    @Override public void removeEffect(Paddle paddle) {}
    @Override public void update() {}
    @Override public void render(Graphics g) {}
}