package com.mygame.arkanoid.core;

import com.mygame.arkanoid.engine.AssetManager;
import com.mygame.arkanoid.objects.*;
import com.mygame.arkanoid.objects.bricks.Brick;
import com.mygame.arkanoid.systems.*;
import com.mygame.arkanoid.systems.*;
import com.mygame.arkanoid.objects.Paddle;
import com.mygame.arkanoid.objects.powerups.PowerUp;
import com.mygame.arkanoid.engine.InputHandler;
import com.mygame.arkanoid.engine.Renderer;
import com.mygame.arkanoid.engine.SoundManager;

import com.mygame.arkanoid.objects.bricks.*;
import com.mygame.arkanoid.objects.powerups.*;

import java.awt.*;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;

public class GameManager {
    private Paddle paddle;
    private Ball ball;
    private int ballSize;
    private List<Ball> balls = new ArrayList<>(); // Danh sách các quả bóng (nếu có Multi-Ball)
    private List<Brick> stagingBricks;
    private List<Brick> bricks;
    private List<PowerUp> powerUps;
    private List<PowerUp> activePowerUps;
    private List<Shard> activeShards; // <--- THÊM DÒNG NÀY
    private List<HeartUI> hearts;
    private Track track;
    private Thumb thumb;
    private BackButton backButton;
    private boolean canContinue = false;

    private int score = 0;
    private int lives;
    private String gameState;
    private int gameOverTimer;
    private List<Laser> lasers;
    private Boss boss;
    private List<LaserShooterBrick> laserShooters;

    private ScoreManager scoreManager;
    private LevelManager levelManager;
    private Renderer renderer;
    private LevelTransition levelTransition;
    private SoundManager soundManager;
    private InputHandler inputHandler;
    private MenuManager menuManager;
    private SetupVolume setupVolume;
    private SelectLevel selectLevel;
    private String currentTheme = "";
    private Image currentBackground = null;

    private long playtimeMillis = 0;
    private long lastUpdateTime = 0;
    private long currentLevelPlaytimeMillis = 0; // Thời gian của màn hiện tại
    private int currentLevelScore = 0;
    private boolean pauseCooldown = false;
    private Rectangle pauseButtonRect;
    private Rectangle resumeButtonRect;
    private Rectangle menuButtonRect;

    public static final String GAMESTATE_PAUSED = "PAUSED";

    public static final String GAMESTATE_TRANSITION_OUT = "TRANSITION_OUT";
    public static final String GAMESTATE_TRANSITION_IN = "TRANSITION_IN";

    int screenHeight = com.mygame.arkanoid.systems.ScalingManager.getInstance().NATIVE_HEIGHT;

    public GameManager() {
        inputHandler = new InputHandler();
        bricks = new ArrayList<>();
        stagingBricks = new ArrayList<>();
        laserShooters = new ArrayList<>();
        powerUps = new ArrayList<>();
        activePowerUps = new ArrayList<>();
        track = new Track(400, 130, 300, 20);
        thumb = new Thumb(400, 120, 40, 40);
        backButton = new BackButton(10, 10, 40, 40);
        activeShards = new ArrayList<>(); // <--- KHỞI TẠO DANH SÁCH
        this.soundManager = new SoundManager();
        this.hearts = new ArrayList<>();

        int gameAreaWidth = ScalingManager.getInstance().GAME_AREA_WIDTH; // 960
        int sidebarWidth = ScalingManager.getInstance().NATIVE_WIDTH - gameAreaWidth; // 160
        int buttonLogicX = gameAreaWidth + (sidebarWidth - 120) / 2; // (960 + (160-120)/2) = 980
        int buttonWidth = 120;
        int buttonHeight = 40;
        int buttonLogicY_Pause = 650; // Vị trí nút Pause/Resume
        int buttonLogicY_Menu = 590;  // Vị trí nút Menu (cao hơn)

        pauseButtonRect = new Rectangle(buttonLogicX, buttonLogicY_Pause, buttonWidth, buttonHeight);
        resumeButtonRect = new Rectangle(buttonLogicX, buttonLogicY_Pause, buttonWidth, buttonHeight); // <-- Đổi tên
        menuButtonRect = new Rectangle(buttonLogicX, buttonLogicY_Menu, buttonWidth, buttonHeight); // <-- THÊM MỚI

        loadAssets();

        levelManager = new LevelManager();
        this.lasers = new ArrayList<>();
        levelManager.loadLevels();
        this.levelTransition = new LevelTransition(this);
        menuManager = new MenuManager(this, inputHandler);
        scoreManager = new ScoreManager(this, inputHandler);
        setupVolume = new SetupVolume(inputHandler, this, soundManager, track, thumb);
        selectLevel = new SelectLevel(inputHandler, this, levelManager);

        // Đặt trạng thái ban đầu của game là MENU
        this.gameState = "MENU";
        canContinue = false;
        soundManager.playBackgroundMusic("Menu.wav");
    }

