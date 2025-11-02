package com.mygame.arkanoid.core;

import com.mygame.arkanoid.engine.InputHandler;
import com.mygame.arkanoid.engine.Renderer;
import com.mygame.arkanoid.systems.helper.ScalingManager;
import com.mygame.arkanoid.systems.level.LevelTransition;
import com.mygame.arkanoid.systems.objectsui.Sidebar;

import static com.mygame.arkanoid.core.GameManager.GAMESTATE_PAUSED;

import javax.swing.JPanel;
import java.awt.*;

public class GamePanel extends JPanel {
    public static final int WIDTH = 1120;
    public static final int HEIGHT = 720;

    private final GameManager gameManager;
    private final Sidebar sidebar;
    private final LevelTransition levelTransition;
    private final Renderer renderer;

    public GamePanel(GameManager gameManager) {
        this.setPreferredSize(new Dimension(WIDTH, HEIGHT));
        this.setFocusable(true);

        this.gameManager = gameManager;
        this.renderer = new Renderer();
        this.sidebar = gameManager.getUIManager();
        this.levelTransition = gameManager.getLevelTransition();
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
            sidebar.draw(g);
            levelTransition.render(g);
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
}