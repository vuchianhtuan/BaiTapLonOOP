package com.mygame.arkanoid.ui.hud;

import com.mygame.arkanoid.core.GameManager;
import com.mygame.arkanoid.engine.InputHandler;
import com.mygame.arkanoid.ui.controls.BackButton;
import com.mygame.arkanoid.systems.ScalingManager;

import java.awt.*;

import static com.mygame.arkanoid.core.GameManager.GAMESTATE_PAUSED;

public class Sidebar {
    private GameManager gameManager;
    private InputHandler inputHandler;
    private HeartUI heartUI;
    private ScoreUI scoreUI;

    // Các nút bấm và thành phần UI (đã di chuyển từ GameManager)
    private BackButton backButton;
    private Rectangle pauseButtonRect;
    private Rectangle resumeButtonRect;
    private Rectangle menuButtonRect;
    private boolean pauseCooldown = false; // Quản lý cooldown ngay tại đây

    public Sidebar(GameManager gm, InputHandler inputHandler) {
        this.gameManager = gm;
        this.inputHandler = inputHandler;
        this.heartUI = new HeartUI(gm);
        this.scoreUI = new ScoreUI(gm);

        // --- Di chuyển logic khởi tạo UI từ GameManager sang đây ---
        this.backButton = new BackButton(10, 10, 40, 40);

        int gameAreaWidth = ScalingManager.getInstance().GAME_AREA_WIDTH; // 960
        int sidebarWidth = ScalingManager.getInstance().NATIVE_WIDTH - gameAreaWidth; // 160
        int buttonLogicX = gameAreaWidth + (sidebarWidth - 120) / 2; // (960 + (160-120)/2) = 980
        int buttonWidth = 120;
        int buttonHeight = 40;
        int buttonLogicY_Pause = 650; // Vị trí nút Pause/Resume
        int buttonLogicY_Menu = 590;  // Vị trí nút Menu (cao hơn)

        pauseButtonRect = new Rectangle(buttonLogicX, buttonLogicY_Pause, buttonWidth, buttonHeight);
        resumeButtonRect = new Rectangle(buttonLogicX, buttonLogicY_Pause, buttonWidth, buttonHeight);
        menuButtonRect = new Rectangle(buttonLogicX, buttonLogicY_Menu, buttonWidth, buttonHeight);
    }

    /**
     * Hàm này sẽ được gọi BÊN TRONG GameManager.updateGame()
     * ở các state "PLAYING" và "PAUSED"
     */
    public void update() {
        // Chỉ cập nhật UI (như nhấp nháy, v.v.) nếu cần
        heartUI.update();
        scoreUI.update();

        // --- Di chuyển logic xử lý click từ GameManager sang đây ---
        ScalingManager sm = ScalingManager.getInstance();
        int screenMouseX = inputHandler.getMouseX();
        int screenMouseY = inputHandler.getMouseY();
        int mx = sm.unscaleX(screenMouseX);
        int my = sm.unscaleY(screenMouseY);

        String gameState = gameManager.getGameState();

        if ("PLAYING".equals(gameState)) {
            if (pauseButtonRect.contains(mx, my) && inputHandler.isMousePressed()) {
                if (!pauseCooldown) {
                    gameManager.setGameState(GAMESTATE_PAUSED);
                    pauseCooldown = true;
                }
                return; // Thoát sớm
            }

            if (menuButtonRect.contains(mx, my) && inputHandler.isMousePressed()) {
                if (!pauseCooldown) {
                    gameManager.goToMenuAndEnableContinue(); // Yêu cầu GameManager xử lý
                    pauseCooldown = true;
                    return; // Thoát sớm
                }
            }

            if (backButton.contains(mx, my) && inputHandler.isMousePressed()) {
                if (!pauseCooldown) {
                    gameManager.goToMenuAndEnableContinue(); // Yêu cầu GameManager xử lý
                    pauseCooldown = true;
                    return; // Thoát sớm
                }
            }

        } else if (GAMESTATE_PAUSED.equals(gameState)) {
            if (resumeButtonRect.contains(mx, my) && inputHandler.isMousePressed()) {
                if (!pauseCooldown) {
                    gameManager.setGameState("PLAYING");
                    gameManager.getPlayerStats().setLastUpdateTime(System.nanoTime());
                    pauseCooldown = true;
                }
            }

            if (menuButtonRect.contains(mx, my) && inputHandler.isMousePressed()) {
                if (!pauseCooldown) {
                    gameManager.goToMenuAndEnableContinue(); // Yêu cầu GameManager xử lý
                    return; // Thoát sớm
                }
            }
        }

        // Reset cooldown
        if (!inputHandler.isMousePressed()) {
            pauseCooldown = false;
        }
    }

    /**
     * Hàm này sẽ được gọi BÊN TRONG GamePanel.paintComponent()
     */
    public void draw(Graphics g) {
        // 1. Vẽ các thành phần UI cũ (Heart, Score)
        heartUI.draw(g);
        scoreUI.draw((Graphics2D) g);

        // 2. Vẽ các thành phần "Extras" (đã di chuyển từ GamePanel)
        drawSidebarExtras(g);

        // 3. Vẽ nút Back
        backButton.draw(g, ScalingManager.getInstance());
    }