    public void loadAssets() {
        AssetManager am = AssetManager.getInstance();

        // Menu, UI, Game Over
        am.loadImage("menuBackground", "/images/backGroundMenu.png");
        am.loadImage("defaultBackground", "/images/default_game_background.png");
        am.loadImage("heart", "/images/heart.png");
        am.loadImage("gameover1", "/images/gameover1.png");
        am.loadImage("gameover2", "/images/gameover2.png");
        am.loadImage("gameover3", "/images/gameover3.png");
        am.loadImage("scoreBackground", "/images/backGroundMenu.png");
        am.loadImage("setupVolumeBackground", "/images/backGroundMenu.png");
        am.loadImage("selectLevelBackground", "/images/backGroundMenu.png");

        am.loadImage("level1_preview", "/images/level1_preview.png");
        am.loadImage("level2_preview", "/images/level2_preview.png");
        am.loadImage("level3_preview", "/images/level3_preview.png");

        // Power-ups (thường là chung)
        am.loadImage("expandPowerUp", "/images/expandPowerUp.png");
        am.loadImage("stickyPowerUp", "/images/stickyPowerUp.png");
        am.loadImage("slowBallPowerUp", "/images/slowBallPowerUp.png");
        am.loadImage("fastBallPowerUp", "/images/fastBallPowerUp.png");
        am.loadImage("extraLifePowerUp", "/images/heart.png");
        am.loadImage("multiBallPowerUp", "/images/multiBallPowerUp.png");
        am.loadImage("thumb", "/images/ball_blue_large_alt.png");
        am.loadImage("track", "/images/paddle.png");
        am.loadImage("Back", "/images/button_back.png");

        // Laser
        am.loadImage("laser", "/images/laser.png");
        am.loadImage("laserShooter", "/images/laser_shooter.png");
        am.loadImage("fire_ball_animation", "/images/fire_ball_animation.png");
        am.loadImage("test_ball", "/images/test_ball.png");

        //Explosive efect
        for (int i = 1; i <= 8; i++) {
            String imageName = "explosion_render" + i;
            String imagePath = "/images/" + imageName + ".png"; // Giả sử file có đuôi .png
            am.loadImage(imageName, imagePath);
        }
    }

    private void loadThemeAssets(String prefix) {
        if (prefix.equals(currentTheme)) {
            return;
        }
        currentTheme = prefix;
        AssetManager am = AssetManager.getInstance();

        // Tải các ảnh với tiền tố (ví dụ "ice_ball.png" hoặc "ball.png")
        am.loadImage("ball", "/images/" + prefix + "ball.png");
        am.loadImage("paddle", "/images/" + prefix + "paddle.png");
        am.loadImage("normalBrick", "/images/" + prefix + "normalBrick.png");
        am.loadImage("explosiveBrick", "/images/" + prefix + "explosiveBrick.png");
        am.loadImage("strongBrick", "/images/" + prefix + "strongBrick.png");
        am.loadImage("strongBrick1", "/images/" + prefix + "strongBrick1.png");
        am.loadImage("strongBrick2", "/images/" + prefix + "strongBrick2.png");
    }

    public void startGame() {
        this.playtimeMillis = 0;
        this.lastUpdateTime = System.nanoTime();
        this.lives = 3;
        this.score = 0;
        this.currentLevelScore = 0; // Reset điểm màn
        this.currentLevelPlaytimeMillis = 0;
        setupLevelObjects();
        canContinue = false;
        levelManager.reset(); // Đưa level manager về màn 1
        loadNextLevel();
    }

