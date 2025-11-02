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

import java.awt.*;
import java.util.*;
import java.util.List;

public class GameManager {
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

    private static class Holder {
        private static final GameManager INSTANCE = new GameManager();
    }

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

    public static GameManager getInstance() { return Holder.INSTANCE; }
    private void loadThemeAssets(String prefix) { AssetManager.getInstance().loadTheme(prefix); }
    public void startGame() {
        playerStats.resetForNewGame();
        setupLevelObjects();
        canContinue = false;
        levelManager.reset();
        loadNextLevel();
    }
    public void startGameAtLevel(int levelIndex) {
        playerStats.resetForNewGame();
        canContinue = false;
        if (levelManager.loadSpecificLevel(levelIndex)) {
            setupLevelObjects();
            levelTransition.startInstantFade();
            setGameState("TRANSITION");
        } else {
            System.err.println("Lỗi: Không thể tải level tại index " + levelIndex);
            setGameState("MENU");
        }
    }
    public void continueGame() {
        playerStats.setLastUpdateTime(System.nanoTime());
        if (!canContinue) return;
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
    public void goToMenuAndEnableContinue() {
        canContinue = true;
        if (menuManager != null) menuManager.setContinueAvailable(true);
        setGameState("MENU");
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
        entityManager.resetForNewLevel(settingManager.getSelectedPaddleSkinKey(), settingManager.getSelectedBallSkinKey());
        for (PowerUp p : entityManager.getActivePowerUps()) {
            p.removeEffect(this);
        }
        entityManager.getActivePowerUps().clear();
    }

    private void loadLevelAssets(Level currentLevel) {
        String prefix = currentLevel.getThemeAssetPrefix();
        loadThemeAssets(prefix);

        String music = currentLevel.getThemeMusic();
        if (music != null && !music.isEmpty()) {
            soundManager.playBackgroundMusic(music);
        } else {
            soundManager.playBackgroundMusic("ExoticBaryon_PhaseXX.wav");
        }

        String bgName = currentLevel.getThemeBackground();
        this.currentBackground = AssetManager.getInstance().getBackgroundImage(bgName);
    }

    private void loadLevelSetup() {
        Level currentLevel = levelManager.getCurrentLevel();
        if (currentLevel == null) return;

        setupLevelObjects();            // 1. Dọn dẹp Player
        loadLevelAssets(currentLevel);       // 2. Tải Assets (Nhạc/Nền)
        entityManager.hydrateLevel(currentLevel); // 3. NẠP THỰC THỂ (Gạch/Boss)
    }


    // Hàm updateGame giữ nguyên
    public void updateGame() {
        long now = System.nanoTime();
        long deltaNanos = (playerStats.getLastUpdateTime() > 0) ? (now - playerStats.getLastUpdateTime()) : 0;
        long deltaMillis = deltaNanos / 1_000_000;
        playerStats.setLastUpdateTime(now);

        if ("PLAYING".equals(gameState)) {
            playerStats.updatePlaytime(deltaMillis);
            entityManager.updateAll(inputHandler);
            sidebar.update();

            Iterator<PowerUp> activePowerUpIterator = entityManager.getActivePowerUps().iterator();
            while (activePowerUpIterator.hasNext()) {
                PowerUp p = activePowerUpIterator.next();
                p.tick();
                if (p.isExpired()) {
                    p.removeEffect(this);
                    activePowerUpIterator.remove();
                }
            }

            collisionSystem.checkAllCollisions(this);

            List<Brick> newlyFinishedExplosions = new ArrayList<>();
            for (Brick brick : entityManager.getBricks()) {
                if (brick instanceof ExplosiveBrick && ((ExplosiveBrick) brick).isFinished()) {
                    newlyFinishedExplosions.add(brick);
                }
            }
            for (Brick sourceBrick : newlyFinishedExplosions) {
                if (sourceBrick instanceof ExplosiveBrick) {
                    getActiveShards().addAll(sourceBrick.shatter());
                }
                soundManager.playSound(SoundManager.SFX_EXPLOSION);
                explosionSystem.explode(sourceBrick, 50.0, this);
            }

            entityManager.cleanupDestroyedObjects(getScreenHeight());

            if (entityManager.isLevelWon()) {
                scoreManager.submitLevelResult(
                        levelManager.getCurrentLevelIndex(),
                        playerStats.getCurrentLevelScore(),
                        playerStats.getCurrentLevelPlaytimeMillis()
                );
                loadNextLevel();
                return;
            }

            if (entityManager.areBallsEmpty()) {
                playerStats.loseLife();
                if (playerStats.getLives() > 0) {
                    soundManager.playSound(SoundManager.SFX_BALL_LOSS);
                    entityManager.respawnBall(settingManager.getSelectedBallSkinKey());
                } else {
                    setGameState("GAME_OVER");
                    gameOverTimer = 360;
                }
            }

        } else if (GAMESTATE_PAUSED.equals(gameState)) {
            sidebar.update();

        } else if ("TRANSITION".equals(gameState)) {
            levelTransition.update();
            if (levelTransition.getCurrentState() == LevelTransition.State.FADE_FROM_BLACK) {
                if (levelTransition.getBrickSpawnCount() == 0) {

                    loadLevelSetup(); // <-- Vẫn gọi hàm tổng hợp

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

            if (levelTransition.getCurrentState() == LevelTransition.State.BRICK_SPAWN) {
                if (entityManager.getStagingBricks().isEmpty()) {
                    levelTransition.finishTransition();
                } else {
                    final int BRICKS_PER_FRAME = 2;
                    int bricksToSpawnThisFrame = Math.min(BRICKS_PER_FRAME, entityManager.getStagingBricks().size());

                    for (int i = 0; i < bricksToSpawnThisFrame; i++) {
                        Brick brickToSpawn = entityManager.getStagingBricks().remove(0);
                        if (brickToSpawn instanceof LaserShooterBrick) {
                            entityManager.addLaserShooter((LaserShooterBrick) brickToSpawn);
                        } else {
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
            SaveSystem.deleteSave();
        }
    }

    public void activatePowerUp(PowerUp newPowerUp) {
        String newType = newPowerUp.getType();
        Iterator<PowerUp> iterator = entityManager.getActivePowerUps().iterator();
        while (iterator.hasNext()) {
            PowerUp existingPowerUp = iterator.next();
            String existingType = existingPowerUp.getType();
            if (existingPowerUp.getType().equals(newType)) {
                existingPowerUp.removeEffect(this);
                iterator.remove();
            } else if ( (existingType.equals("fast_ball") && newType.equals("slow_ball")) || (existingType.equals("slow_ball") && newType.equals("fast_ball")) ) {
                existingPowerUp.removeEffect(this);
                iterator.remove();
            }
        }
        soundManager.playSound(SoundManager.SFX_POWERUP);
        entityManager.addActivePowerUp(newPowerUp);
        newPowerUp.applyEffect(this);
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

    private void ensureLevelHydrated() {
        Level currentLevel = levelManager.getCurrentLevel();
        if (currentLevel == null) return;
        loadLevelAssets(currentLevel);
        if (entityManager.getBricks().isEmpty() && entityManager.getLaserShooters().isEmpty()) {
            entityManager.hydrateLevel(currentLevel);
        }
    }

    public void restoreFromSave(SaveData data) {
        if (data == null) return;

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

        entityManager.hydrateFromSave(
                currentLevel,
                new java.util.HashSet<>(aliveList) // <-- Sửa lại
        );

        playerStats.setSessionStats(
                data.getScore(),
                data.getLives(),
                data.getPlaytimeMillis(),
                data.getCurrentLevelPlaytimeMillis()
        );
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