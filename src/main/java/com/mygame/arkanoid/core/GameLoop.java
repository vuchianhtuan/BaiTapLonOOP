package com.mygame.arkanoid.core;

/**
 * Quản lý vòng lặp (game loop) trên một luồng (Thread) riêng.
 * Lớp này chịu trách nhiệm gọi `update` (logic) và `repaint` (vẽ)
 * liên tục để duy trì một FPS (Frames Per Second) mục tiêu.
 */
public class GameLoop extends Thread {

    /**
     * Cờ kiểm soát vòng lặp.
     * Dùng 'volatile' để đảm bảo tính hiển thị (visibility)
     * khi luồng khác (ví dụ: luồng UI) gọi stopLoop().
     */
    private volatile boolean running = true;

    private final GameManager gameManager;
    private final GamePanel gamePanel;

    private static final int TARGET_FPS = 60;

    /** * Thời gian tối ưu cho mỗi khung hình (tính bằng nano giây).
     * 1 giây = 1,000,000,000 nano giây.
     * (1 giây / 60 FPS) ≈ 16.67ms (16,666,666 nano giây).
     */
    private static final long OPTIMAL_TIME = 1000000000 / TARGET_FPS;

    /**
     * Khởi tạo vòng lặp game với các thành phần cốt lõi.
     * @param gameManager Đối tượng quản lý logic game (update).
     * @param gamePanel Bảng (panel) để vẽ game lên (repaint).
     */
    public GameLoop(GameManager gameManager, GamePanel gamePanel) {
        this.gameManager = gameManager;
        this.gamePanel = gamePanel;
    }

    /**
     * Vòng lặp game chính. Liên tục cập nhật logic và yêu cầu vẽ lại.
     */
    @Override
    public void run() {
        // Biến này lưu thời điểm BẮT ĐẦU của frame hiện tại
        // để tính toán thời gian sleep.
        long lastLoopTime = System.nanoTime();

        while (running) {
            long now = System.nanoTime();
            // Ghi lại thời điểm bắt đầu của frame hiện tại
            lastLoopTime = now;

            // 1. Cập nhật logic game (vật lý, trạng thái, v.v.)
            gameManager.updateGame();

            // 2. Yêu cầu hệ thống vẽ lại.
            // (Việc vẽ thực tế sẽ được thực hiện bởi luồng AWT/Swing EDT)
            gamePanel.repaint();

            // 3. Điều khiển FPS: Tính toán thời gian ngủ (sleep)
            // Logic: sleepTime = (Thời gian chuẩn 1 frame) - (Thời gian đã tiêu tốn cho update/repaint)
            try {
                // (lastLoopTime - System.nanoTime()) là số âm, chính là thời gian đã tiêu tốn
                // OPTIMAL_TIME + (thời gian tiêu tốn) = thời gian còn lại cần ngủ
                // Chia cho 1,000,000 để đổi từ nano giây sang mili giây cho Thread.sleep()
                long sleepTime = (lastLoopTime - System.nanoTime() + OPTIMAL_TIME) / 1000000;

                if (sleepTime > 0) {
                    // Chỉ ngủ nếu công việc (update/repaint) hoàn thành sớm hơn OPTIMAL_TIME
                    Thread.sleep(sleepTime);
                }
            } catch (InterruptedException e) {
                running = false; // Nếu luồng bị ngắt, dừng vòng lặp
                Thread.currentThread().interrupt(); // Đặt lại cờ ngắt (good practice)
            }
        }
    }

    public void stopLoop() {
        running = false;
    }
}