    public void startGameAtLevel(int levelIndex) {
        this.playtimeMillis = 0; // Reset tổng thời gian session
        this.lastUpdateTime = System.nanoTime();
        this.lives = 3;
        this.score = 0; // Reset tổng điểm session
        this.currentLevelScore = 0; // Reset điểm cho màn mới
        this.currentLevelPlaytimeMillis = 0; // Reset thời gian cho màn mới
        canContinue = false;

        // Sử dụng phương thức mới của LevelManager để tải trực tiếp
        if (levelManager.loadSpecificLevel(levelIndex)) {
            setupLevelObjects(); // Setup paddle, ball...
            levelTransition.startInstantFade(); // Chuyển cảnh ngay lập tức
            setGameState("TRANSITION");
        } else {
            // Xử lý trường hợp không tải được level (vd: index sai)
            System.err.println("Lỗi: Không thể tải level tại index " + levelIndex);
            setGameState("MENU"); // Quay về Menu
        }
    }

    public void continueGame() {
        this.lastUpdateTime = System.nanoTime();
        if (!canContinue) {
            return;
        }
        setGameState("PLAYING");
        Level currentLevel = levelManager.getCurrentLevel();
        if (currentLevel != null) {
            String music = currentLevel.getThemeMusic();
            if (music != null && !music.isEmpty()) {
                soundManager.playBackgroundMusic(music);
            } else {
                soundManager.playBackgroundMusic("ExoticBaryon_PhaseXX.wav");
            }
        }
    }

    private void goToMenuAndEnableContinue() {
        canContinue = true;
        if (menuManager != null) {
            menuManager.setContinueAvailable(true);
        }
        setGameState("MENU");
        pauseCooldown = true; // Kích hoạt cooldown để tránh click đúp
    }

    private void loadNextLevel() {
        this.currentLevelScore = 0;
        this.currentLevelPlaytimeMillis = 0;
        if (levelManager.loadNextLevel()) {
            if (levelManager.getCurrentLevelIndex() == 0) {
                levelTransition.startInstantFade();
                setGameState("TRANSITION");
            } else {
                levelTransition.startTransition(paddle);
                setGameState("TRANSITION");
            }
        } else {
            setGameState("GAME_WIN");
        }
    }

    private void setupLevelObjects() {
        int gameAreaWidth = ScalingManager.getInstance().GAME_AREA_WIDTH;
        int nativeHeight = ScalingManager.getInstance().NATIVE_HEIGHT;

        int paddleWidth = 120;
        this.ballSize = 20;

        int finalPaddleX = (gameAreaWidth / 2) - (paddleWidth / 2);
        int spawnPaddleY = nativeHeight + 20;

        paddle = new Paddle(finalPaddleX, spawnPaddleY, paddleWidth, 18);
        ball = new Ball(finalPaddleX + (paddleWidth / 2) - (ballSize / 2), spawnPaddleY - ballSize - 1, ballSize, ballSize);

        ball.resetBallPosition(paddle);
        balls.clear();
        balls.add(ball);
        powerUps.clear();
        lasers.clear();
        this.boss = null;
        bricks.clear();
        laserShooters.clear();
        activeShards.clear();
        for (PowerUp p : activePowerUps) {
            p.removeEffect(this);
        }
        activePowerUps.clear();
    }

    private void loadLevelAssetsAndBricks() {
        Level currentLevel = levelManager.getCurrentLevel();
        if (currentLevel != null) {
            String prefix = currentLevel.getThemeAssetPrefix();
            loadThemeAssets(prefix);

            // Tải nhạc nền
            String music = currentLevel.getThemeMusic();
            if (music != null && !music.isEmpty()) {
                soundManager.playBackgroundMusic(music);
            } else {
                soundManager.playBackgroundMusic("ExoticBaryon_PhaseXX.wav");
            }

            // Tải ảnh nền
            String bgName = currentLevel.getThemeBackground();
            if (bgName != null && !bgName.isEmpty()) {
                String assetKey = "bg_" + bgName;
                AssetManager.getInstance().loadImage(assetKey, "/images/" + bgName);
                this.currentBackground = AssetManager.getInstance().getImage(assetKey);
            } else {
                this.currentBackground = AssetManager.getInstance().getImage("defaultBackground");
            }

            if (currentLevel.isBossLevel() && !currentLevel.getBossBricks().isEmpty()) {
                java.awt.Rectangle bossBounds = currentLevel.getBossInitialBounds();
                float startX = (ScalingManager.getInstance().GAME_AREA_WIDTH / 2.0f) - (bossBounds.width / 2.0f);
                float startY = bossBounds.y;

                // TẠO BOSS VỚI GẠCH CỦA NÓ.
                this.boss = new Boss(currentLevel.getBossBricks(), startX, startY, bossBounds.x, ScalingManager.getInstance().GAME_AREA_WIDTH);
            } else {
                this.boss = null;
            }
        }
    }

