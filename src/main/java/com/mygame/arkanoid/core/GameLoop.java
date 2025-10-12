package com.mygame.arkanoid.core;
import com.mygame.arkanoid.engine.Renderer;

// Đa luồng: GameLoop chạy update + render song song với GUI
public class GameLoop extends Thread {
    private volatile boolean running = true;
    private final GameManager gameManager;
    private final GamePanel gamePanel;

    /*public GameLoop(GameManager gameManager, Renderer renderer) {
        this.gameManager = gameManager;
        this.renderer = renderer;
    }*/

    public GameLoop(GameManager gameManager, GamePanel gamePanel) {
        this.gameManager = gameManager;
        this.gamePanel = gamePanel;
    }

    @Override
    public void run() {
        while (running) {
            gameManager.updateGame();
            gamePanel.repaint();
            try {
                Thread.sleep(16); // ~60 FPS
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void stopLoop() {
        running = false;
    }
}