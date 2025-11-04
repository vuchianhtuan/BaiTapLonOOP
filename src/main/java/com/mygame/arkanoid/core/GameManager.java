package com.mygame.arkanoid.core;

import com.mygame.arkanoid.effects.Shard;
import com.mygame.arkanoid.engine.AssetManager;
import com.mygame.arkanoid.objects.*;
import com.mygame.arkanoid.objects.bricks.Brick;
import com.mygame.arkanoid.objects.Paddle;
import com.mygame.arkanoid.objects.powerups.PowerUp;
import com.mygame.arkanoid.engine.InputHandler;
import com.mygame.arkanoid.engine.SoundManager;

import com.mygame.arkanoid.objects.bricks.*;
import com.mygame.arkanoid.systems.CollisionSystem;
import com.mygame.arkanoid.systems.ExplosionSystem;
import com.mygame.arkanoid.ui.screens.ScoreManager;
import com.mygame.arkanoid.ui.hud.GameSummaryPanel;
import com.mygame.arkanoid.systems.ScalingManager;
import com.mygame.arkanoid.level.Level;
import com.mygame.arkanoid.level.LevelManager;
import com.mygame.arkanoid.level.LevelTransition;
import com.mygame.arkanoid.systems.PlayerStats;
import com.mygame.arkanoid.ui.hud.Sidebar;
import com.mygame.arkanoid.systems.EntityManager;
import com.mygame.arkanoid.ui.screens.SelectLevel;
import com.mygame.arkanoid.ui.screens.MenuManager;
import com.mygame.arkanoid.ui.screens.SettingManager;
import com.mygame.arkanoid.save.SaveData;
import com.mygame.arkanoid.save.SaveSystem;
import com.mygame.arkanoid.config.GameConstants;

import java.awt.*;
import java.util.*;
import java.util.List;

/**
 * Lớp quản lý trò chơi chính, chịu trách nhiệm về trạng thái trò chơi,
 */
public class GameManager {
    /**
     * Các biến và đối tượng quản lý trò chơi chính
     */
    private boolean canContinue = false;
    private PlayerStats playerStats;
    private String gameState;
    private int gameOverTimer;
    private GameSummaryPanel gameSummaryPanel;
    private Sidebar sidebar;
    private EntityManager entityManager;
    private CollisionSystem collisionSystem;
    private ScoreManager scoreManager;
    private LevelManager levelManager;
    private LevelTransition levelTransition;
    private SoundManager soundManager;
    private InputHandler inputHandler;
    private MenuManager menuManager;
    private SettingManager settingManager;
    private SelectLevel selectLevel;
    private Image currentBackground = null;
    private ExplosionSystem explosionSystem;
    public static final String GAMESTATE_PAUSED = "PAUSED";

    public SoundManager getSoundManager() { return soundManager; }

    private GameManager() {
        inputHandler = new InputHandler();
        entityManager = new EntityManager();
        this.soundManager = new SoundManager();
        playerStats = new PlayerStats();
        this.sidebar = new Sidebar(this, inputHandler);
        AssetManager.getInstance().loadGlobalAssets();
        levelManager = new LevelManager();
        levelManager.loadLevels();
        this.levelTransition = new LevelTransition(this);
        menuManager = new MenuManager(this, inputHandler);
        this.gameSummaryPanel = new GameSummaryPanel();
        collisionSystem = new CollisionSystem();
        explosionSystem = new ExplosionSystem();
        scoreManager = new ScoreManager(this, inputHandler);
        settingManager = new SettingManager(inputHandler, this, soundManager);
        selectLevel = new SelectLevel(inputHandler, this);
        this.gameState = "MENU";
        canContinue = false;
        soundManager.playBackgroundMusic("Menu.wav");
    }

    /**
     * Singleton Holder pattern để đảm bảo chỉ có một instance của GameManager.
     */
    private static class Holder {
        private static final GameManager INSTANCE = new GameManager();
    }

    /**
     * Lấy instance duy nhất của GameManager.
     * @return Instance của GameManager.
     */
    public static GameManager getInstance() { return Holder.INSTANCE; }

    private void loadThemeAssets(String prefix) { AssetManager.getInstance().loadTheme(prefix); }