    private void loadLevelSetup() {
        setupLevelObjects();
        loadLevelAssetsAndBricks();
    }

    private void explode(Brick sourceBrick, double radius) {
        int sourceCenterX = sourceBrick.getX() + sourceBrick.getWidth() / 2;
        int sourceCenterY = sourceBrick.getY() + sourceBrick.getHeight() / 2;

        // Tốc độ lan truyền của vụ nổ (số frame tối đa để lan truyền hết bán kính)
        // Bạn có thể điều chỉnh số này, 30 frame là nửa giây (ở 60FPS)
        final float MAX_PROPAGATION_FRAMES = 30.0f;

        List<Brick> allActiveBricks = new ArrayList<>();
        allActiveBricks.addAll(this.laserShooters);
        allActiveBricks.addAll(this.bricks);
        for (Brick otherBrick : allActiveBricks) {
            if (otherBrick == sourceBrick) { // Bỏ qua chính nó
                continue;
            }

            int otherCenterX = otherBrick.getX() + otherBrick.getWidth() / 2;
            int otherCenterY = otherBrick.getY() + otherBrick.getHeight() / 2;
            double distance = Math.sqrt(Math.pow(sourceCenterX - otherCenterX, 2) + Math.pow(sourceCenterY - otherCenterY, 2));

            // Nếu gạch nằm trong bán kính nổ
            if (distance <= radius) {

                // Tính toán độ trễ dựa trên khoảng cách
                // (distance / radius) là tỉ lệ từ 0.0 đến 1.0
                // Gạch ở gần (distance = 0) -> delay = 0
                // Gạch ở xa (distance = radius) -> delay = MAX_PROPAGATION_FRAMES
                int delay = (int) ((distance / radius) * MAX_PROPAGATION_FRAMES);

                // Kiểm tra xem gạch lân cận có phải là gạch nổ không
                if (otherBrick instanceof ExplosiveBrick) {
                    ExplosiveBrick eb = (ExplosiveBrick) otherBrick;

                    // Chỉ kích hoạt nếu nó còn sống
                    if (eb.isAlive()) {
                        eb.ignite(delay); // Kích hoạt với độ trễ
                    }

                } else if (!otherBrick.isDestroyed()) {

                    boolean wasAboutToDie = otherBrick.getHitPoints() == 1;

                    otherBrick.takeHit();
                    score += 10;

                    if (otherBrick.isDestroyed()) {
                        activeShards.addAll(otherBrick.shatter()); // Vỡ vụn hoàn toàn
                    } else if (!wasAboutToDie) {
                        // Chỉ tạo vỡ vụn nhẹ nếu nó chưa vỡ (HP > 0) và không phải là cú đánh chí mạng
                        otherBrick.shatterHit(activeShards);
                    }
                }
            }
        }
    }

