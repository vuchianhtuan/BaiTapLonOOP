package com.mygame.arkanoid.core;

// Đa luồng: GameLoop chạy update + render song song với GUI
public class GameLoop extends Thread {
    /**
     * Cờ để kiểm soát vòng lặp game.
     */
    private volatile boolean running = true;
    private final GameManager gameManager;
    private final GamePanel gamePanel;

    // Các hằng số để kiểm soát tốc độ khung hình ở mức 60 FPS
    private static final int TARGET_FPS = 60;
    private static final long OPTIMAL_TIME = 1000000000 / TARGET_FPS;

    public GameLoop(GameManager gameManager, GamePanel gamePanel) {
        this.gameManager = gameManager;
        this.gamePanel = gamePanel;
    }

    /**
     * Vòng lặp chính của game, chạy liên tục để cập nhật trạng thái game và vẽ lại màn hình.
     */
    @Override
    public void run() {
        long lastLoopTime = System.nanoTime();

        while (running) {
            long now = System.nanoTime();
            lastLoopTime = now;

            gameManager.updateGame();
            gamePanel.repaint();

            // 3. Tính toán thời gian ngủ để duy trì 60 FPS
            try {
                long sleepTime = (lastLoopTime - System.nanoTime() + OPTIMAL_TIME) / 1000000;
                if (sleepTime > 0) {
                    Thread.sleep(sleepTime);
                }
            } catch (InterruptedException e) {
                running = false;
                Thread.currentThread().interrupt();
            }
        }
    }

    public void stopLoop() {
        running = false;
    }
}