    /**
     * Bắt đầu trò chơi từ đầu với level đầu tiên.
     */
    public void startGame() {
        playerStats.resetForNewGame();
        setupLevelObjects();
        canContinue = false;
        levelManager.reset();
        loadNextLevel();
    }
    public void startGameAtLevel(int levelIndex) {
        // Bắt đầu trò chơi từ level cụ thể
        playerStats.resetForNewGame();
        canContinue = false;

        // Thử tải level cụ thể nếu không được thì quay về menu
        if (levelManager.loadSpecificLevel(levelIndex)) {
            setupLevelObjects();
            levelTransition.startInstantFade();
            setGameState("TRANSITION");
        } else {
            System.err.println("Lỗi: Không thể tải level tại index " + levelIndex);
            setGameState("MENU");
        }
    }

    /**
     * Tiếp tục trò chơi từ trạng thái tạm dừng hoặc menu khi có thể tiếp tục.
     */
    public void continueGame() {
        playerStats.setLastUpdateTime(System.nanoTime()); // Cập nhật thời gian lần cuối chơi
        if (!canContinue) return; // Không thể tiếp tục nếu không có quyền
        ensureLevelHydrated(); // Đảm bảo level đã được nạp
        ensurePaddleAndBallReadyForPlay(); // Đảm bảo paddle và ball sẵn sàng
        setGameState("PLAYING"); // Chuyển sang trạng thái chơi
        var currentLevel = levelManager.getCurrentLevel(); // Lấy level hiện tại
        String music = (currentLevel != null) ? currentLevel.getThemeMusic() : null;
        if (music != null && !music.isEmpty()) {
            soundManager.playBackgroundMusic(music);
        } else {
            soundManager.playBackgroundMusic("Background_Music_Backup.wav");
        }
    }

    /**
     * Quay về menu và cho phép tiếp tục trò chơi.
     * Lưu trạng thái trò chơi hiện tại.
     */
    public void goToMenuAndEnableContinue() {
        canContinue = true;
        if (menuManager != null) menuManager.setContinueAvailable(true); // Cập nhật menu
        setGameState("MENU"); // Chuyển sang trạng thái menu
        SaveSystem.save(SaveSystem.capture(this)); // Lưu trạng thái trò chơi
    }

    /**
     * Tải level tiếp theo trong trò chơi.
     * Nếu không còn level nào, chuyển sang trạng thái chiến thắng.
     */
    private void loadNextLevel() {
        playerStats.resetForNextLevel(); // Đặt lại các chỉ số cho level mới

        // Thử tải level tiếp theo nếu có không thì chuyển sang trạng thái chiến thắng
        if (levelManager.loadNextLevel()) {
            if (levelManager.getCurrentLevelIndex() == 0) {
                levelTransition.startInstantFade(); // Bắt đầu chuyển cảnh ngay lập tức cho level đầu tiên
                setGameState("TRANSITION");
            } else {
                levelTransition.startTransition(entityManager.getPaddle());
                setGameState("TRANSITION");
            }
        } else {
            setGameState("GAME_WIN");
        }
    }

    /**
     * Thiết lập các đối tượng cần thiết cho level mới.
     */
    private void setupLevelObjects() {
        // Đặt lại thực thể và xóa các hiệu ứng PowerUp
        entityManager.resetForNewLevel(settingManager.getSelectedPaddleSkinKey(), settingManager.getSelectedBallSkinKey());
        for (PowerUp p : entityManager.getActivePowerUps()) {
            p.removeEffect(this);
        }
        entityManager.getActivePowerUps().clear(); // Xóa danh sách PowerUp đang hoạt động
    }

    /**
     * Tải các tài nguyên (assets) cần thiết cho level hiện tại.
     * @param currentLevel Level hiện tại.
     */
    private void loadLevelAssets(Level currentLevel) {
        String prefix = currentLevel.getThemeAssetPrefix();
        loadThemeAssets(prefix); // Tải tài nguyên theo tiền tố của theme

        String music = currentLevel.getThemeMusic();
        // Phát nhạc nền phù hợp với level
        if (music != null && !music.isEmpty()) {
            soundManager.playBackgroundMusic(music);
        } else {
            soundManager.playBackgroundMusic("ExoticBaryon_PhaseXX.wav");
        }

        // Tải hình nền phù hợp với level
        String bgName = currentLevel.getThemeBackground();
        this.currentBackground = AssetManager.getInstance().getBackgroundImage(bgName);
    }

