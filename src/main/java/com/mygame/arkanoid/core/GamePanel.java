package com.mygame.arkanoid.core;

import com.mygame.arkanoid.engine.InputHandler;
import com.mygame.arkanoid.engine.Renderer;
import com.mygame.arkanoid.systems.helper.ScalingManager;
import com.mygame.arkanoid.systems.objectsui.UIManager;

import static com.mygame.arkanoid.core.GameManager.GAMESTATE_PAUSED;

import javax.swing.JPanel;
import java.awt.*;

public class GamePanel extends JPanel {
    public static final int WIDTH = 1120;
    public static final int HEIGHT = 720;

    private final GameManager gameManager;
    private final UIManager uiManager;
    private final Renderer renderer;

    public GamePanel(GameManager gameManager, UIManager uiManager) {
        this.setPreferredSize(new Dimension(WIDTH, HEIGHT));
        this.setFocusable(true);

        this.gameManager = gameManager;
        this.renderer = new Renderer();
        this.uiManager = uiManager;
        InputHandler inputHandler = gameManager.getInputHandler();

        // Thêm các trình nghe sự kiện input
        this.addKeyListener(inputHandler);
        this.addMouseMotionListener(inputHandler);
        this.addMouseListener(inputHandler);

    }

    /**
     * Phương thức này được khôi phục lại để xử lý tất cả việc vẽ.
     * Đây là cách làm tiêu chuẩn và ổn định của Swing.
     */
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        ScalingManager sm = ScalingManager.getInstance();
        sm.update(getWidth(), getHeight());
        String currentState = gameManager.getGameState();
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, getWidth(), getHeight());

        if ("PLAYING".equals(currentState) || GAMESTATE_PAUSED.equals(currentState)
                || "GAME_OVER".equals(currentState) || "TRANSITION".equals(currentState)
                || "GAME_WIN".equals(currentState)) {
            Image bg = gameManager.getCurrentBackground();
            if (bg != null) {
                g.drawImage(bg,
                        sm.scaleX(0), sm.scaleY(0),
                        sm.scaleWidth(sm.GAME_AREA_WIDTH), // <-- Chỉ rộng 960px
                        sm.scaleHeight(sm.NATIVE_HEIGHT),
                        null);
            } else {
                g.setColor(Color.BLACK);
                g.fillRect(sm.scaleX(0), sm.scaleY(0),
                        sm.scaleWidth(sm.GAME_AREA_WIDTH),
                        sm.scaleHeight(sm.NATIVE_HEIGHT));
            }

            // 3. Vẽ Nền Sidebar
            g.setColor(new Color(30, 30, 30)); // Màu xám tối
            g.fillRect(sm.scaleX(sm.GAME_AREA_WIDTH), sm.scaleY(0), // <-- Bắt đầu từ 960px
                    sm.scaleWidth(sm.NATIVE_WIDTH - sm.GAME_AREA_WIDTH),
                    sm.scaleHeight(sm.NATIVE_HEIGHT));

            renderer.renderGame(g, gameManager.getPaddle(),
                    gameManager.getBall(),
                    gameManager.getBricks(),
                    gameManager.getPowerUps(),
                    gameManager.getBalls(),
                    gameManager.getBoss(),
                    gameManager.getLasers(),
                    gameManager.getLaserShooters(),
                    gameManager.getCurrentBackground(),
                    gameManager.getActiveShards());
            uiManager.draw(g);
            drawSidebarExtras(g, sm);
            gameManager.getLevelTransition().render(g);
            if ("GAME_WIN".equals(currentState)) {
                Color overlayColor = new Color(0, 0, 0, 128); // 50% mờ
                g.setColor(overlayColor);
                g.fillRect(sm.scaleX(0), sm.scaleY(0),
                        sm.scaleWidth(sm.NATIVE_WIDTH),
                        sm.scaleHeight(sm.NATIVE_HEIGHT));

                // 2. Vẽ bảng thống kê
                gameManager.getGameSummaryPanel().draw(g,
                        "YOU WIN!",
                        gameManager.getFinalScore(),
                        gameManager.getFinalPlaytimeMillis()
                );

            } else if ("GAME_OVER".equals(currentState)) {

                Color overlayColor = new Color(0, 0, 0, 128); // 50% mờ
                g.setColor(overlayColor);
                g.fillRect(sm.scaleX(0), sm.scaleY(0),
                        sm.scaleWidth(sm.NATIVE_WIDTH),
                        sm.scaleHeight(sm.NATIVE_HEIGHT));

                gameManager.getGameSummaryPanel().draw(g,
                        "GAME OVER",
                        gameManager.getFinalScore(),
                        gameManager.getFinalPlaytimeMillis()
                );
            }
        } else if ("MENU".equals(currentState)) {
            gameManager.getMenuManager().render(g);
        } else if ("HIGH_SCORES".equals(currentState)) {
            gameManager.getScoreManager().render(g);
        } else if ("SETTING".equals(currentState)) {
            gameManager.getSettingManager().render(g);
        } else if ("LEVEL_SELECT".equals(currentState)) {
            gameManager.getSelectLevel().render(g);
        }
    }

    /**
     * Hàm này vẽ các phần tử sidebar không được UIManager quản lý
     */
    private void drawSidebarExtras(Graphics g, ScalingManager sm) {
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

        // --- KẾT THÚC THÊM MỚI ---

        // 2. Vẽ Thời gian (sử dụng lại font từ trên)
        g.setFont(scaledTitleFont);
        g.setColor(Color.WHITE);
        g.drawString("TIME", sm.scaleX(logicX), sm.scaleY(currentY));

        currentY += 30;

        g.setFont(scaledValueFont); // Dùng font giá trị

        long totalSeconds = gameManager.getCurrentLevelPlaytimeMillis() / 1000;
        long minutes = totalSeconds / 60;
        long seconds = totalSeconds % 60;
        g.drawString(String.format("%02d:%02d", minutes, seconds), sm.scaleX(logicX), sm.scaleY(currentY));


        // 3. VẼ NÚT MENU (Giữ nguyên logic cũ)
        Rectangle menuRect = gameManager.getMenuButtonRect();
        String menuText = "MENU";

        g.setColor(Color.GRAY);
        g.fillRect(sm.scaleX(menuRect.x), sm.scaleY(menuRect.y),
                sm.scaleWidth(menuRect.width), sm.scaleHeight(menuRect.height));

        g.setColor(Color.WHITE);
        g.drawRect(sm.scaleX(menuRect.x), sm.scaleY(menuRect.y),
                sm.scaleWidth(menuRect.width), sm.scaleHeight(menuRect.height));

        // Phải đặt lại font trước khi đo, vì nó có thể đã bị thay đổi
        g.setFont(scaledValueFont);
        FontMetrics fm = g.getFontMetrics(); // Lấy metrics của scaledValueFont
        int textWidthScaled = fm.stringWidth(menuText);
        int textX_screen = sm.scaleX(menuRect.x) + (sm.scaleWidth(menuRect.width) - textWidthScaled) / 2;
        int textY_screen = sm.scaleY(menuRect.y) + (sm.scaleHeight(menuRect.height) - fm.getHeight()) / 2 + fm.getAscent();

        g.drawString(menuText, textX_screen, textY_screen);


        // 4. VẼ NÚT PAUSE/RESUME (Giữ nguyên logic cũ)
        Rectangle buttonRect;
        String buttonText;
        if (GAMESTATE_PAUSED.equals(gameManager.getGameState())) {
            buttonRect = gameManager.getResumeButtonRect();
            buttonText = "RESUME";
        } else {
            buttonRect = gameManager.getPauseButtonRect();
            buttonText = "PAUSE";
        }

        g.setColor(Color.GRAY);
        g.fillRect(sm.scaleX(buttonRect.x), sm.scaleY(buttonRect.y),
                sm.scaleWidth(buttonRect.width), sm.scaleHeight(buttonRect.height));

        g.setColor(Color.WHITE);
        g.drawRect(sm.scaleX(buttonRect.x), sm.scaleY(buttonRect.y),
                sm.scaleWidth(buttonRect.width), sm.scaleHeight(buttonRect.height));

        // (Font và FontMetrics vẫn là scaledValueFont, không cần set lại)
        textWidthScaled = fm.stringWidth(buttonText);
        textX_screen = sm.scaleX(buttonRect.x) + (sm.scaleWidth(buttonRect.width) - textWidthScaled) / 2;
        textY_screen = sm.scaleY(buttonRect.y) + (sm.scaleHeight(buttonRect.height) - fm.getHeight()) / 2 + fm.getAscent();

        g.drawString(buttonText, textX_screen, textY_screen);
    }
}