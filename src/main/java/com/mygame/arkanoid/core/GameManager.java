package com.mygame.arkanoid.core;

import com.mygame.arkanoid.engine.AssetManager;
import com.mygame.arkanoid.objects.*;
import com.mygame.arkanoid.objects.bricks.Brick;
import com.mygame.arkanoid.systems.*;
import com.mygame.arkanoid.objects.Paddle;
import com.mygame.arkanoid.objects.powerups.PowerUp;
import com.mygame.arkanoid.engine.InputHandler;
import com.mygame.arkanoid.engine.Renderer;
import com.mygame.arkanoid.engine.SoundManager;

import com.mygame.arkanoid.objects.bricks.*;
import com.mygame.arkanoid.objects.powerups.*;
import com.mygame.arkanoid.systems.Save.SaveData;
import com.mygame.arkanoid.systems.Save.SaveSystem;

import java.awt.*;
import java.util.*;
import java.util.List;

public class GameManager {
    private List<HeartUI> hearts;
    private BackButton backButton;
    private boolean canContinue = false;

    private PlayerStats playerStats;

    private String gameState;
    private int gameOverTimer;

    private GameSummaryPanel gameSummaryPanel;

    private EntityManager entityManager;
    private CollisionSystem collisionSystem;
    private ScoreManager scoreManager;
    private LevelManager levelManager;
    private Renderer renderer;
    private LevelTransition levelTransition;
    private SoundManager soundManager;
    private InputHandler inputHandler;
    private MenuManager menuManager;
    private SettingManager settingManager;
    private SelectLevel selectLevel;
    private Image currentBackground = null;

    private boolean pauseCooldown = false;
    private Rectangle pauseButtonRect;
    private Rectangle resumeButtonRect;
    private Rectangle menuButtonRect;

    private String selectedBallSkinKey = "skin_ball_1";
    private String selectedPaddleSkinKey = "skin_paddle_1";

    public static final String GAMESTATE_PAUSED = "PAUSED";

    public static final String GAMESTATE_TRANSITION_OUT = "TRANSITION_OUT";
    public static final String GAMESTATE_TRANSITION_IN = "TRANSITION_IN";

    public SoundManager getSoundManager() {
        return soundManager;
    }

    // Holder idiom: thread-safe, lazy
    private static class Holder {
        private static final GameManager INSTANCE = new GameManager();
    }

    private GameManager() {
        inputHandler = new InputHandler();
        entityManager = new EntityManager();
        backButton = new BackButton(10, 10, 40, 40);
        this.soundManager = new SoundManager();
        this.hearts = new ArrayList<>();

        playerStats = new PlayerStats();

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

        AssetManager.getInstance().loadGlobalAssets();

        levelManager = new LevelManager();
        levelManager.loadLevels();
        this.levelTransition = new LevelTransition(this);
        menuManager = new MenuManager(this, inputHandler);

        this.gameSummaryPanel = new GameSummaryPanel();

        collisionSystem = new CollisionSystem();
        scoreManager = new ScoreManager(this, inputHandler);
        settingManager = new SettingManager(inputHandler, this, soundManager);
        selectLevel = new SelectLevel(inputHandler, this, levelManager);

        // Đặt trạng thái ban đầu của game là MENU
        this.gameState = "MENU";
        canContinue = false;
        soundManager.playBackgroundMusic("Menu.wav");
    }

    public static GameManager getInstance() {
        return Holder.INSTANCE;
    }

    private void loadThemeAssets(String prefix) {
        AssetManager.getInstance().loadTheme(prefix);
    }

    public void startGame() {
        playerStats.resetForNewGame();
        setupLevelObjects();
        canContinue = false;
        levelManager.reset(); // Đưa level manager về màn 1
        loadNextLevel();
    }

    public void startGameAtLevel(int levelIndex) {
        playerStats.resetForNewGame();
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
        playerStats.setLastUpdateTime(System.nanoTime());
        if (!canContinue) {
            return;
        }

        ensureLevelHydrated();
        ensurePaddleAndBallReadyForPlay();

        setGameState("PLAYING");
        var currentLevel = levelManager.getCurrentLevel();
        String music = (currentLevel != null) ? currentLevel.getThemeMusic() : null;
        if (music != null && !music.isEmpty()) {
            soundManager.playBackgroundMusic(music);
        } else {
            soundManager.playBackgroundMusic("Background_Music_Backup.wav");
        }
    }

