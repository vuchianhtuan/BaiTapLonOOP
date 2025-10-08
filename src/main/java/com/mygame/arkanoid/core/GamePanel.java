package com.mygame.arkanoid.core;

import com.mygame.arkanoid.engine.InputHandler;
import com.mygame.arkanoid.engine.Renderer;
import com.mygame.arkanoid.systems.MenuManager;

import javax.swing.JPanel;
import java.awt.Dimension;
import java.awt.Graphics;

public class GamePanel extends JPanel {

    public static final int WIDTH = 800;
    public static final int HEIGHT = 600;

    private final GameManager gameManager;
    private final Renderer renderer;

    public GamePanel(GameManager gameManager) {
        this.setPreferredSize(new Dimension(WIDTH, HEIGHT));
        this.setFocusable(true);

        this.gameManager = gameManager;
        this.renderer = new Renderer();
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
                    gameManager.getBricks(), gameManager.getPowerUps());
        } else if ("MENU".equals(currentState)) {
            gameManager.getMenuManager().render(g);
        }

        g.dispose();
    }
}