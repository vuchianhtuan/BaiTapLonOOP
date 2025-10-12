package com.mygame.arkanoid.objects.powerups;
import com.mygame.arkanoid.core.GameManager;

import java.awt.*;


public class FastBallPowerUp extends PowerUp {
    public FastBallPowerUp(int x, int y, int width, int height, String type, int duration) {
        super(x, y, width, height, type, duration);
    }

    public FastBallPowerUp(int x, int y, int width, int height) {
        super();
    }

    @Override public void applyEffect(GameManager gameManager) {}
    @Override public void removeEffect(GameManager gameManager) {}
    @Override public void update() {}
    @Override public void render(Graphics g) {}

}
