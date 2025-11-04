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
 * Lớp quản lý trò chơi chính (Singleton), đóng vai trò là "bộ não" trung tâm.
 * Chịu trách nhiệm quản lý máy trạng thái (state machine) của game
 * (ví dụ: MENU, PLAYING, PAUSED, TRANSITION) và điều phối tất cả
 * các hệ thống con (EntityManager, CollisionSystem, LevelManager, v.v.).
 */
public class GameManager {
    /**
     * Cờ cho phép người chơi "Tiếp tục" (Continue)
     * từ menu chính hay không.
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

    /**
     * Constructor private để thực thi Singleton.
     * Khởi tạo một lần tất cả các hệ thống quản lý chính.
     */
    private GameManager() {
        inputHandler = new InputHandler();
        entityManager = new EntityManager();
        this.soundManager = new SoundManager();
        playerStats = new PlayerStats();
        this.sidebar = new Sidebar(this, inputHandler);
        AssetManager.getInstance().loadGlobalAssets(); // Tải các tài sản chung
        levelManager = new LevelManager();
        levelManager.loadLevels(); // Tải thông tin tất cả các level
        this.levelTransition = new LevelTransition(this);
        menuManager = new MenuManager(this, inputHandler);
        this.gameSummaryPanel = new GameSummaryPanel();
        collisionSystem = new CollisionSystem();
        explosionSystem = new ExplosionSystem();
        scoreManager = new ScoreManager(this, inputHandler);
        settingManager = new SettingManager(inputHandler, this, soundManager);
        selectLevel = new SelectLevel(inputHandler, this);
        this.gameState = "MENU"; // Trạng thái bắt đầu game
        canContinue = false;
        soundManager.playBackgroundMusic("Menu.wav");
    }

    /**
     * Singleton Holder pattern (Initialization-on-demand holder idiom).
     * Đảm bảo khởi tạo an toàn, lười biếng (lazy) và thread-safe.
     */
    private static class Holder {
        private static final GameManager INSTANCE = new GameManager();
    }

    /**
     * Lấy instance duy nhất của GameManager.
     * @return Instance của GameManager.
     */
    public static GameManager getInstance() { return Holder.INSTANCE; }

    /** Tải các tài sản (assets) dành riêng cho một chủ đề (theme). */
    private void loadThemeAssets(String prefix) { AssetManager.getInstance().loadTheme(prefix); }

    /**
     * Bắt đầu một lượt chơi mới hoàn toàn (từ level 1).
     */
    public void startGame() {
        playerStats.resetForNewGame(); // Đặt lại điểm, mạng, thời gian
        setupLevelObjects(); // Dọn dẹp paddle/ball cũ (nếu có)
        canContinue = false;
        levelManager.reset(); // Đặt LevelManager về level đầu tiên
        loadNextLevel();
    }

    /**
     * Bắt đầu chơi từ một level được chỉ định (thường từ màn hình chọn level).
     * @param levelIndex Index của level muốn bắt đầu.
     */
    public void startGameAtLevel(int levelIndex) {
        playerStats.resetForNewGame();
        canContinue = false;

        if (levelManager.loadSpecificLevel(levelIndex)) {
            setupLevelObjects();
            levelTransition.startInstantFade(); // Chuyển cảnh tức thì
            setGameState("TRANSITION");
        } else {
            // Lỗi không mong muốn, quay về menu
            System.err.println("Lỗi: Không thể tải level tại index " + levelIndex);
            setGameState("MENU");
        }
    }

    /**
     * Tiếp tục trò chơi từ trạng thái đã tạm dừng (khi ở trong menu).
     */
    public void continueGame() {
        playerStats.setLastUpdateTime(System.nanoTime()); // Đặt lại đồng hồ delta time
        if (!canContinue) return; // Kiểm tra an toàn

        // Đảm bảo level được "nạp" (hydrate) lại các thực thể (gạch, boss)
        // phòng trường hợp trạng thái bị mất hoặc là game được load từ save.
        ensureLevelHydrated();
        // Đảm bảo paddle và ball ở trạng thái sẵn sàng chơi.
        ensurePaddleAndBallReadyForPlay();
        setGameState("PLAYING");

        // Phát lại nhạc nền của level
        var currentLevel = levelManager.getCurrentLevel();
        String music = (currentLevel != null) ? currentLevel.getThemeMusic() : null;
        if (music != null && !music.isEmpty()) {
            soundManager.playBackgroundMusic(music);
        } else {
            soundManager.playBackgroundMusic("Background_Music_Backup.wav");
        }
    }