    /**
     * Tải thiết lập level bao gồm dọn dẹp đối tượng, tải assets và nạp thực thể.
     */
    private void loadLevelSetup() {
        // Lấy level hiện tại
        Level currentLevel = levelManager.getCurrentLevel();
        if (currentLevel == null) return;

        // Thực hiện các bước thiết lập level
        setupLevelObjects();            // 1. Dọn dẹp Player
        loadLevelAssets(currentLevel);       // 2. Tải Assets (Nhạc/Nền)
        entityManager.hydrateLevel(currentLevel); // 3. NẠP THỰC THỂ (Gạch/Boss)
    }


    /**
     * Cập nhật trạng thái trò chơi dựa trên gameState hiện tại.
     */
    public void updateGame() {
        // Tính toán delta time kể từ lần cập nhật cuối cùng
        long now = System.nanoTime();
        long deltaNanos = (playerStats.getLastUpdateTime() > 0) ? (now - playerStats.getLastUpdateTime()) : 0;
        long deltaMillis = deltaNanos / 1_000_000;
        playerStats.setLastUpdateTime(now);

        // Cập nhật trạng thái trò chơi dựa trên gameState hiện tại
        if ("PLAYING".equals(gameState)) {
            playerStats.updatePlaytime(deltaMillis); // Cập nhật thời gian chơi
            entityManager.updateAll(inputHandler); // Cập nhật tất cả thực thể
            sidebar.update(); // Cập nhật sidebar

            // Cập nhật PowerUps đang hoạt động
            Iterator<PowerUp> activePowerUpIterator = entityManager.getActivePowerUps().iterator();
            while (activePowerUpIterator.hasNext()) {
                PowerUp p = activePowerUpIterator.next(); // Lấy PowerUp tiếp theo
                p.tick(); // Cập nhật trạng thái PowerUp
                if (p.isExpired()) {
                    p.removeEffect(this); // Loại bỏ hiệu ứng PowerUp
                    activePowerUpIterator.remove(); // Xóa PowerUp khỏi danh sách
                }
            }

            collisionSystem.checkAllCollisions(this); // Kiểm tra va chạm giữa các thực thể

            // Xử lý các vụ nổ từ ExplosiveBricks đã hoàn thành
            List<Brick> newlyFinishedExplosions = new ArrayList<>();
            for (Brick brick : entityManager.getBricks()) {
                // Kiểm tra nếu brick là ExplosiveBrick và đã hoàn thành vụ nổ
                if (brick instanceof ExplosiveBrick && ((ExplosiveBrick) brick).isFinished()) {
                    newlyFinishedExplosions.add(brick); // Thêm vào danh sách để xử lý sau
                }
            }

            // Xử lý vụ nổ và tạo mảnh vỡ
            for (Brick sourceBrick : newlyFinishedExplosions) {
                if (sourceBrick instanceof ExplosiveBrick) {
                    getActiveShards().addAll(sourceBrick.shatter()); // Tạo mảnh vỡ từ gạch
                }
                soundManager.playSound(SoundManager.SFX_EXPLOSION);
                explosionSystem.explode(sourceBrick, 50.0, this); // Thực hiện vụ nổ
            }

            // Dọn dẹp các đối tượng đã bị phá hủy khỏi trò chơi
            entityManager.cleanupDestroyedObjects(getScreenHeight());

            // Kiểm tra điều kiện thắng/thua
            if (entityManager.isLevelWon()) {
                // Ghi nhận kết quả level hiện tại vào ScoreManager và chuyển sang level tiếp theo
                scoreManager.submitLevelResult(
                        levelManager.getCurrentLevelIndex(),
                        playerStats.getCurrentLevelScore(),
                        playerStats.getCurrentLevelPlaytimeMillis()
                );
                loadNextLevel();
                return;
            }

            // Kiểm tra nếu không còn bóng nào trên màn hình
            if (entityManager.areBallsEmpty()) {
                playerStats.loseLife(); // Giảm số mạng sống

                // Kiểm tra còn mạng sống không để quyết định tiếp tục hay kết thúc trò chơi
                if (playerStats.getLives() > 0) {
                    soundManager.playSound(SoundManager.SFX_BALL_LOSS);
                    entityManager.respawnBall(settingManager.getSelectedBallSkinKey());
                } else {
                    setGameState("GAME_OVER");
                    gameOverTimer = GameConstants.GAME_OVER_TIMER_FRAMES;
                }
            }

        } else if (GAMESTATE_PAUSED.equals(gameState)) {
            sidebar.update(); // Cập nhật sidebar khi trò chơi bị tạm dừng

        } else if ("TRANSITION".equals(gameState)) {
            // Cập nhật quá trình chuyển cảnh
            levelTransition.update();
            // Xử lý các giai đoạn chuyển cảnh khác nhau
            if (levelTransition.getCurrentState() == LevelTransition.State.FADE_FROM_BLACK) {
                if (levelTransition.getBrickSpawnCount() == 0) {
                    loadLevelSetup();

                    // Cập nhật boss 1 lần
                    if (entityManager.getBoss() != null) {
                        entityManager.getBoss().update();
                    }

                    // Dọn dẹp danh sách sẽ hiển thị
                    entityManager.getBricks().clear();
                    entityManager.getLaserShooters().clear();
                    entityManager.getStagingBricks().clear();

                    // Nạp gạch (thường + boss) vào "cánh gà" (staging)
                    Level currentLevel = levelManager.getCurrentLevel();
                    if (currentLevel != null) {
                        entityManager.getStagingBricks().addAll(currentLevel.getBricks());
                    }
                    if (entityManager.getBoss() != null) {
                        entityManager.getStagingBricks().addAll(entityManager.getBoss().getBricks());
                    }

                    levelTransition.setBrickSpawnCount(1);
                }

            }

            // Xử lý giai đoạn spawn gạch từng phần một
            if (levelTransition.getCurrentState() == LevelTransition.State.BRICK_SPAWN) {
                // Kiểm tra nếu không còn gạch để spawn
                if (entityManager.getStagingBricks().isEmpty()) {
                    levelTransition.finishTransition(); // Hoàn tất chuyển cảnh khi hết gạch để spawn
                } else {
                    // Spawn một số gạch mỗi khung hình
                    final int BRICKS_PER_FRAME = 2;
                    int bricksToSpawnThisFrame = Math.min(BRICKS_PER_FRAME, entityManager.getStagingBricks().size());

                    // Thêm gạch từ "cánh gà" vào trò chơi
                    for (int i = 0; i < bricksToSpawnThisFrame; i++) {
                        Brick brickToSpawn = entityManager.getStagingBricks().remove(0);
                        if (brickToSpawn instanceof LaserShooterBrick) {
                            // Thêm gạch bắn laser vào danh sách riêng
                            entityManager.addLaserShooter((LaserShooterBrick) brickToSpawn);
                        } else {
                            // Thêm gạch thông thường vào danh sách gạch
                            entityManager.getBricks().add(brickToSpawn);
                        }
                    }
                }
            }
            // Kiểm tra nếu quá trình chuyển cảnh đã hoàn tất
            if (!levelTransition.isTransitioning()) {
                setGameState("PLAYING");
            }

        } else if ("MENU".equals(gameState)) {
            menuManager.update();
        } else if ("GAME_OVER".equals(gameState)) {
            gameOverTimer--;
            if (gameOverTimer <= 0) setGameState("MENU");
        } else if ("GAME_WIN".equals(gameState)) {
            gameOverTimer--;
            if (gameOverTimer <= 0) setGameState("MENU");
        } else if ("HIGH_SCORES".equals(gameState)) {
            scoreManager.update();
        } else if ("SETTING".equals(gameState)) {
            settingManager.update();
        } else if ("LEVEL_SELECT".equals(gameState)) {
            selectLevel.update();
        }
    }

