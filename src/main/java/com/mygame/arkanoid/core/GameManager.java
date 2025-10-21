package com.mygame.arkanoid.core;

import com.mygame.arkanoid.engine.AssetManager;
import com.mygame.arkanoid.objects.*;
import com.mygame.arkanoid.objects.bricks.Brick;
import com.mygame.arkanoid.systems.*;
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
    private List<Ball> balls = new ArrayList<>(); // Danh sách các quả bóng (nếu có Multi-Ball)
    private List<Brick> bricks;
    private List<PowerUp> powerUps;
    private List<PowerUp> activePowerUps;
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
    private SoundManager soundManager;
    private InputHandler inputHandler;
    private MenuManager menuManager;
    private SetupVolume setupVolume;
    private SelectLevel selectLevel;
    private String currentTheme = "";
    private Image currentBackground = null;
    int screenHeight = com.mygame.arkanoid.systems.ScalingManager.getInstance().NATIVE_HEIGHT;

    public GameManager() {
        inputHandler = new InputHandler();
        bricks = new ArrayList<>();
        laserShooters = new ArrayList<>();
        powerUps = new ArrayList<>();
        activePowerUps = new ArrayList<>();
        track = new Track(400, 130, 300, 20);
        thumb = new Thumb(400, 120, 40, 40);
        backButton = new BackButton(10, 10, 40, 40);
        this.soundManager = new SoundManager();
        this.hearts = new ArrayList<>();
        loadAssets();

        levelManager = new LevelManager();
        this.lasers = new ArrayList<>();
        levelManager.loadLevels();
        menuManager = new MenuManager(this, inputHandler);
        scoreManager = new ScoreManager(this, inputHandler);
        setupVolume = new SetupVolume(inputHandler, this, soundManager, track, thumb);
        selectLevel = new SelectLevel(inputHandler, this, levelManager);

        // Đặt trạng thái ban đầu của game là MENU
        this.gameState = "MENU";
        canContinue = false;
        soundManager.playBackgroundMusic("RegressiveTrip_Release.wav");
    }

    public void loadAssets() {
        /*
        AssetManager.getInstance().loadImage("normalBrick", "/images/button_blue.png");
        AssetManager.getInstance().loadImage("ball", "/images/ball.png");
        AssetManager.getInstance().loadImage("paddle", "/images/button_yellow.png");
        AssetManager.getInstance().loadImage("expandPowerUp", "/images/hole_small_end.png");
        AssetManager.getInstance().loadImage("menuBackground", "/images/backGroundMenu.png");
        AssetManager.getInstance().loadImage("explosiveBrick", "/images/button_grey.png");
        AssetManager.getInstance().loadImage("stickyPowerUp", "/images/star.png");
        AssetManager.getInstance().loadImage("slowBallPowerUp", "/images/ball_blue_large.png");
        AssetManager.getInstance().loadImage("strongBrick", "/images/strongbrick.png");
        AssetManager.getInstance().loadImage("strongBrick1", "/images/strongbrick1.png");
        AssetManager.getInstance().loadImage("strongBrick2", "/images/strongbrick2.png");
        AssetManager.getInstance().loadImage("laserShooter", "/images/laser_shooter.png");

        AssetManager.getInstance().loadImage("fastBallPowerUp", "/images/ball.png");
        AssetManager.getInstance().loadImage("extraLifePowerUp", "/images/heart.png");
        AssetManager.getInstance().loadImage("multiBallPowerUp", "/images/hole_start.png");
        AssetManager.getInstance().loadImage("heart", "/images/heart.png");
        AssetManager.getInstance().loadImage("gameover1", "/images/gameover1.png");
        AssetManager.getInstance().loadImage("gameover2", "/images/gameover2.png");
        AssetManager.getInstance().loadImage("gameover3", "/images/gameover3.png");
        AssetManager.getInstance().loadImage("scoreBackground", "/images/arkanoid_Background.png");
        AssetManager.getInstance().loadImage("laser", "/images/laser.png");
         */

        AssetManager am = AssetManager.getInstance();

        // Menu, UI, Game Over
        am.loadImage("menuBackground", "/images/backGroundMenu.png");
        am.loadImage("defaultBackground", "/images/default_game_background.png");
        am.loadImage("heart", "/images/heart.png");
        am.loadImage("gameover1", "/images/gameover1.png");
        am.loadImage("gameover2", "/images/gameover2.png");
        am.loadImage("gameover3", "/images/gameover3.png");
        am.loadImage("scoreBackground", "/images/arkanoid_Background.png");
        am.loadImage("setupVolumeBackground", "/images/arkanoid_Background.png");
        am.loadImage("selectLevelBackground", "/images/arkanoid_Background.png");

        // Power-ups (thường là chung)
        am.loadImage("expandPowerUp", "/images/expandPowerUp.png");
        am.loadImage("stickyPowerUp", "/images/stickyPowerUp.png");
        am.loadImage("slowBallPowerUp", "/images/slowBallPowerUp.png");
        am.loadImage("fastBallPowerUp", "/images/fastBallPowerUp.png");
        am.loadImage("extraLifePowerUp", "/images/heart.png");
        am.loadImage("multiBallPowerUp", "/images/multiBallPowerUp.png");
        am.loadImage("thumb", "/images/ball_blue_large_alt.png");
        am.loadImage("track", "/images/paddle.png");
        am.loadImage("Back", "/images/number_cross.png");

        // Laser
        am.loadImage("laser", "/images/laser.png");
        am.loadImage("laserShooter", "/images/laser_shooter.png");
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
        this.lives = 3;
        this.score = 0;
        canContinue = false;
        levelManager.reset(); // Đưa level manager về màn 1
        loadNextLevel();
    }

    public void startGameAtLevel(int levelIndex) {
        this.lives = 3;
        this.score = 0;
        canContinue = false;
        levelManager.setCurrentLevel(levelIndex);
        loadNextLevel();
    }

    public void continueGame() {
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

    private void loadNextLevel() {
        if (levelManager.loadNextLevel()) {
            // Tải level thành công, thiết lập màn chơi
            loadLevelSetup();
        } else {
            // Xử lý khi người chơi đã thắng tất cả các màn
            // Ví dụ: hiển thị màn hình chiến thắng hoặc quay về menu
            setGameState("GAME_WIN"); // Cần tạo thêm trạng thái này hoặc quay về MENU
        }
    }

    private void loadLevelSetup() {
        paddle = new Paddle(580, 670, 120, 18);
        ball = new Ball(634, 652, 12, 12);
        ball.resetBallPosition(paddle);
        balls.clear();
        balls.add(ball);
        powerUps.clear();
        lasers.clear();
        this.boss = null;
        bricks.clear();
        laserShooters.clear();

        for (PowerUp p : activePowerUps) {
            p.removeEffect(this);
        }
        activePowerUps.clear();

        Level currentLevel = levelManager.getCurrentLevel();
        if (currentLevel != null) {
            String prefix = currentLevel.getThemeAssetPrefix();
            loadThemeAssets(prefix);
            String music = currentLevel.getThemeMusic();
            if (music != null && !music.isEmpty()) {
                soundManager.playBackgroundMusic(music);
            } else {
                soundManager.playBackgroundMusic("ExoticBaryon_PhaseXX.wav");
            }

            String bgName = currentLevel.getThemeBackground();
            if (bgName != null && !bgName.isEmpty()) {
                // Tải ảnh nền riêng của màn
                String assetKey = "bg_" + bgName; // Tạo key duy nhất, ví dụ "bg_background_ice.png"
                AssetManager.getInstance().loadImage(assetKey, "/images/" + bgName);
                this.currentBackground = AssetManager.getInstance().getImage(assetKey);
            } else {
                // Tải ảnh nền mặc định
                this.currentBackground = AssetManager.getInstance().getImage("defaultBackground");
            }

            for (Brick brick : currentLevel.getBricks()) {
                if (brick instanceof LaserShooterBrick) {
                    laserShooters.add((LaserShooterBrick) brick);
                } else {
                    bricks.add(brick);
                }
            }

            if (currentLevel.isBossLevel() && !currentLevel.getBossBricks().isEmpty()) {
                java.awt.Rectangle bossBounds = currentLevel.getBossInitialBounds();

                float startX = (GamePanel.WIDTH / 2.0f) - (bossBounds.width / 2.0f);
                float startY = bossBounds.y;

                this.boss = new Boss(currentLevel.getBossBricks(), startX, startY, bossBounds.x, GamePanel.WIDTH);
                // Thêm các shooter của boss vào danh sách quản lý
                for(Brick bossBrick : boss.getBricks()) {
                    if (bossBrick instanceof LaserShooterBrick) {
                        laserShooters.add((LaserShooterBrick) bossBrick);
                    }
                }
            }
        }
        setGameState("PLAYING");
    }

    private void explode(Brick sourceBrick, double radius) {
        int sourceCenterX = sourceBrick.getX() + sourceBrick.getWidth() / 2;
        int sourceCenterY = sourceBrick.getY() + sourceBrick.getHeight() / 2;
        List<Brick> allActiveBricks = new ArrayList<>();

        allActiveBricks.addAll(this.laserShooters);

        allActiveBricks.addAll(this.bricks);

        if (boss != null) {
            for (Brick bossBrick : boss.getBricks()) {
                if (!(bossBrick instanceof LaserShooterBrick)) {
                    allActiveBricks.add(bossBrick);
                }
            }
        }

        for (Brick otherBrick : allActiveBricks) {
            if (otherBrick == sourceBrick || otherBrick.isDestroyed()) {
                continue;
            }

            int otherCenterX = otherBrick.getX() + otherBrick.getWidth() / 2;
            int otherCenterY = otherBrick.getY() + otherBrick.getHeight() / 2;
            double distance = Math.sqrt(Math.pow(sourceCenterX - otherCenterX, 2) + Math.pow(sourceCenterY - otherCenterY, 2));

            if (distance <= radius) {
                while (!otherBrick.isDestroyed()) {
                    otherBrick.takeHit();
                }
                score += 10;
            }
        }
    }

    public void updateGame() {
        if ("PLAYING".equals(gameState)) {
            boolean esc = inputHandler.isKeyDown(java.awt.event.KeyEvent.VK_ESCAPE);
            int mx = inputHandler.getMouseX();
            int my = inputHandler.getMouseY();

            if (esc || backButton.contains(mx, my) && inputHandler.isMousePressed()) {

                canContinue = true;
                if (menuManager != null) menuManager.setContinueAvailable(true);
                setGameState("MENU");
                return;
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
            if (boss != null) {
                for (Brick bossBrick : boss.getBricks()) {
                    if (!(bossBrick instanceof LaserShooterBrick)) {
                        allTargets.add(bossBrick);
                    }
                }
            }

            //Duyệt từng quả bóng tương tác với bricks
            for (Ball b : balls) {
                Iterator<Brick> targetIterator = allTargets.iterator();
                while (targetIterator.hasNext()) {
                    Brick target = targetIterator.next();

                    // Chỉ kiểm tra va chạm với những viên gạch chưa bị phá hủy
                    if (!target.isDestroyed() && b.checkCollision(target)) {
                        target.takeHit(); // Gạch nhận sát thương
                        score += 10;
                        b.bounceOff(target);

                        // Kiểm tra ngay sau khi nhận sát thương, nếu gạch bị phá hủy thì xóa nó
                        if (target.isDestroyed()) {
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
                            if (target instanceof ExplosiveBrick) {
                                explode(target, 100.0);
                            }
                        }
                        break;
                    }
                }
            }

            if (boss != null) {
                boss.removeDestroyedBricks();
                bricks.removeIf(Brick::isDestroyed);
                laserShooters.removeIf(Brick::isDestroyed);
                if (boss.isDefeated() && bricks.isEmpty()) {
                    loadNextLevel(); // Thắng boss -> chuyển màn
                }
            } else {
                bricks.removeIf(Brick::isDestroyed);
                laserShooters.removeIf(Brick::isDestroyed);
                if (bricks.isEmpty()) {
                    loadNextLevel(); // Hết gạch màn thường -> chuyển màn
                }
            }

            // Xử lý khi bóng rơi xuống đất.
            if (ball.getY() > screenHeight) {
                lives--;
                if (lives > 0) {
                    ball.resetBallPosition(paddle);
                } else {
                    setGameState("GAME_OVER");
                    gameOverTimer = 360;
                }
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
        this.gameState = state;

        if ("MENU".equals(state)) {
            soundManager.playBackgroundMusic("RegressiveTrip_Release.wav");
            //currentTheme = "";
            //this.currentBackground = null;
            // Khi vào MENU không tự reset canContinue; ESC đã gán đúng ở trên.
            if (menuManager != null) menuManager.setContinueAvailable(canContinue);
        } else if ("GAME_OVER".equals(state) || "GAME_WIN".equals(state)) {
            soundManager.stopBackgroundMusic();
            canContinue = false; // không thể continue sau khi game over/win
            if (menuManager != null) menuManager.setContinueAvailable(false);

            if (scoreManager != null) {
                scoreManager.submitScore(score);
            }
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
}