    /**
     * Quay về menu chính và kích hoạt cờ 'canContinue'.
     * Tự động lưu trạng thái game hiện tại.
     */
    public void goToMenuAndEnableContinue() {
        canContinue = true;
        if (menuManager != null) menuManager.setContinueAvailable(true);
        setGameState("MENU");
        // Lưu trạng thái game để có thể "Continue" hoặc "Restore" sau này
        SaveSystem.save(SaveSystem.capture(this));
    }

    /**
     * Tải level tiếp theo.
     * Nếu không còn level, chuyển sang trạng thái "GAME_WIN".
     */
    private void loadNextLevel() {
        playerStats.resetForNextLevel(); // Đặt lại điểm/thời gian của level

        if (levelManager.loadNextLevel()) {
            // Nếu là level đầu tiên (index 0), chuyển cảnh tức thì.
            if (levelManager.getCurrentLevelIndex() == 0) {
                levelTransition.startInstantFade();
            } else {
                // Các level sau sẽ có hiệu ứng paddle bay đi
                levelTransition.startTransition(entityManager.getPaddle());
            }
            setGameState("TRANSITION");
        } else {
            // Đã hoàn thành tất cả các level
            setGameState("GAME_WIN");
        }
    }

    /**
     * Dọn dẹp và thiết lập lại các đối tượng của người chơi (paddle, ball)
     * và các hiệu ứng power-up đang hoạt động.
     */
    private void setupLevelObjects() {
        // Reset paddle/ball về skin đã chọn và vị trí ban đầu
        entityManager.resetForNewLevel(settingManager.getSelectedPaddleSkinKey(), settingManager.getSelectedBallSkinKey());

        // Gỡ bỏ hiệu ứng của tất cả các power-up đang hoạt động từ level trước
        for (PowerUp p : entityManager.getActivePowerUps()) {
            p.removeEffect(this);
        }
        entityManager.getActivePowerUps().clear(); // Xóa sạch danh sách
    }

    /**
     * Tải các tài nguyên (assets) dành riêng cho level hiện tại.
     * @param currentLevel Level hiện tại.
     */
    private void loadLevelAssets(Level currentLevel) {
        // 1. Tải các tài sản theo chủ đề (ví dụ: gạch, background)
        String prefix = currentLevel.getThemeAssetPrefix();
        loadThemeAssets(prefix);

        // 2. Tải nhạc nền
        String music = currentLevel.getThemeMusic();
        if (music != null && !music.isEmpty()) {
            soundManager.playBackgroundMusic(music);
        } else {
            // Nhạc nền dự phòng
            soundManager.playBackgroundMusic("ExoticBaryon_PhaseXX.wav");
        }

        // 3. Tải hình nền
        String bgName = currentLevel.getThemeBackground();
        this.currentBackground = AssetManager.getInstance().getBackgroundImage(bgName);
    }

    /**
     * Thực hiện chuỗi hành động cần thiết để thiết lập một level mới.
     * Thứ tự rất quan trọng.
     */
    private void loadLevelSetup() {
        Level currentLevel = levelManager.getCurrentLevel();
        if (currentLevel == null) return;

        // 1. Dọn dẹp trạng thái người chơi (paddle, ball, powerups)
        setupLevelObjects();
        // 2. Tải assets (âm nhạc, hình ảnh, skin gạch) cho level
        loadLevelAssets(currentLevel);
        // 3. Nạp (Hydrate) các thực thể (gạch, boss) từ định nghĩa level
        entityManager.hydrateLevel(currentLevel);
    }