    public void updateGame() {
        long now = System.nanoTime();
        long deltaNanos = (lastUpdateTime > 0) ? (now - lastUpdateTime) : 0;
        long deltaMillis = deltaNanos / 1_000_000;
        this.lastUpdateTime = now;
        ScalingManager sm = ScalingManager.getInstance();
        if ("PLAYING".equals(gameState)) {
            this.playtimeMillis += deltaMillis; // Cập nhật tổng thời gian
            this.currentLevelPlaytimeMillis += deltaMillis;

            Iterator<Shard> shardIterator = activeShards.iterator();
            while (shardIterator.hasNext()) {
                Shard s = shardIterator.next();
                s.update();
                if (!s.isAlive()) {
                    shardIterator.remove();
                }
            }

            boolean esc = inputHandler.isKeyDown(java.awt.event.KeyEvent.VK_ESCAPE);
            int screenMouseX = inputHandler.getMouseX();
            int screenMouseY = inputHandler.getMouseY();

            // Chuyển về tọa độ LOGIC (ảo)
            int mx = sm.unscaleX(screenMouseX); // <-- DÙNG HÀM MỚI
            int my = sm.unscaleY(screenMouseY);

            if (pauseButtonRect.contains(mx, my) && inputHandler.isMousePressed()) {
                if (!pauseCooldown) {
                    setGameState(GAMESTATE_PAUSED);
                    pauseCooldown = true;
                }
                return;
            }

            if (menuButtonRect.contains(mx, my) && inputHandler.isMousePressed()) {
                if (!pauseCooldown) {
                    goToMenuAndEnableContinue();
                    return;
                }
            }

            if (esc || backButton.contains(mx, my) && inputHandler.isMousePressed()) {
                if (!pauseCooldown) {
                    goToMenuAndEnableContinue();
                    return;
                }
            }

            if (!inputHandler.isMousePressed()) {
                pauseCooldown = false;
            }

            paddle.update(inputHandler);
            for (Ball b : balls) {
                b.update(inputHandler, paddle);
            }

            for (Brick brick : this.bricks) {
                brick.update();
            }

            for (LaserShooterBrick shooter : laserShooters) {
                shooter.update();
                Laser newLaser = shooter.tryToShoot();
                if (newLaser != null) {
                    lasers.add(newLaser);
                }
            }

            if (boss != null) {
                boss.update();
            }

            Iterator<Laser> laserIterator = lasers.iterator();
            while (laserIterator.hasNext()) {
                Laser laser = laserIterator.next();
                laser.update();

                if (paddle != null && laser.getBounds().intersects(paddle.getBounds())) {
                    lives--;
                    laserIterator.remove();
                    if (lives <= 0) {
                        setGameState("GAME_OVER");
                        gameOverTimer = 360;
                    }
                    continue;
                }

                if (laser.getY() > screenHeight) {
                    laserIterator.remove();
                }
            }

            // Kiểm tra tương tác với paddle cho tất cả các quả bóng
            for (Ball b : balls) {
                if(b.getY() > screenHeight && balls.indexOf(b) != 0) { // Giữ lại quả bóng đầu tiên để tránh mất hết bóng
                    balls.remove(b);
                    break; // Thoát vòng lặp để tránh ConcurrentModificationException
                }
                if(b.checkCollision(paddle) && !b.isStuckToPaddle()) {
                    if (paddle.isSticky()) {
                        b.stickToPaddle(paddle);
                    } else {
                        b.bounceOff(paddle);
                    }
                }
            }

            Iterator<PowerUp> fallingPowerUpIterator = powerUps.iterator();
            while (fallingPowerUpIterator.hasNext()) {
                PowerUp p = fallingPowerUpIterator.next();
                p.update(); // Cho power-up rơi xuống

                if (paddle.getBounds().intersects(p.getBounds())) {
                    // Kích hoạt power-up mới
                    activatePowerUp(p);
                    fallingPowerUpIterator.remove(); // Xóa khỏi danh sách đang rơi
                }
                // Nếu power-up rơi ra ngoài màn hình
                else if (p.getY() > screenHeight) {
                    fallingPowerUpIterator.remove();
                }
            }

            // 2. QUẢN LÝ THỜI GIAN CỦA TẤT CẢ POWER-UP ĐANG HOẠT ĐỘNG
            Iterator<PowerUp> activePowerUpIterator = activePowerUps.iterator();
            while (activePowerUpIterator.hasNext()) {
                PowerUp p = activePowerUpIterator.next();
                p.tick(); // Đếm ngược thời gian

                if (p.isExpired()) {
                    p.removeEffect(this); // Hủy hiệu ứng
                    activePowerUpIterator.remove(); // Xóa khỏi danh sách đang hoạt động
                }
            }

            List<Brick> allTargets = new ArrayList<>();
            allTargets.addAll(this.laserShooters);
            allTargets.addAll(this.bricks);

            //Duyệt từng quả bóng tương tác với bricks
            for (Ball b : balls) {
                Iterator<Brick> targetIterator = allTargets.iterator();
                while (targetIterator.hasNext()) {
                    Brick target = targetIterator.next();

                    // Chỉ kiểm tra va chạm với những viên gạch chưa bị phá hủy
                    if (!target.isDestroyed() && b.checkCollision(target)) {
                        boolean detonatedImmediately = false;

                        // 1. KIỂM TRA: GẠCH NỔ ĐANG CHỜ BỊ VA CHẠM (KÍCH NỔ TỨC THÌ)
                        if (target instanceof ExplosiveBrick) {
                            ExplosiveBrick eb = (ExplosiveBrick) target;
                            if (eb.isAwaitingDetonation()) {
                                eb.detonateOnHit(activeShards); // Nổ tức thì, tạo mảnh vụn, chuyển EXPLODING
                                score += 10;
                                currentLevelScore += 10;
                                b.bounceOff(target);
                                detonatedImmediately = true;
                            }
                        }

                        // 2. XỬ LÝ VA CHẠM THƯỜNG (CHỈ XẢY RA NẾU KHÔNG NỔ TỨC THÌ)
                        if (!detonatedImmediately) {

                            // A. Vỡ vụn nhẹ (SHATTER HIT)
                            // Nếu gạch còn nhiều hơn 1 hit (chắc chắn chưa vỡ)
                            if (target.getHitPoints() > 1) {
                                target.shatterHit(activeShards);
                            }

                            target.takeHit(); // Gạch nhận sát thương (Giảm HitPoints)
                            score += 10;
                            currentLevelScore += 10;
                            b.bounceOff(target);

                            // B. KIỂM TRA PHÁ HỦY HOÀN TOÀN (SAU KHI takeHit())
                            if (target.isDestroyed()) {

                                if (target instanceof ExplosiveBrick) {
                                    // ExplosiveBrick ALIVE đã gọi startExplosion() trong takeHit()
                                } else {
                                    // Gạch thường/mạnh bị phá hủy hoàn toàn -> Vỡ vụn nặng
                                    activeShards.addAll(target.shatter());
                                }

                                // ... (Logic PowerUp và LaserShooter)
                                PowerUpType typeToDrop = levelManager.getCurrentLevel().getRandomPowerUpType();
                                if (typeToDrop != null) {
                                    PowerUp newPowerUp = createPowerUp(typeToDrop, target.getX(), target.getY());
                                    if (newPowerUp != null) {
                                        powerUps.add(newPowerUp);
                                    }
                                }
                                if (!(target instanceof LaserShooterBrick) && !(target instanceof ExplosiveBrick)) {
                                    double spawnRate = levelManager.getCurrentLevel().getLaserShooterSpawnRate();
                                    if (Math.random() < spawnRate) {
                                        LaserShooterBrick newShooter = new LaserShooterBrick(target.getX(), target.getY(), target.getWidth(), target.getHeight(), 2);
                                        laserShooters.add(newShooter);
                                    }
                                }
                            }
                        }
                        // Bắt buộc ngắt vòng lặp khi va chạm xảy ra
                        break;
                    }
                }
            }
            List<Brick> newlyFinishedExplosions = new ArrayList<>();
            // Tìm tất cả gạch nổ vừa hoàn thành hoạt ảnh
            for (Brick brick : bricks) {
                if (brick instanceof ExplosiveBrick && ((ExplosiveBrick) brick).isFinished()) {
                    newlyFinishedExplosions.add(brick);
                }
            }
            // (Bạn cũng có thể lặp qua laserShooters và boss.getBricks() nếu chúng có thể nổ)

            // Kích hoạt vụ nổ tiếp theo cho mỗi gạch vừa nổ xong
            for (Brick sourceBrick : newlyFinishedExplosions) {
                if (sourceBrick instanceof ExplosiveBrick) {
                    activeShards.addAll(sourceBrick.shatter()); // <--- THÊM DÒNG NÀY
                }
                explode(sourceBrick, 100.0);
            }

            bricks.removeIf(brick -> {
                if (brick instanceof ExplosiveBrick) {
                    return ((ExplosiveBrick) brick).isFinished(); // Remove only when animation is done
                }
                return brick.isDestroyed(); // Remove immediately if not explosive
            });

            laserShooters.removeIf(Brick::isDestroyed); // Remove destroyed laser shooters

            if (boss != null) {
                boss.removeDestroyedBricks(); // Let boss handle its own bricks
            }


            // --- Check Level Win Condition ---
            boolean levelWon = false;
            if (boss != null) {
                // Win boss level if boss is defeated AND no other bricks/shooters remain
                if (boss.isDefeated() && bricks.isEmpty() && laserShooters.isEmpty()) {
                    levelWon = true;
                }
            } else {
                // Win normal level if no bricks AND no laser shooters remain
                if (bricks.isEmpty() && laserShooters.isEmpty()) {
                    levelWon = true;
                }
            }

            if (levelWon) {
                // Submit results for the COMPLETED level
                scoreManager.submitLevelResult(
                        levelManager.getCurrentLevelIndex(), // Index of the level just finished
                        this.currentLevelScore,
                        this.currentLevelPlaytimeMillis
                );
                loadNextLevel(); // Load the next level (this resets score/time for the new level)
                return; // IMPORTANT: Exit updateGame immediately after starting loadNextLevel
            }

            balls.removeIf(b -> b.getY() > screenHeight + 50);

            if (balls.isEmpty()) {
                lives--;
                if (lives > 0) {
                    // SỬ DỤNG BIẾN THÀNH VIÊN:
                    ball = new Ball(paddle.getX() + (paddle.getWidth() / 2) - (this.ballSize / 2), paddle.getY() - this.ballSize - 1, this.ballSize, this.ballSize);
                    ball.stickToPaddle(paddle);
                    balls.add(ball);
                } else {
                    setGameState("GAME_OVER");
                    gameOverTimer = 360;
                }
            }
        } else if (GAMESTATE_PAUSED.equals(gameState)) {
            int screenMouseX = inputHandler.getMouseX();
            int screenMouseY = inputHandler.getMouseY();
            int mx = sm.unscaleX(screenMouseX);
            int my = sm.unscaleY(screenMouseY);

            if (resumeButtonRect.contains(mx, my) && inputHandler.isMousePressed()) {
                if (!pauseCooldown) {
                    setGameState("PLAYING");
                    this.lastUpdateTime = System.nanoTime();
                    pauseCooldown = true;
                }
            }

            // 2. THÊM MỚI: Kiểm tra nút Menu (khi đang pause)
            if (menuButtonRect.contains(mx, my) && inputHandler.isMousePressed()) {
                if (!pauseCooldown) {
                    goToMenuAndEnableContinue();
                    return;
                }
            }

            // 2. Reset cooldown
            if (!inputHandler.isMousePressed()) {
                pauseCooldown = false;
            }
        } else if ("TRANSITION".equals(gameState)) {
            levelTransition.update();
            if (levelTransition.getCurrentState() == LevelTransition.State.FADE_FROM_BLACK) {
                // Dùng getBrickSpawnCount() làm cờ "chỉ chạy 1 lần"
                if (levelTransition.getBrickSpawnCount() == 0) {

                    loadLevelSetup(); // <-- TẠO PADDLE MỚI (this.paddle)
                    // Đây là nơi DUY NHẤT gọi loadLevelSetup()

                    // Dọn dẹp và chuẩn bị gạch chờ
                    this.bricks.clear();
                    this.laserShooters.clear();
                    this.stagingBricks.clear();

                    Level currentLevel = levelManager.getCurrentLevel();
                    if (currentLevel != null) {
                        this.stagingBricks.addAll(currentLevel.getBricks());
                    }

                    if (boss != null) {
                        this.stagingBricks.addAll(boss.getBricks());
                    }

                    levelTransition.setBrickSpawnCount(1); // Đặt cờ để không chạy lại
                }
            }

            if (levelTransition.getCurrentState() == LevelTransition.State.BRICK_SPAWN) {
                if (stagingBricks.isEmpty()) {
                    levelTransition.finishTransition();
                } else {

                    final int BRICKS_PER_FRAME = 2;
                    int bricksToSpawnThisFrame = Math.min(BRICKS_PER_FRAME, stagingBricks.size());

                    for (int i = 0; i < bricksToSpawnThisFrame; i++) {
                        Brick brickToSpawn = stagingBricks.remove(0);
                        if (brickToSpawn instanceof LaserShooterBrick) {
                            laserShooters.add((LaserShooterBrick) brickToSpawn);
                        } else {
                            bricks.add(brickToSpawn);
                        }
                    }
                }
            }

            if (!levelTransition.isTransitioning()) {
                setGameState("PLAYING");
            }

        } else if ("MENU".equals(gameState)) {
            menuManager.update();
        } else if ("GAME_OVER".equals(gameState)) {
            gameOverTimer--; // Đếm ngược
            if (gameOverTimer <= 0) {
                setGameState("MENU");
            }
        } else if ("HIGH_SCORES".equals(gameState)) {
            scoreManager.update();
        } else if ("SETUP".equals(gameState)) {
            setupVolume.update();
        } else if ("LEVEL_SELECT".equals(gameState)) {
            selectLevel.update();
        }
    }

