package com.mygame.arkanoid.objects.powerups;

public final class PowerUpFactory {
    private static final int SIZE = 30;

    private PowerUpFactory() {}

    public static PowerUp create(PowerUpType type, int x, int y) {
        switch (type) {
            case EXPAND:      return new ExpandPaddlePowerUp(x, y, SIZE, SIZE);
            case STICKY:      return new StickyPaddlePowerUp(x, y, SIZE, SIZE);
            case SLOW_BALL:   return new SlowBallPowerUp(x, y, SIZE, SIZE);
            case FAST_BALL:   return new FastBallPowerUp(x, y, SIZE, SIZE);
            case EXTRA_LIFE:  return new ExtraLifePowerUp(x, y, SIZE, SIZE);
            case MULTI_BALL:  return new MultiBallPowerUp(x, y, SIZE, SIZE);
            default:
                throw new IllegalArgumentException("Unsupported PowerUpType: " + type);
        }
    }
}
