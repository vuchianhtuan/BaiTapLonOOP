package com.mygame.arkanoid.util.config;

/**
 * Định nghĩa các tài sản (assets) của game.
 * Mỗi tài sản bao gồm một khóa (key) duy nhất và tên tệp (file name).
 */
public enum AssetDefinition {

    // --- UI, Menu, Backgrounds ---
    MENU_BACKGROUND("menuBackground", "backGroundMenu.png"),
    DEFAULT_BACKGROUND("defaultBackground", "default_game_background.png"),
    HEART("heart", "heart.png"),
    GAMEOVER1("gameover1", "gameover1.png"),
    GAMEOVER2("gameover2", "gameover2.png"),
    GAMEOVER3("gameover3", "gameover3.png"),
    SCORE_BACKGROUND("scoreBackground", "backGroundMenu.png"),
    SETTING_BACKGROUND("settingBackground", "backGroundMenu.png"),
    SELECT_LEVEL_BACKGROUND("selectLevelBackground", "backGroundMenu.png"),
    LEVEL1_PREVIEW("level1_preview", "background_forest.png"),
    LEVEL2_PREVIEW("level2_preview", "background_storm.png"),
    LEVEL3_PREVIEW("level3_preview", "background_boss.png"),
    THUMB("thumb", "ball_blue_large_alt.png"),
    TRACK("track", "paddle.png"),
    BACK_BUTTON("Back", "button_back.png"),
    ARROW_LEFT("arrow_left", "button_left.png"),
    ARROW_RIGHT("arrow_right", "button_right.png"),

    // --- Power-ups ---
    EXPAND_POWERUP("expandPowerUp", "expandPowerUp.png"),
    STICKY_POWERUP("stickyPowerUp", "stickyPowerUp.png"),
    SLOW_BALL_POWERUP("slowBallPowerUp", "slowBallPowerUp.png"),
    FAST_BALL_POWERUP("fastBallPowerUp", "fastBallPowerUp.png"),
    EXTRA_LIFE_POWERUP("extraLifePowerUp", "heart.png"), // Dùng lại ảnh heart
    MULTI_BALL_POWERUP("multiBallPowerUp", "multiBallPowerUp.png"),

    // --- Misc Game Objects ---
    LASER("laser", "laser.png"),
    LASER_SHOOTER("laserShooter", "laser_shooter.png"),
    FIRE_BALL_ANIM("fire_ball_animation", "fire_ball_animation.png"),

    // --- Ball Skins ---
    BALL_SKIN_1("skin_ball_1", "test_ball.png"),
    BALL_SKIN_2("skin_ball_2", "skin_ball_2.png"),
    BALL_SKIN_3("skin_ball_3", "skin_ball_3.png"),
    BALL_SKIN_4("skin_ball_4", "skin_ball_4.png"),
    BALL_SKIN_5("skin_ball_5", "skin_ball_5.png"),
    BALL_SKIN_6("skin_ball_6", "skin_ball_6.png"),

    // --- Paddle Skins ---
    PADDLE_SKIN_1("skin_paddle_1", "skin_paddle_1.png"),
    PADDLE_SKIN_2("skin_paddle_2", "skin_paddle_2.png"),
    PADDLE_SKIN_3("skin_paddle_3", "skin_paddle_3.png"),
    PADDLE_SKIN_4("skin_paddle_4", "skin_paddle_4.png"),

    // --- Themed Bricks (Quan trọng: isThemed = true) ---
    NORMAL_BRICK("normalBrick", "normalBrick.png", true),
    EXPLOSIVE_BRICK("explosiveBrick", "explosiveBrick.png", true),
    STRONG_BRICK("strongBrick", "strongBrick.png", true),
    STRONG_BRICK_1("strongBrick1", "strongBrick1.png", true),
    STRONG_BRICK_2("strongBrick2", "strongBrick2.png", true),
    FOREST_BRICK("forest_brick", "forest_normalBrick.png", true); // Ví dụ gạch "forest" cũng là 1 theme


    private final String key;
    private final String fileName;
    private final boolean isThemed;
    private static final String IMAGE_PATH_PREFIX = "/images/";

    /**
     * Constructor cho tài sản toàn cục (global)
     */
    AssetDefinition(String key, String fileName) {
        this(key, fileName, false);
    }

    /**
     * Constructor cho tài sản theo chủ đề (themed)
     */
    AssetDefinition(String key, String fileName, boolean isThemed) {
        this.key = key;
        this.fileName = fileName;
        this.isThemed = isThemed;
    }

    public String getKey() { return key; }
    public String getFileName() { return fileName; }
    public boolean isThemed() { return isThemed; }

    /**
     * Lấy đường dẫn đầy đủ cho tài sản toàn cục.
     */
    public String getFullPath() {
        return IMAGE_PATH_PREFIX + fileName;
    }
}