    public MenuManager getMenuManager() { return menuManager; }

    public ScoreManager getScoreManager() {
        return scoreManager;
    }

    public SetupVolume getSetupVolume() {
        return setupVolume;
    }

    public SelectLevel getSelectLevel() {
        return selectLevel;
    }

    public String getGameState() {
        return gameState;
    }

    public void setGameState(String state) {
        if (this.gameState != null && this.gameState.equals(state)) return;
        if (GAMESTATE_PAUSED.equals(state)) {
            soundManager.pauseBackgroundMusic();
        } else if ("PLAYING".equals(state) && GAMESTATE_PAUSED.equals(this.gameState)) {
            soundManager.resumeBackgroundMusic();
        }
        this.gameState = state;

        if ("MENU".equals(state)) {
            soundManager.playBackgroundMusic("Menu.wav");
            if (menuManager != null) menuManager.setContinueAvailable(canContinue);
        } else if ("GAME_OVER".equals(state) || "GAME_WIN".equals(state)) {
            soundManager.stopBackgroundMusic();
            canContinue = false; // không thể continue sau khi game over/win
            if (menuManager != null) menuManager.setContinueAvailable(false);

            if (scoreManager != null) {
                // Gọi hàm mới, truyền cả điểm, thời gian và trạng thái thắng/thua
                boolean didWin = "GAME_WIN".equals(state);
                scoreManager.submitSessionResult(score, playtimeMillis, didWin);
            }
            score = 0;
            playtimeMillis = 0;
        }
    }

