package com.mygame.arkanoid.config;

/**
 * Chứa các hằng số (constants) cấu hình của game.
 * Lớp này không thể được tạo đối tượng (instantiated).
 */
public final class GameConstants {

    /** Constructor private để ngăn chặn việc tạo đối tượng. */
    private GameConstants() {}

    // --- Thông số Gameplay ---
    public static final int PLAYER_STARTING_LIVES = 4; // Số mạng ban đầu

    /** Thời gian chờ (tính bằng frame) khi thắng/thua. (VD: 150f / 60fps ≈ 2.5s) */
    public static final int GAME_OVER_TIMER_FRAMES = 150;

    /** Thời gian đếm ngược (tính bằng frame) trước khi bắt đầu. (VD: 300f / 60fps ≈ 5s) */
    public static final int GAME_START_TIMER_FRAMES = 300;

    // --- Thông số Vật thể (Entity) ---
    public static final int BALL_SIZE = 18; // Kích thước bóng (pixel)
    public static final double BALL_SPEED = 7; // Tốc độ bóng (pixel/frame)

    public static final int PADDLE_WIDTH = 120; // Chiều rộng paddle (pixel)
    public static final int PADDLE_HEIGHT = 30; // Chiều cao paddle (pixel)
    public static final int PADDLE_SPEED = 15; // Tốc độ paddle (pixel/frame)

}