    /**
     * Cập nhật trạng thái trò chơi và xử lý các hành động liên quan.
     * @param state Trạng thái mới của trò chơi.
     */
    public void setGameState(String state) {
        if (this.gameState != null && this.gameState.equals(state)) return;
        String oldState = this.gameState;
        // Xử lý các hành động dựa trên trạng thái mới của trò chơi
        if (GAMESTATE_PAUSED.equals(state)) {
            soundManager.pauseBackgroundMusic();
        } else if ("PLAYING".equals(state) && GAMESTATE_PAUSED.equals(oldState)) {
            soundManager.resumeBackgroundMusic();
        }
        this.gameState = state;
        if ("MENU".equals(state)) {
            boolean wasInMenuScreens = "MENU".equals(oldState) || "SETTING".equals(oldState) || "HIGH_SCORES".equals(oldState) || "LEVEL_SELECT".equals(oldState);
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
                boolean didWin = "GAME_WIN".equals(state);
                scoreManager.submitSessionResult(playerStats.getFinalScore(), playerStats.getFinalPlaytimeMillis(), didWin);
            }
            SaveSystem.deleteSave(); // Xóa dữ liệu lưu khi kết thúc trò chơi
        }
    }

    /**
     * Kích hoạt một PowerUp mới, xử lý xung đột với các PowerUp hiện có.
     * @param newPowerUp PowerUp mới cần kích hoạt.
     */
    public void activatePowerUp(PowerUp newPowerUp) {
        String newType = newPowerUp.getType();
        // Loại bỏ các PowerUp xung đột
        Iterator<PowerUp> iterator = entityManager.getActivePowerUps().iterator();
        while (iterator.hasNext()) {
            PowerUp existingPowerUp = iterator.next();
            String existingType = existingPowerUp.getType();
            if (existingPowerUp.getType().equals(newType)) {
                // Xử lý xung đột cùng loại PowerUp
                existingPowerUp.removeEffect(this); // Loại bỏ hiệu ứng của PowerUp hiện có
                iterator.remove(); // Xóa PowerUp khỏi danh sách
            } else if ( (existingType.equals("fast_ball") && newType.equals("slow_ball")) || (existingType.equals("slow_ball") && newType.equals("fast_ball")) ) {
                // Xử lý xung đột giữa fast_ball và slow_ball
                existingPowerUp.removeEffect(this); // Loại bỏ hiệu ứng của PowerUp hiện có
                iterator.remove(); // Xóa PowerUp khỏi danh sách
            }
        }
        soundManager.playSound(SoundManager.SFX_POWERUP);
        entityManager.addActivePowerUp(newPowerUp); // Thêm PowerUp mới vào danh sách
        newPowerUp.applyEffect(this);
    }

    /**
     * Đảm bảo rằng paddle và ball đã sẵn sàng để chơi.
     * Nếu không, tạo mới chúng và đặt vào vị trí thích hợp.
     */
    private void ensurePaddleAndBallReadyForPlay() {
        int gameAreaWidth = ScalingManager.getInstance().GAME_AREA_WIDTH;
        int nativeHeight = ScalingManager.getInstance().NATIVE_HEIGHT;
        int currentBallSize = (entityManager.getBallSize() > 0) ? entityManager.getBallSize() : 18;
        boolean needRecreate = (entityManager.getPaddle() == null || entityManager.getBall() == null); // Kiểm tra nếu paddle hoặc ball bị null
        if (!needRecreate) {
            needRecreate = entityManager.getPaddle().getY() > nativeHeight; // Kiểm tra nếu paddle nằm ngoài màn hình
        }
        if (needRecreate) {
            // Tạo mới paddle và ball nếu cần
            int paddleWidth = 120;
            int px = (gameAreaWidth - paddleWidth) / 2;
            int py = nativeHeight - 80;
            Paddle newPaddle = new Paddle(px, py, paddleWidth, 30, settingManager.getSelectedPaddleSkinKey());
            Ball newBall = new Ball(px + (paddleWidth / 2) - (currentBallSize / 2), py - currentBallSize - 1, currentBallSize, currentBallSize, settingManager.getSelectedBallSkinKey());
            newBall.resetBallPosition(newPaddle);
            entityManager.setPaddle(newPaddle);
            entityManager.getBalls().clear();
            entityManager.addBall(newBall);
            entityManager.setBall(newBall);
            for (PowerUp p : entityManager.getActivePowerUps()) {
                p.removeEffect(this);
            }
            entityManager.getActivePowerUps().clear();
        }
    }

    /**
     * Đảm bảo rằng level hiện tại đã được nạp đầy đủ các thực thể.
     * Nếu chưa, tiến hành nạp lại các thực thể từ dữ liệu level.
     */
    private void ensureLevelHydrated() {
        // Lấy level hiện tại
        Level currentLevel = levelManager.getCurrentLevel();
        if (currentLevel == null) return;
        loadLevelAssets(currentLevel);

        // Kiểm tra nếu chưa có gạch và gạch bắn laser thì nạp lại level
        if (entityManager.getBricks().isEmpty() && entityManager.getLaserShooters().isEmpty()) {
            entityManager.hydrateLevel(currentLevel);
        }
    }

    /**
     * Khôi phục trạng thái trò chơi từ dữ liệu lưu trữ.
     * @param data Dữ liệu lưu trữ để khôi phục.
     */
    public void restoreFromSave(SaveData data) {
        if (data == null) return; // Kiểm tra dữ liệu lưu trữ hợp lệ

        // Tải level cụ thể từ dữ liệu lưu trữ
        if (getLevelManager() != null) {
            getLevelManager().loadSpecificLevel(data.getLevelIndex());
        }
        Level currentLevel = getLevelManager().getCurrentLevel();
        if (currentLevel == null) return;

        loadLevelAssets(currentLevel); // 1. Tải Assets
        entityManager.hydrateLevel(currentLevel); // 2. Tải Entities (để tạo Boss, gán ID)

        // 3. Lọc lại Entities dựa trên SaveData
        List<Integer> aliveList = (data.getAliveBrickIds() != null)
                ? data.getAliveBrickIds()
                : java.util.Collections.emptyList();

        // Hydrate lại trạng thái thực thể từ dữ liệu lưu trữ
        entityManager.hydrateFromSave(
                currentLevel,
                new java.util.HashSet<>(aliveList)
        );

        // 4. Khôi phục trạng thái paddle và ball
        playerStats.setSessionStats(
                data.getScore(),
                data.getLives(),
                data.getPlaytimeMillis(),
                data.getCurrentLevelPlaytimeMillis()
        );

        // Đặt lại vị trí paddle và ball theo dữ liệu lưu trữ
        this.canContinue = data.isCanContinue();
        if (menuManager != null) {
            menuManager.setContinueAvailable(this.canContinue);
        }
        setGameState("MENU");
    }

    public PlayerStats getPlayerStats() { return playerStats; }
    public LevelTransition getLevelTransition() { return levelTransition; }
    public LevelManager getLevelManager() { return levelManager; }
    public InputHandler getInputHandler() { return inputHandler; }
    public boolean canContinue() { return canContinue; }
    public void applyBallSkin(String selectedBallSkinKey) {
        entityManager.setBallSkin(selectedBallSkinKey);
    }
    public void applyPaddleSkin(String selectedPaddleSkinKey) {
        entityManager.setPaddleSkin(selectedPaddleSkinKey);
    }
    public String getSelectedBallSkinKey() { return settingManager.getSelectedBallSkinKey(); }
    public String getSelectedPaddleSkinKey() { return settingManager.getSelectedPaddleSkinKey(); }
    public GameSummaryPanel getGameSummaryPanel() { return gameSummaryPanel; }
    public Sidebar getUIManager() { return sidebar; }
    public MenuManager getMenuManager() { return menuManager; }
    public ScoreManager getScoreManager() { return scoreManager; }
    public SettingManager getSettingManager() { return settingManager; }
    public SelectLevel getSelectLevel() { return selectLevel; }
    public String getGameState() { return gameState; }
    public Image getCurrentBackground() { return this.currentBackground; }


    public List<Laser> getLasers() { return entityManager.getLasers(); }
    public List<LaserShooterBrick> getLaserShooters() { return entityManager.getLaserShooters(); }
    public Boss getBoss() { return entityManager.getBoss(); }
    public long getPlaytimeMillis() { return playerStats.getPlaytimeMillis(); }
    public long getCurrentLevelPlaytimeMillis() { return playerStats.getCurrentLevelPlaytimeMillis(); }
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
    public List<Brick> getBricks() { return entityManager.getBricks(); }
    public List<PowerUp> getPowerUps() { return entityManager.getPowerUps(); }
    public List<Shard> getActiveShards() { return entityManager.getActiveShards(); }
    public int getFinalScore() { return playerStats.getFinalScore(); }
    public long getFinalPlaytimeMillis() { return playerStats.getFinalPlaytimeMillis(); }
    public int getScreenHeight() { return ScalingManager.getInstance().NATIVE_HEIGHT; }
    public void addScore(int points) {
        playerStats.addScore(points);
    }

}