    public Image getCurrentBackground() {
        return this.currentBackground;
    }

    private void activatePowerUp(PowerUp newPowerUp) {
        // Dùng iterator để có thể xóa phần tử một cách an toàn
        Iterator<PowerUp> iterator = activePowerUps.iterator();
        while (iterator.hasNext()) {
            PowerUp existingPowerUp = iterator.next();
            // Nếu đã có power-up cùng loại đang hoạt động
            if (existingPowerUp.getType().equals(newPowerUp.getType())) {
                // Hủy hiệu ứng cũ và xóa nó khỏi danh sách
                existingPowerUp.removeEffect(this);
                iterator.remove();
            }
        }

        // Thêm power-up mới vào danh sách và áp dụng hiệu ứng
        activePowerUps.add(newPowerUp);
        newPowerUp.applyEffect(this);
    }

    private PowerUp createPowerUp(PowerUpType type, int x, int y) {
        switch (type) {
            case EXPAND: return new ExpandPaddlePowerUp(x, y, 30, 30);
            case STICKY: return new StickyPaddlePowerUp(x, y, 30, 30);
            case SLOW_BALL: return new SlowBallPowerUp(x, y, 30, 30);
            case FAST_BALL: return new FastBallPowerUp(x, y, 30, 30);
            case EXTRA_LIFE: return new ExtraLifePowerUp(x, y, 30, 30);
            case MULTI_BALL: return new MultiBallPowerUp(x, y, 30, 30);
            default: return null;
        }
    }

