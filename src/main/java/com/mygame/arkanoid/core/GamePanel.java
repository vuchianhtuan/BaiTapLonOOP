package com.mygame.arkanoid.core;

import com.mygame.arkanoid.engine.InputHandler;
import com.mygame.arkanoid.engine.Renderer;
import com.mygame.arkanoid.systems.MenuManager;
import com.mygame.arkanoid.systems.ScoreManager;
import com.mygame.arkanoid.systems.UIManager;

import javax.swing.JPanel;
import java.awt.*;

public class GamePanel extends JPanel {

    public static final int WIDTH = 800;
    public static final int HEIGHT = 600;

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

        this.addKeyListener(inputHandler);
        this.addMouseMotionListener(inputHandler);
        this.addMouseListener(inputHandler);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        String currentState = gameManager.getGameState();

        if ("PLAYING".equals(currentState)) {
            renderer.renderGame(g, gameManager.getPaddle(), gameManager.getBall(),
                    gameManager.getBricks(), gameManager.getPowerUps(), gameManager.getBalls());
            Graphics2D g2d = (Graphics2D) g;
            uiManager.draw(g2d);
        } else if ("MENU".equals(currentState)) {
            gameManager.getMenuManager().render(g);
        } else if ("HIGH_SCORES".equals(currentState)) {
            gameManager.getScoreManager().render(g);
        }

        g.dispose();
    }
}