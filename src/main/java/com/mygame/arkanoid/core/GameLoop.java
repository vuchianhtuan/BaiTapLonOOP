package com.mygame.arkanoid.core;

/**
 * Quản lý vòng lặp chính của game trên một luồng riêng.
 * Chịu trách nhiệm cập nhật (update) và vẽ lại (render) liên tục để duy trì FPS.
 */
public class GameLoop extends Thread {
    /**
     * Cờ để kiểm soát vòng lặp game.
     */
    private volatile boolean running = true;

    /**
     * Đối tượng quản lý game và bảng điều khiển game.
     */
    private final GameManager gameManager;

    /**
     * Bảng điều khiển game.
     */
    private final GamePanel gamePanel;

    /**
     * Mục tiêu FPS và thời gian tối ưu giữa các khung hình (nanoseconds).
     */
    private static final int TARGET_FPS = 60;
    private static final long OPTIMAL_TIME = 1000000000 / TARGET_FPS;

    /**
     * Khởi tạo vòng lặp game với GameManager và GamePanel.
     * @param gameManager
     * @param gamePanel
     */
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

            // Tính toán thời gian ngủ để duy trì 60 FPS
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

    /**
        * Dừng vòng lặp game.
        */
    public void stopLoop() {
        running = false;
    }
}