    /**
     * Đây là hàm private, lấy logic từ GamePanel.drawSidebarExtras
     * Giờ đây nó là một phần của Sidebar
     */
    private void drawSidebarExtras(Graphics g) {
        ScalingManager sm = ScalingManager.getInstance();
        int logicX = 980; // 960 + 20 padding
        int currentY = 320;

        // 1. VẼ LEVEL
        Font titleFont = new Font("Arial", Font.BOLD, 24);
        Font scaledTitleFont = titleFont.deriveFont((float)(titleFont.getSize() * sm.getScale()));

        Font valueFont = new Font("Arial", Font.PLAIN, 22);
        Font scaledValueFont = valueFont.deriveFont((float)(valueFont.getSize() * sm.getScale()));

        g.setFont(scaledTitleFont);
        g.setColor(Color.WHITE);
        g.drawString("LEVEL", sm.scaleX(logicX), sm.scaleY(currentY));

        currentY += 30; // Tăng Y để vẽ giá trị

        // Lấy số level (index + 1)
        int levelIndex = gameManager.getLevelManager().getCurrentLevelIndex();
        String levelText = "N/A"; // Mặc định

        if (levelIndex == 2) { // Level 3 (với index 2) là màn Boss
            levelText = "Boss";
        } else if (levelIndex >= 0) { // Các level khác
            levelText = String.valueOf(levelIndex + 1);
        }

        g.setFont(scaledValueFont);
        g.drawString(levelText, sm.scaleX(logicX), sm.scaleY(currentY));

        currentY += 50; // Thêm khoảng cách trước khi vẽ TIME

        // 2. Vẽ Thời gian
        g.setFont(scaledTitleFont);
        g.setColor(Color.WHITE);
        g.drawString("TIME", sm.scaleX(logicX), sm.scaleY(currentY));

        currentY += 30;

        g.setFont(scaledValueFont); // Dùng font giá trị

        long totalSeconds = gameManager.getCurrentLevelPlaytimeMillis() / 1000;
        long minutes = totalSeconds / 60;
        long seconds = totalSeconds % 60;
        g.drawString(String.format("%02d:%02d", minutes, seconds), sm.scaleX(logicX), sm.scaleY(currentY));

        // 3. VẼ NÚT MENU
        g.setColor(Color.GRAY);
        g.fillRect(sm.scaleX(menuButtonRect.x), sm.scaleY(menuButtonRect.y),
                sm.scaleWidth(menuButtonRect.width), sm.scaleHeight(menuButtonRect.height));

        g.setColor(Color.WHITE);
        g.drawRect(sm.scaleX(menuButtonRect.x), sm.scaleY(menuButtonRect.y),
                sm.scaleWidth(menuButtonRect.width), sm.scaleHeight(menuButtonRect.height));

        g.setFont(scaledValueFont);
        FontMetrics fm = g.getFontMetrics();
        String menuText = "MENU";
        int textWidthScaled = fm.stringWidth(menuText);
        int textX_screen = sm.scaleX(menuButtonRect.x) + (sm.scaleWidth(menuButtonRect.width) - textWidthScaled) / 2;
        int textY_screen = sm.scaleY(menuButtonRect.y) + (sm.scaleHeight(menuButtonRect.height) - fm.getHeight()) / 2 + fm.getAscent();
        g.drawString(menuText, textX_screen, textY_screen);

        // 4. VẼ NÚT PAUSE/RESUME
        Rectangle buttonRect;
        String buttonText;
        if (GAMESTATE_PAUSED.equals(gameManager.getGameState())) {
            buttonRect = resumeButtonRect;
            buttonText = "RESUME";
        } else {
            buttonRect = pauseButtonRect;
            buttonText = "PAUSE";
        }

        g.setColor(Color.GRAY);
        g.fillRect(sm.scaleX(buttonRect.x), sm.scaleY(buttonRect.y),
                sm.scaleWidth(buttonRect.width), sm.scaleHeight(buttonRect.height));

        g.setColor(Color.WHITE);
        g.drawRect(sm.scaleX(buttonRect.x), sm.scaleY(buttonRect.y),
                sm.scaleWidth(buttonRect.width), sm.scaleHeight(buttonRect.height));

        textWidthScaled = fm.stringWidth(buttonText);
        textX_screen = sm.scaleX(buttonRect.x) + (sm.scaleWidth(buttonRect.width) - textWidthScaled) / 2;
        textY_screen = sm.scaleY(buttonRect.y) + (sm.scaleHeight(buttonRect.height) - fm.getHeight()) / 2 + fm.getAscent();
        g.drawString(buttonText, textX_screen, textY_screen);
    }

    // Thêm các getter này để GameManager không cần lưu trữ chúng
    public Rectangle getPauseButtonRect() { return pauseButtonRect; }
    public Rectangle getResumeButtonRect() { return resumeButtonRect; }
    public Rectangle getMenuButtonRect() { return menuButtonRect; }
    public BackButton getBackButton() { return backButton; }
}