    // SỬA THÂN HÀM: LƯU KHI VỀ MENU
    private void goToMenuAndEnableContinue() {
        canContinue = true;
        if (menuManager != null) {
            menuManager.setContinueAvailable(true);
        }
        setGameState("MENU");
        pauseCooldown = true;
        // Lưu trạng thái để Continue
        SaveSystem.save(SaveSystem.capture(this));
    }

    private void loadNextLevel() {
        playerStats.resetForNextLevel();
        if (levelManager.loadNextLevel()) {
            if (levelManager.getCurrentLevelIndex() == 0) {
                levelTransition.startInstantFade();
                setGameState("TRANSITION");
            } else {
                levelTransition.startTransition(entityManager.getPaddle());
                setGameState("TRANSITION");
            }
        } else {
            setGameState("GAME_WIN");
        }
    }

    private void setupLevelObjects() {
        entityManager.resetForNewLevel(selectedPaddleSkinKey, selectedBallSkinKey);
        for (PowerUp p : entityManager.getActivePowerUps()) {
            p.removeEffect(this);
        }
        entityManager.getActivePowerUps().clear();
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

            Boss newBoss = null;
            if (currentLevel.isBossLevel() && !currentLevel.getBossBricks().isEmpty()) {
                java.awt.Rectangle bossBounds = currentLevel.getBossInitialBounds();
                float startX = (ScalingManager.getInstance().GAME_AREA_WIDTH / 2.0f) - (bossBounds.width / 2.0f);
                float startY = bossBounds.y;

                newBoss = new Boss(currentLevel.getBossBricks(), startX, startY, bossBounds.x, ScalingManager.getInstance().GAME_AREA_WIDTH);
            }

            entityManager.hydrateLevel(currentLevel, newBoss);
            ensureBrickIdsAssigned();
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
        allActiveBricks.addAll(entityManager.getLaserShooters());
        allActiveBricks.addAll(entityManager.getBricks());
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
                    addScore(10);
                    if (otherBrick.isDestroyed()) {
                        getActiveShards().addAll(otherBrick.shatter()); // Vỡ vụn hoàn toàn
                    } else if (!wasAboutToDie) {
                        // Chỉ tạo vỡ vụn nhẹ nếu nó chưa vỡ (HP > 0) và không phải là cú đánh chí mạng
                        otherBrick.shatterHit(getActiveShards());
                    }
                }
            }
        }
    }

    public void addScore(int points) {
        playerStats.addScore(points);
    }

    public void updateGame() {
        long now = System.nanoTime();
        long deltaNanos = (playerStats.getLastUpdateTime() > 0) ? (now - playerStats.getLastUpdateTime()) : 0;
        long deltaMillis = deltaNanos / 1_000_000;
        playerStats.setLastUpdateTime(now);
        ScalingManager sm = ScalingManager.getInstance();
        if ("PLAYING".equals(gameState)) {
            playerStats.updatePlaytime(deltaMillis);

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

            entityManager.updateAll(inputHandler);

            // 2. QUẢN LÝ THỜI GIAN CỦA TẤT CẢ POWER-UP ĐANG HOẠT ĐỘNG
            Iterator<PowerUp> activePowerUpIterator = entityManager.getActivePowerUps().iterator();
            while (activePowerUpIterator.hasNext()) {
                PowerUp p = activePowerUpIterator.next();
                p.tick(); // Đếm ngược thời gian
                if (p.isExpired()) {
                    p.removeEffect(this); // Hủy hiệu ứng
                    activePowerUpIterator.remove(); // Xóa khỏi danh sách đang hoạt động
                }
            }

            collisionSystem.checkAllCollisions(this);

            List<Brick> newlyFinishedExplosions = new ArrayList<>();
            // Tìm tất cả gạch nổ vừa hoàn thành hoạt ảnh
            for (Brick brick : entityManager.getBricks()) {
                if (brick instanceof ExplosiveBrick && ((ExplosiveBrick) brick).isFinished()) {
                    newlyFinishedExplosions.add(brick);
                }
            }

            for (Brick sourceBrick : newlyFinishedExplosions) {
                if (sourceBrick instanceof ExplosiveBrick) {
                    getActiveShards().addAll(sourceBrick.shatter()); // <--- THÊM DÒNG NÀY
                }
                soundManager.playSound(SoundManager.SFX_EXPLOSION);
                explode(sourceBrick, 100.0);
            }

            entityManager.cleanupDestroyedObjects(getScreenHeight());

            if (entityManager.isLevelWon()) {
                // Submit results for the COMPLETED level
                scoreManager.submitLevelResult(
                        levelManager.getCurrentLevelIndex(), // Index of the level just finished
                        playerStats.getCurrentLevelScore(),
                        playerStats.getCurrentLevelPlaytimeMillis()
                );
                loadNextLevel(); // Load the next level (this resets score/time for the new level)
                return; // IMPORTANT: Exit updateGame immediately after starting loadNextLevel
            }


            if (entityManager.areBallsEmpty()) { // <-- Dùng hàm của EntityManager
                playerStats.loseLife();
                if (playerStats.getLives() > 0) {
                    soundManager.playSound(SoundManager.SFX_BALL_LOSS);
                    entityManager.respawnBall(selectedBallSkinKey);

                } else {
                    setGameState("GAME_OVER");
                    gameOverTimer = 360; // (Bạn có thể cân nhắc chuyển 360 vào GameConstants)
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
                    playerStats.setLastUpdateTime(System.nanoTime());
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

                    loadLevelSetup();

                    // SỬA LẠI: boss là của entityManager
                    if (entityManager.getBoss() != null) {
                        entityManager.getBoss().update();
                    }

                    // SỬA LẠI: Các list này đều là của entityManager
                    entityManager.getBricks().clear();
                    entityManager.getLaserShooters().clear();
                    entityManager.getStagingBricks().clear();

                    Level currentLevel = levelManager.getCurrentLevel();
                    if (currentLevel != null) {
                        entityManager.getStagingBricks().addAll(currentLevel.getBricks());
                    }

                    // SỬA LẠI: boss là của entityManager
                    if (entityManager.getBoss() != null) {
                        entityManager.getStagingBricks().addAll(entityManager.getBoss().getBricks());
                    }

                    levelTransition.setBrickSpawnCount(1); // Đặt cờ để không chạy lại
                }

            }

            if (levelTransition.getCurrentState() == LevelTransition.State.BRICK_SPAWN) {
                if (entityManager.getStagingBricks().isEmpty()) {
                    levelTransition.finishTransition();
                } else {

                    final int BRICKS_PER_FRAME = 2;
                    int bricksToSpawnThisFrame = Math.min(BRICKS_PER_FRAME, entityManager.getStagingBricks().size());

                    for (int i = 0; i < bricksToSpawnThisFrame; i++) {
                        // SỬA LẠI: Thao tác trên list của entityManager
                        Brick brickToSpawn = entityManager.getStagingBricks().remove(0);
                        if (brickToSpawn instanceof LaserShooterBrick) {
                            entityManager.addLaserShooter((LaserShooterBrick) brickToSpawn);
                        } else {
                            // Chúng ta quên thêm addBrick(), nên dùng getBricks().add()
                            entityManager.getBricks().add(brickToSpawn);
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
        } else if ("GAME_WIN".equals(gameState)) {
            gameOverTimer--;
            if (gameOverTimer <= 0) {
                setGameState("MENU");
            }

        } else if ("HIGH_SCORES".equals(gameState)) {
            scoreManager.update();
        } else if ("SETTING".equals(gameState)) {
            settingManager.update();
        } else if ("LEVEL_SELECT".equals(gameState)) {
            selectLevel.update();
        }
    }

    public MenuManager getMenuManager() { return menuManager; }

    public ScoreManager getScoreManager() {
        return scoreManager;
    }

    public SettingManager getSettingManager() { return settingManager; }
    public SelectLevel getSelectLevel() {
        return selectLevel;
    }

    public String getGameState() {
        return gameState;
    }

    public void setGameState(String state) {
        if (this.gameState != null && this.gameState.equals(state)) return;

        String oldState = this.gameState;
        if (GAMESTATE_PAUSED.equals(state)) {
            soundManager.pauseBackgroundMusic();
        } else if ("PLAYING".equals(state) && GAMESTATE_PAUSED.equals(oldState)) {
            soundManager.resumeBackgroundMusic();
        }
        this.gameState = state;

        if ("MENU".equals(state)) {
            boolean wasInMenuScreens = "MENU".equals(oldState) ||
                    "SETTING".equals(oldState) ||
                    "HIGH_SCORES".equals(oldState) ||
                    "LEVEL_SELECT".equals(oldState);

            if (!wasInMenuScreens) {
                soundManager.playBackgroundMusic("Menu.wav");
            }
            if (menuManager != null) menuManager.setContinueAvailable(canContinue);
        } else if ("GAME_OVER".equals(state) || "GAME_WIN".equals(state)) {
            soundManager.stopBackgroundMusic();
            canContinue = false;
            if (menuManager != null) menuManager.setContinueAvailable(false);

            this.gameOverTimer = 480;
            playerStats.captureFinalStats();

            if (scoreManager != null) {
                // Gọi hàm mới, truyền cả điểm, thời gian và trạng thái thắng/thua
                boolean didWin = "GAME_WIN".equals(state);
                scoreManager.submitSessionResult(playerStats.getFinalScore(), playerStats.getFinalPlaytimeMillis(), didWin);
            }

            // Xoá file save vì phiên chơi đã kết thúc
            SaveSystem.deleteSave();
        }
    }

    public Image getCurrentBackground() {
        return this.currentBackground;
    }

    public void activatePowerUp(PowerUp newPowerUp) {
        String newType = newPowerUp.getType();
        Iterator<PowerUp> iterator = entityManager.getActivePowerUps().iterator();
        while (iterator.hasNext()) {
            PowerUp existingPowerUp = iterator.next();
            String existingType = existingPowerUp.getType();
            // Nếu đã có power-up cùng loại đang hoạt động
            if (existingPowerUp.getType().equals(newPowerUp.getType())) {
                // Hủy hiệu ứng cũ và xóa nó khỏi danh sách
                existingPowerUp.removeEffect(this);
                iterator.remove();
            } else if ( (existingType.equals("fast_ball") && newType.equals("slow_ball")) ||
                    (existingType.equals("slow_ball") && newType.equals("fast_ball")) )
            {
                existingPowerUp.removeEffect(this); // Hủy hiệu ứng CŨ
                iterator.remove(); // Xóa power-up CŨ khỏi danh sách
            }
        }
        soundManager.playSound(SoundManager.SFX_POWERUP);
        entityManager.addActivePowerUp(newPowerUp);
        newPowerUp.applyEffect(this);
    }

    public void ensureBrickIdsAssigned() {
        int idCounter = 0;
        if (levelManager != null && levelManager.getCurrentLevel() != null) {
            for (Brick b : levelManager.getCurrentLevel().getBricks()) {
                if (b.getId() < 0) b.setId(idCounter++);
            }
        }
        Boss currentBoss = entityManager.getBoss();
        if (currentBoss != null) {
            for (Brick b : currentBoss.getBricks()) {
                if (b.getId() < 0) b.setId(idCounter++);
            }
        }
    }

    private void ensurePaddleAndBallReadyForPlay() {
        int gameAreaWidth = ScalingManager.getInstance().GAME_AREA_WIDTH;
        int nativeHeight = ScalingManager.getInstance().NATIVE_HEIGHT;
        int currentBallSize = (entityManager.getBallSize() > 0) ? entityManager.getBallSize() : 18;

        boolean needRecreate = (entityManager.getPaddle() == null || entityManager.getBall() == null);
        if (!needRecreate) {
            needRecreate = entityManager.getPaddle().getY() > nativeHeight;
        }

        if (needRecreate) {
            int paddleWidth = 120;
            int px = (gameAreaWidth - paddleWidth) / 2;
            int py = nativeHeight - 80;

            Paddle newPaddle = new Paddle(px, py, paddleWidth, 30, selectedPaddleSkinKey);
            Ball newBall = new Ball(px + (paddleWidth / 2) - (currentBallSize / 2), py - currentBallSize - 1, currentBallSize, currentBallSize, selectedBallSkinKey);
            newBall.resetBallPosition(newPaddle);

            // Dọn dẹp và thêm vào EntityManager
            entityManager.getPaddle(); // (để gán paddle)
            entityManager.getBalls().clear();
            entityManager.addBall(newBall);

            // ... (Dọn dẹp active power-ups)
            for (PowerUp p : entityManager.getActivePowerUps()) {
                p.removeEffect(this);
            }
            entityManager.getActivePowerUps().clear();
        }
    }

    private void ensureLevelHydrated() {
        Level currentLevel = levelManager.getCurrentLevel();
        if (currentLevel == null) return;

        // Đảm bảo theme/background/boss sẵn sàng
        loadLevelAssetsAndBricks();

        // Chỉ nạp nếu đang rỗng để tránh nhân đôi
        if (entityManager.getBricks().isEmpty() && entityManager.getLaserShooters().isEmpty()) {
            List<Brick> source = new ArrayList<>();
            source.addAll(currentLevel.getBricks());

            // SỬA LẠI: Dùng getter của entityManager
            if (entityManager.getBoss() != null) {
                source.addAll(entityManager.getBoss().getBricks());
            }

            for (Brick brickToSpawn : source) {
                if (brickToSpawn instanceof LaserShooterBrick) {
                    // SỬA LẠI: Dùng getter của entityManager
                    if (!entityManager.getLaserShooters().contains(brickToSpawn)) {
                        entityManager.getLaserShooters().add((LaserShooterBrick) brickToSpawn);
                    }
                } else if (!entityManager.getBricks().contains(brickToSpawn)) {
                    // SỬA LẠI: Dùng getter của entityManager
                    entityManager.getBricks().add(brickToSpawn);
                }
            }
        }
    }

    public void restoreFromSave(SaveData data) {
        if (data == null) return;

        if (getLevelManager() != null) {
            getLevelManager().loadSpecificLevel(data.getLevelIndex());
        }

        // Nạp theme/background/boss và gán ID
        loadLevelAssetsAndBricks();
        ensureBrickIdsAssigned();

        List<Integer> aliveList = (data.getAliveBrickIds() != null)
                ? data.getAliveBrickIds()
                : java.util.Collections.emptyList();

        entityManager.hydrateFromSave(
                levelManager.getCurrentLevel(),
                entityManager.getBoss(), // Truyền boss đã được tạo
                new java.util.HashSet<>(aliveList)
        );

        // Áp số liệu phiên chơi
        playerStats.setSessionStats(
                data.getScore(),
                data.getLives(),
                data.getPlaytimeMillis(),
                data.getCurrentLevelPlaytimeMillis()
        );

        // Bật continue trong menu
        this.canContinue = data.isCanContinue();
        if (menuManager != null) {
            menuManager.setContinueAvailable(this.canContinue);
        }

        // Về MENU, chờ người chơi ấn Continue
        setGameState("MENU");
    }

    public PlayerStats getPlayerStats() { return playerStats; }

    public List<Laser> getLasers() {
        return entityManager.getLasers();
    }
    public List<LaserShooterBrick> getLaserShooters() {
        return entityManager.getLaserShooters();
    }

    public Boss getBoss() {
        return entityManager.getBoss();
    }

    public long getPlaytimeMillis() {
        return playerStats.getPlaytimeMillis();
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
        return playerStats.getCurrentLevelPlaytimeMillis();
    }

    public int getLives () { return playerStats.getLives(); }
    public void setLives(int lives) { playerStats.setLives(lives); }
    public int getScore() { return playerStats.getScore(); }
    public void setScore(int score) { playerStats.setScore(score); }


    public void addball(Ball ball) { entityManager.addBall(ball); }
    public void addLaserShooter(LaserShooterBrick brick) { entityManager.addLaserShooter(brick); }
    public void addPowerUp(PowerUp powerUp) { entityManager.addPowerUp(powerUp); }

    public List<Ball> getBalls() { return entityManager.getBalls(); }
    public Paddle getPaddle() { return entityManager.getPaddle(); }
    public Ball getBall() { return entityManager.getBall(); }
    public boolean canContinue() { return canContinue; }
    public BackButton getBackButton() { return backButton; }
    public List<Brick> getBricks() { return entityManager.getBricks(); }
    public List<PowerUp> getPowerUps() { return entityManager.getPowerUps(); }
    public InputHandler getInputHandler() { return inputHandler; }
    public List<Shard> getActiveShards() { // <--- THÊM GETTER NÀY
        return entityManager.getActiveShards();
    }

    public LevelTransition getLevelTransition() {
        return levelTransition;
    }

    public LevelManager getLevelManager() {
        return levelManager;
    }

    public String getSelectedBallSkinKey() {
        return selectedBallSkinKey;
    }

    public void setSelectedBallSkinKey(String selectedBallSkinKey) {
        this.selectedBallSkinKey = selectedBallSkinKey;
        entityManager.setBallSkin(selectedBallSkinKey);
    }

    public String getSelectedPaddleSkinKey() {
        return selectedPaddleSkinKey;
    }

    public void setSelectedPaddleSkinKey(String selectedPaddleSkinKey) {
        this.selectedPaddleSkinKey = selectedPaddleSkinKey;
        entityManager.setPaddleSkin(selectedPaddleSkinKey);
    }

    public GameSummaryPanel getGameSummaryPanel() {
        return gameSummaryPanel;
    }

    public int getFinalScore() {
        return playerStats.getFinalScore();
    }

    public long getFinalPlaytimeMillis() {
        return playerStats.getFinalPlaytimeMillis();
    }

    public int getScreenHeight() {
        return ScalingManager.getInstance().NATIVE_HEIGHT;
    }


}