    /**
     * Phương thức update chính, được gọi liên tục bởi GameLoop.
     * Hoạt động như một máy trạng thái (State Machine).
     */
    public void updateGame() {
        // Tính toán Delta Time (thời gian trôi qua giữa các frame)
        long now = System.nanoTime();
        long deltaNanos = (playerStats.getLastUpdateTime() > 0) ? (now - playerStats.getLastUpdateTime()) : 0;
        long deltaMillis = deltaNanos / 1_000_000;
        playerStats.setLastUpdateTime(now);

        // --- Cập nhật dựa trên trạng thái (GameState) ---

        if ("PLAYING".equals(gameState)) {
            playerStats.updatePlaytime(deltaMillis); // Cập nhật tổng thời gian chơi
            entityManager.updateAll(inputHandler); // Cập nhật paddle, ball, boss, power-ups
            sidebar.update(); // Cập nhật UI (điểm, mạng)

            // Cập nhật các PowerUp đang hoạt động và xóa nếu hết hạn
            Iterator<PowerUp> activePowerUpIterator = entityManager.getActivePowerUps().iterator();
            while (activePowerUpIterator.hasNext()) {
                PowerUp p = activePowerUpIterator.next();
                p.tick(); // Đếm ngược thời gian
                if (p.isExpired()) {
                    p.removeEffect(this); // Gỡ bỏ hiệu ứng
                    activePowerUpIterator.remove(); // Xóa khỏi danh sách
                }
            }

            // Kiểm tra va chạm
            collisionSystem.checkAllCollisions(this);

            // Xử lý các gạch nổ (ExplosiveBrick) đã nổ xong
            // (Sử dụng 2 vòng lặp để tránh ConcurrentModificationException)
            List<Brick> newlyFinishedExplosions = new ArrayList<>();
            for (Brick brick : entityManager.getBricks()) {
                if (brick instanceof ExplosiveBrick && ((ExplosiveBrick) brick).isFinished()) {
                    newlyFinishedExplosions.add(brick);
                }
            }

            // Kích hoạt vụ nổ (gây sát thương lan) và tạo mảnh vỡ
            for (Brick sourceBrick : newlyFinishedExplosions) {
                if (sourceBrick instanceof ExplosiveBrick) {
                    getActiveShards().addAll(sourceBrick.shatter()); // Tạo mảnh vỡ
                }
                soundManager.playSound(SoundManager.SFX_EXPLOSION);
                explosionSystem.explode(sourceBrick, 50.0, this); // Gây sát thương
            }

            // Dọn dẹp các thực thể đã bị đánh dấu "destroyed"
            entityManager.cleanupDestroyedObjects(getScreenHeight());

            // --- Kiểm tra điều kiện Thắng / Thua ---
            if (entityManager.isLevelWon()) {
                // Ghi lại kết quả level
                scoreManager.submitLevelResult(
                        levelManager.getCurrentLevelIndex(),
                        playerStats.getCurrentLevelScore(),
                        playerStats.getCurrentLevelPlaytimeMillis()
                );
                loadNextLevel(); // Tải level tiếp theo
                return; // Thoát khỏi hàm update ngay lập tức
            }

            if (entityManager.areBallsEmpty()) {
                // Người chơi mất bóng
                playerStats.loseLife();
                if (playerStats.getLives() > 0) {
                    // Còn mạng, hồi sinh bóng
                    soundManager.playSound(SoundManager.SFX_BALL_LOSS);
                    entityManager.respawnBall(settingManager.getSelectedBallSkinKey());
                } else {
                    // Hết mạng
                    setGameState("GAME_OVER");
                    gameOverTimer = GameConstants.GAME_OVER_TIMER_FRAMES;
                }
            }

        } else if (GAMESTATE_PAUSED.equals(gameState)) {
            sidebar.update(); // Chỉ cập nhật sidebar

        } else if ("TRANSITION".equals(gameState)) {
            // --- Xử lý logic chuyển cảnh (rất phức tạp) ---
            levelTransition.update();

            // Giai đoạn 1: Màn hình mờ dần (FADE_FROM_BLACK)
            if (levelTransition.getCurrentState() == LevelTransition.State.FADE_FROM_BLACK) {
                // Cờ 'brickSpawnCount == 0' đảm bảo logic này chỉ chạy 1 lần
                if (levelTransition.getBrickSpawnCount() == 0) {
                    // Tải tất cả assets và thực thể của level
                    loadLevelSetup();

                    // Cập nhật boss 1 lần để đảm bảo vị trí/trạng thái (nếu có)
                    if (entityManager.getBoss() != null) {
                        entityManager.getBoss().update();
                    }

                    // Dọn dẹp danh sách hiển thị
                    entityManager.getBricks().clear();
                    entityManager.getLaserShooters().clear();
                    entityManager.getStagingBricks().clear();

                    // Nạp gạch (thường + boss) vào "cánh gà" (staging)
                    // Chúng chưa được hiển thị, chỉ chờ để "spawn"
                    Level currentLevel = levelManager.getCurrentLevel();
                    if (currentLevel != null) {
                        entityManager.getStagingBricks().addAll(currentLevel.getBricks());
                    }
                    if (entityManager.getBoss() != null) {
                        entityManager.getStagingBricks().addAll(entityManager.getBoss().getBricks());
                    }

                    levelTransition.setBrickSpawnCount(1); // Đặt cờ
                }

            }

            // Giai đoạn 2: Gạch rơi xuống (BRICK_SPAWN)
            if (levelTransition.getCurrentState() == LevelTransition.State.BRICK_SPAWN) {
                if (entityManager.getStagingBricks().isEmpty()) {
                    // Không còn gạch trong "cánh gà"
                    levelTransition.finishTransition(); // Hoàn tất chuyển cảnh
                } else {
                    // Spawn từng cụm gạch mỗi frame
                    final int BRICKS_PER_FRAME = 2;
                    int bricksToSpawnThisFrame = Math.min(BRICKS_PER_FRAME, entityManager.getStagingBricks().size());

                    // Chuyển gạch từ "cánh gà" (staging) sang danh sách (active)
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

            // Khi Transition báo đã kết thúc, chuyển sang PLAYING
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
     * Đặt trạng thái mới cho game. Đây là nơi trung tâm để xử lý
     * logic khi "vào" (enter) và "thoát" (exit) một trạng thái.
     * @param state Trạng thái mới (ví dụ: "PLAYING", "MENU").
     */
    public void setGameState(String state) {
        // Guard clause: Không chạy lại logic nếu trạng thái không đổi
        if (this.gameState != null && this.gameState.equals(state)) return;

        String oldState = this.gameState;

        // Xử lý logic khi THOÁT trạng thái cũ hoặc VÀO trạng thái mới
        if (GAMESTATE_PAUSED.equals(state)) {
            soundManager.pauseBackgroundMusic(); // Vào PAUSED
        } else if ("PLAYING".equals(state) && GAMESTATE_PAUSED.equals(oldState)) {
            soundManager.resumeBackgroundMusic(); // Thoát PAUSED để vào PLAYING
        }

        this.gameState = state;

        if ("MENU".equals(state)) {
            // Logic phức tạp để không phát lại nhạc Menu
            // nếu chỉ chuyển đổi giữa các màn hình trong Menu (Setting, Score...)
            boolean wasInMenuScreens = "MENU".equals(oldState) || "SETTING".equals(oldState) || "HIGH_SCORES".equals(oldState) || "LEVEL_SELECT".equals(oldState);
            if (!wasInMenuScreens) {
                // Chỉ phát nhạc Menu nếu thoát ra từ PLAYING, PAUSED, GAME_OVER
                soundManager.playBackgroundMusic("Menu.wav");
            }
            if (menuManager != null) menuManager.setContinueAvailable(canContinue);

        } else if ("GAME_OVER".equals(state) || "GAME_WIN".equals(state)) {
            soundManager.stopBackgroundMusic();
            canContinue = false; // Không thể "Continue" sau khi game kết thúc
            if (menuManager != null) menuManager.setContinueAvailable(false);

            this.gameOverTimer = 480; // Tăng thời gian hiển thị màn hình kết quả
            playerStats.captureFinalStats(); // Chốt điểm số và thời gian

            // Nộp kết quả cuối cùng
            if (scoreManager != null) {
                boolean didWin = "GAME_WIN".equals(state);
                scoreManager.submitSessionResult(playerStats.getFinalScore(), playerStats.getFinalPlaytimeMillis(), didWin);
            }
            // Xóa file save vì lượt chơi đã kết thúc
            SaveSystem.deleteSave();
        }
    }

    /**
     * Kích hoạt một PowerUp. Xử lý logic xung đột
     * (ví dụ: Slow Ball và Fast Ball không thể cùng tồn tại).
     * @param newPowerUp PowerUp vừa được nhặt.
     */
    public void activatePowerUp(PowerUp newPowerUp) {
        String newType = newPowerUp.getType();

        // Duyệt qua các power-up đang hoạt động để gỡ bỏ các power-up xung đột
        Iterator<PowerUp> iterator = entityManager.getActivePowerUps().iterator();
        while (iterator.hasNext()) {
            PowerUp existingPowerUp = iterator.next();
            String existingType = existingPowerUp.getType();

            // 1. Xung đột cùng loại (ví dụ: nhặt Expand khi đang có Expand)
            // -> Gỡ bỏ cái cũ, áp dụng cái mới (làm mới thời gian)
            if (existingType.equals(newType)) {
                existingPowerUp.removeEffect(this);
                iterator.remove();
            }
            // 2. Xung đột khác loại (ví dụ: Fast vs Slow)
            else if ( (existingType.equals("fast_ball") && newType.equals("slow_ball")) ||
                    (existingType.equals("slow_ball") && newType.equals("fast_ball")) )
            {
                existingPowerUp.removeEffect(this);
                iterator.remove();
            }
        }
        soundManager.playSound(SoundManager.SFX_POWERUP);
        entityManager.addActivePowerUp(newPowerUp); // Thêm power-up mới
        newPowerUp.applyEffect(this); // Kích hoạt hiệu ứng
    }

    /**
     * Đảm bảo paddle và ball tồn tại và ở đúng vị trí khi
     * người chơi chọn "Continue Game".
     */
    private void ensurePaddleAndBallReadyForPlay() {
        int gameAreaWidth = ScalingManager.getInstance().GAME_AREA_WIDTH;
        int nativeHeight = ScalingManager.getInstance().NATIVE_HEIGHT;
        int currentBallSize = (entityManager.getBallSize() > 0) ? entityManager.getBallSize() : 18;

        // Kiểm tra xem có cần tạo lại paddle/ball không
        boolean needRecreate = (entityManager.getPaddle() == null || entityManager.getBall() == null);
        if (!needRecreate) {
            // Nếu paddle bị "lọt" ra ngoài màn hình (do lỗi hoặc chuyển cảnh)
            needRecreate = entityManager.getPaddle().getY() > nativeHeight;
        }

        if (needRecreate) {
            // Tạo lại paddle và ball ở vị trí mặc định
            int paddleWidth = 120;
            int px = (gameAreaWidth - paddleWidth) / 2;
            int py = nativeHeight - 80;
            Paddle newPaddle = new Paddle(px, py, paddleWidth, 30, settingManager.getSelectedPaddleSkinKey());
            Ball newBall = new Ball(px + (paddleWidth / 2) - (currentBallSize / 2), py - currentBallSize - 1, currentBallSize, currentBallSize, settingManager.getSelectedBallSkinKey());

            newBall.resetBallPosition(newPaddle); // Gắn bóng lên paddle
            entityManager.setPaddle(newPaddle);
            entityManager.getBalls().clear();
            entityManager.addBall(newBall);
            entityManager.setBall(newBall);

            // Xóa tất cả power-up đang hoạt động khi phải tạo lại paddle
            for (PowerUp p : entityManager.getActivePowerUps()) {
                p.removeEffect(this);
            }
            entityManager.getActivePowerUps().clear();
        }
    }

    /**
     * Đảm bảo level hiện tại đã được "nạp" (hydrated) các thực thể (gạch, boss).
     * Cần thiết khi "Continue Game" hoặc khôi phục từ save.
     */
    private void ensureLevelHydrated() {
        Level currentLevel = levelManager.getCurrentLevel();
        if (currentLevel == null) return;

        // Tải lại assets (nhạc, nền) vì có thể chúng đã bị dọn dẹp
        loadLevelAssets(currentLevel);

        // Nếu không có gạch (hoặc gạch laser),
        // nghĩa là level cần được nạp lại từ định nghĩa.
        if (entityManager.getBricks().isEmpty() && entityManager.getLaserShooters().isEmpty()) {
            entityManager.hydrateLevel(currentLevel);
        }
    }

    /**
     * Khôi phục trạng thái trò chơi từ một đối tượng SaveData.
     * @param data Dữ liệu đã lưu.
     */
    public void restoreFromSave(SaveData data) {
        if (data == null) return;

        // 1. Tải level cụ thể
        if (getLevelManager() != null) {
            getLevelManager().loadSpecificLevel(data.getLevelIndex());
        }
        Level currentLevel = getLevelManager().getCurrentLevel();
        if (currentLevel == null) return; // Không thể khôi phục nếu level không hợp lệ

        // 2. Tải Assets (nhạc, nền, theme gạch)
        loadLevelAssets(currentLevel);

        // 3. Nạp TẤT CẢ thực thể (gạch, boss) từ định nghĩa level
        // (Đây là trạng thái "sạch" của level)
        entityManager.hydrateLevel(currentLevel);

        // 4. Lọc lại thực thể dựa trên ID đã lưu
        List<Integer> aliveList = (data.getAliveBrickIds() != null)
                ? data.getAliveBrickIds()
                : java.util.Collections.emptyList();

        // Hydrate lại trạng thái (xóa các gạch đã bị phá)
        entityManager.hydrateFromSave(
                currentLevel,
                new java.util.HashSet<>(aliveList)
        );

        // 5. Khôi phục trạng thái người chơi
        playerStats.setSessionStats(
                data.getScore(),
                data.getLives(),
                data.getPlaytimeMillis(),
                data.getCurrentLevelPlaytimeMillis()
        );

        // 6. Đặt cờ "Continue" và chuyển về MENU
        this.canContinue = data.isCanContinue();
        if (menuManager != null) {
            menuManager.setContinueAvailable(this.canContinue);
        }
        setGameState("MENU"); // Luôn đưa người chơi về Menu sau khi load
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