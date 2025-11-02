package com.mygame.arkanoid.objects.bricks;

// Nhà máy tạo gạch dựa trên loại gạch
public final class BrickFactory {
    // Kích thước mặc định cho gạch
    private static final int DEFAULT_WIDTH = 45;
    private static final int DEFAULT_HEIGHT = 20;

    private static final int DEFAULT_STRONG_HP = 3;
    private static final int DEFAULT_MOVING_SPEED = 2;
    private static final int DEFAULT_SHOOTER_FIRE_RATE = 5;

    private BrickFactory() {}

    // Nhanh gọn với kích thước mặc định
    public static Brick create(char type, int x, int y) {
        return create(x, y, DEFAULT_WIDTH, DEFAULT_HEIGHT, type);
    }

    // Tạo theo loại với kích thước tuỳ chỉnh
    public static Brick create(int x, int y, int width, int height, char type) {
        switch (type) {
            case '1': return new NormalBrick(x, y, width, height);
            case '2': return new StrongBrick(x, y, width, height, 2);
            case 'E': return new ExplosiveBrick(x, y, width, height);
            case 'M': return new MovingBrick(x, y, width, height, DEFAULT_MOVING_SPEED, 200);
            case 'L': return new LaserShooterBrick(x, y, width, height, 3);
            case 'B': return new NormalBrick(x, y, width, height);
            case 'T': return new StrongBrick(x, y, width, height, 3); // Máu 3
            case 'C': return new LaserShooterBrick(x, y, width, height, 5);
            case '_': case '0': default: return null;
            //default: throw new IllegalArgumentException("Unsupported BrickType: " + type);
        }
    }
}
