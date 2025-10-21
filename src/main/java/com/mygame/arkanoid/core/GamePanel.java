package com.mygame.arkanoid.core;

import com.mygame.arkanoid.engine.InputHandler;
import com.mygame.arkanoid.engine.Renderer;
import com.mygame.arkanoid.systems.UIManager;

import javax.swing.JPanel;
import java.awt.*;

public class GamePanel extends JPanel {
    public static final int WIDTH = 1280;
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
        String currentState = gameManager.getGameState();

        if ("PLAYING".equals(currentState) || "GAME_OVER".equals(currentState)) {
            renderer.renderGame(g, gameManager.getPaddle(),
                    gameManager.getBall(),
                    gameManager.getBricks(),
                    gameManager.getPowerUps(),
                    gameManager.getBalls(),
                    gameManager.getBoss(),
                    gameManager.getLasers(),
                    gameManager.getLaserShooters(),
                    gameManager.getBackButton(),
                    gameManager.getCurrentBackground()
            );
            uiManager.draw(g);
        } else if ("MENU".equals(currentState)) {
            gameManager.getMenuManager().render(g);
        } else if ("HIGH_SCORES".equals(currentState)) {
            gameManager.getScoreManager().render(g);
        } else if ("SETUP".equals(currentState)) {
            gameManager.getSetupVolume().render(g);
        } else if ("LEVEL_SELECT".equals(currentState)) {
            gameManager.getSelectLevel().render(g);
        }
    }
}