package com.mygame.arkanoid.config;

public final class GameConstants {

    private GameConstants() {}

    // --- Kích thước Game & Cửa sổ ---
    public static final int NATIVE_WIDTH = 1120;
    //public static final int NATIVE_HEIGHT = 720;
    //public static final int GAME_AREA_WIDTH = 960;
    public static final int SIDEBAR_WIDTH = 160; // (1120 - 960)

    // --- Thông số Gameplay ---
    public static final int PLAYER_STARTING_LIVES = 4;
    public static final int GAME_END_TIMER_FRAMES = 480; // Thời gian hiển thị màn hình Game Over/Win
    public static final float EXPLOSION_PROPAGATION_FRAMES = 30.0f; // Tốc độ nổ lan
    public static final int TRANSITION_BRICKS_PER_FRAME = 2; // Gạch rơi khi chuyển màn

    // --- Thông số Vật thể (Entity) ---
    public static final int DEFAULT_PADDLE_WIDTH = 120;
    public static final int PADDLE_SPAWN_Y_OFFSET = 80; // Cách đáy màn hình
    public static final int DEFAULT_BALL_SIZE = 18;
    public static final float BOSS_INITIAL_SPEED = 2.0f;

    // --- Thông số UI (Sidebar) ---
    public static final int SIDEBAR_BUTTON_WIDTH = 120;
    public static final int SIDEBAR_BUTTON_HEIGHT = 40;
    public static final int SIDEBAR_MENU_BUTTON_Y = 590;
    public static final int SIDEBAR_PAUSE_BUTTON_Y = 650;

}
