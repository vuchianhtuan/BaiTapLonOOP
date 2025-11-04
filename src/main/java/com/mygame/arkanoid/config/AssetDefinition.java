package com.mygame.arkanoid.config;

/**
 * Định nghĩa tập trung tất cả các tài sản hình ảnh (assets) của game.
 * <p>
 * Enum này hoạt động như một bảng tra cứu (registry), cung cấp một cách
 * an toàn về kiểu (type-safe) để tham chiếu đến các tài sản. Mỗi hằng số enum
 * ánh xạ một tên logic (ví dụ: {@code MENU_BACKGROUND}) tới một **khóa (key)**
 * duy nhất (dùng cho asset manager) và một **tên tệp (fileName)**.
 * <p>
 * Tài sản được phân loại thành hai loại:
 * <ul>
 * <li><b>Toàn cục (Global):</b> Tài sản dùng chung, không thay đổi (ví dụ: UI, power-ups).
 * Chúng được tải một lần.</li>
 * <li><b>Theo chủ đề (Themed):</b> Tài sản có thể thay đổi tùy theo chủ đề
 * của màn chơi (ví dụ: các loại gạch). Chúng có thể cần được tải
 * hoặc ánh xạ lại khi thay đổi chủ đề.</li>
 * </ul>
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


    /** Khóa (key) định danh duy nhất cho tài sản, thường dùng trong AssetManager. */
    private final String key;
    /** Tên tệp thực tế của hình ảnh (bao gồm cả phần mở rộng). */
    private final String fileName;
    /** Cờ đánh dấu đây có phải là tài sản theo chủ đề (thay đổi theo level) hay không. */
    private final boolean isThemed;
    /** Tiền tố đường dẫn cố định cho tất cả các tài sản hình ảnh toàn cục. */
    private static final String IMAGE_PATH_PREFIX = "/images/";

    /**
     * Constructor cho tài sản **toàn cục** (global).
     * Mặc định cờ {@code isThemed} là {@code false}.
     *
     * @param key Khóa định danh của tài sản (ví dụ: "menuBackground").
     * @param fileName Tên tệp của tài sản (ví dụ: "backGroundMenu.png").
     */
    AssetDefinition(String key, String fileName) {
        this(key, fileName, false);
    }

    /**
     * Constructor đầy đủ để định nghĩa một tài sản.
     *
     * @param key Khóa định danh của tài sản.
     * @param fileName Tên tệp của tài sản.
     * @param isThemed {@code true} nếu tài sản này thuộc về một chủ đề (theme),
     * {@code false} nếu là tài sản toàn cục.
     */
    AssetDefinition(String key, String fileName, boolean isThemed) {
        this.key = key;
        this.fileName = fileName;
        this.isThemed = isThemed;
    }

    /**
     * Lấy khóa (key) định danh của tài sản.
     * @return Chuỗi key duy nhất.
     */
    public String getKey() { return key; }

    /**
     * Lấy tên tệp (file name) của tài sản.
     * @return Tên tệp, ví dụ: "heart.png".
     */
    public String getFileName() { return fileName; }

    /**
     * Kiểm tra xem tài sản này có phải là tài sản theo chủ đề hay không.
     * <p>
     * Tài sản theo chủ đề (ví dụ: gạch) có thể có các phiên bản khác nhau
     * tùy thuộc vào màn chơi.
     *
     * @return {@code true} nếu là tài sản theo chủ đề, {@code false} nếu là toàn cục.
     */
    public boolean isThemed() { return isThemed; }

    /**
     * Lấy đường dẫn đầy đủ, cố định đến tệp tài sản.
     * <p>
     * Phương thức này <b>luôn</b> trả về đường dẫn dựa trên tiền tố toàn cục
     * ({@code /images/}). Nó không tự động xử lý logic cho các đường dẫn
     * theo chủ đề (ví dụ: "/images/forest/normalBrick.png").
     * <p>
     * Việc xử lý đường dẫn cho tài sản {@code isThemed = true} phải được
     * thực hiện bởi một logic riêng (ví dụ: trong AssetLoader).
     *
     * @return Đường dẫn đầy đủ, ví dụ: "/images/heart.png".
     */
    public String getFullPath() {
        return IMAGE_PATH_PREFIX + fileName;
    }
}