package com.mygame.arkanoid.objects.powerups;

// NHÀ MÁY TẠO POWER-UP
public final class PowerUpFactory {
    private PowerUpFactory() {}

    /**
     * Tạo power-up dựa trên loại.
     * @param type
     * @param x
     * @param y
     * @return
     */
    public static PowerUp create(PowerUpType type, int x, int y) {
        switch (type) {
            case EXPAND: return new ExpandPaddlePowerUp(x, y, 63, 30);
            case STICKY: return new StickyPaddlePowerUp(x, y, 59, 30);
            case SLOW_BALL: return new SlowBallPowerUp(x, y, 63, 30);
            case FAST_BALL: return new FastBallPowerUp(x, y, 63, 30);
            case EXTRA_LIFE: return new ExtraLifePowerUp(x, y, 30, 30);
            case MULTI_BALL: return new MultiBallPowerUp(x, y, 61, 30);
            default:
                throw new IllegalArgumentException("Unsupported PowerUpType: " + type);
        }
    }
}
