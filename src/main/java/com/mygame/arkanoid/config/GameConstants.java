package com.mygame.arkanoid.config;

/**
 * Chứa các hằng số (constants) cấu hình của game.
 */
public final class GameConstants {

    private GameConstants() {}
    // --- Thông số Gameplay ---
    public static final int PLAYER_STARTING_LIVES = 4; // Số mạng ban đầu của người chơi
    public static final int GAME_OVER_TIMER_FRAMES = 150; // Thời gian hiển thị màn hình Game Over/Win
    public static final int GAME_START_TIMER_FRAMES = 300;  // Thời gian chờ trước khi bắt đầu game

    // --- Thông số Vật thể (Entity) ---
    public static final int BALL_SIZE = 18; // Kích thước bóng
    public static final double BALL_SPEED = 7; // Tốc độ bóng

    public static final int PADDLE_WIDTH = 120; // Kích thước paddle
    public static final int PADDLE_HEIGHT = 30; // Kích thước paddle
    public static final int PADDLE_SPEED = 15; // Tốc độ di chuyển paddle

}