    // THÊM GETTER NÀY ĐỂ GAMEPANEL CÓ THỂ VẼ LASER
    public List<Laser> getLasers() {
        return lasers;
    }
    public List<LaserShooterBrick> getLaserShooters() {
        return laserShooters;
    }

    public Boss getBoss() {
        return boss;
    }

    public long getPlaytimeMillis() {
        return playtimeMillis;
    }

    public Rectangle getPauseButtonRect() {
        return pauseButtonRect;
    }

    public Rectangle getResumeButtonRect() {
        return resumeButtonRect;
    }

    public Rectangle getMenuButtonRect() {
        return menuButtonRect;
    }

    public long getCurrentLevelPlaytimeMillis() {
        return currentLevelPlaytimeMillis;
    }

    public void handleInput() {}
    public void checkCollisions() {}
    public void gameOver() {}
    public int getLives () { return lives; }
    public void setLives(int lives) { this.lives = lives; }
    public int getScore() { return score; }
    public void setScore(int score) { this.score = score; }
    public void addball(Ball ball) { balls.add(ball); }
    public List<Ball> getBalls() { return balls; }
    public Paddle getPaddle() { return paddle; }
    public Ball getBall() { return ball; }
    public boolean canContinue() { return canContinue; }
    public BackButton getBackButton() { return backButton; }
    public List<Brick> getBricks() { return bricks; }
    public List<PowerUp> getPowerUps() { return powerUps; }
    public InputHandler getInputHandler() { return inputHandler; }
    public List<Shard> getActiveShards() { // <--- THÊM GETTER NÀY
        return activeShards;
    }

    public LevelTransition getLevelTransition() {
        return levelTransition;
    }

    public LevelManager getLevelManager() {
        return levelManager;
    }
}

