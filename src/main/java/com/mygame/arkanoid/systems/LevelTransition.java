// Đặt trong com.mygame.arkanoid.systems
package com.mygame.arkanoid.systems;

import com.mygame.arkanoid.core.GamePanel;
import com.mygame.arkanoid.objects.Paddle;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;

public class LevelTransition {
    // Trạng thái chuyển đổi
    public enum State {
        IDLE,           // Không làm gì cả
        PADDLE_FLY_UP,  // Paddle bay lên
        FADE_TO_BLACK,  // Màn hình tối lại
        FADE_FROM_BLACK,// Màn hình sáng lên
        BRICK_SPAWN,    // Gạch từ từ xuất hiện
        FINISH          // Hoàn thành chuyển đổi
    }

    private State currentState = State.IDLE;
    private int timer = 0; // Bộ đếm thời gian cho mỗi pha
    private int maxDuration = 0; // Thời gian tối đa của pha hiện tại

    // Cần có tham chiếu đến Paddle để điều khiển nó
    private Paddle paddle;

    // Biến cho các hiệu ứng
    private float alpha = 0.0f; // Độ trong suốt cho hiệu ứng tối/sáng
    private int brickSpawnCount = 0; // Số gạch đã xuất hiện

    public void startTransition(Paddle paddle) {
        this.paddle = paddle;
        currentState = State.PADDLE_FLY_UP;
        timer = 0;
        maxDuration = 120; // 60 frame (1 giây ở 60 FPS)
        brickSpawnCount = 0;
    }

    public void update() {
        if (currentState == State.IDLE || currentState == State.FINISH) {
            return;
        }

        timer++;

        switch (currentState) {
            case PADDLE_FLY_UP:
                if (paddle == null) {
                    // ... (Logic kiểm tra null giữ nguyên)
                    break;
                }

                // --- CẤU HÌNH CÁC ĐIỂM CHUYỂN ĐỘNG ---

                // Vị trí ban đầu của paddle (giả sử bạn lấy startX từ paddle)
                int startX = paddle.getX();
                int startY = 670;

                // Vị trí giữa màn hình (pha 1 kết thúc)
                int midX = GamePanel.WIDTH / 2 - paddle.getWidth() / 2;
                int midY = 400; // Vị trí Y cao hơn một chút so với vị trí chơi

                // Vị trí cuối (ngoài màn hình)
                int endY = -paddle.getHeight();

                final int TOTAL_DURATION = maxDuration;
                final int PHASE_1_END = 60;  // Kết thúc di chuyển chéo
                final int PHASE_2_END = 70;  // Kết thúc dừng/giữ vị trí
                final int PHASE_3_END = 120; // 120 frame

                float newX = midX, newY = midY; // Vị trí mặc định là tâm màn hình

                if (timer <= PHASE_1_END) {
                    // --- PHA 1: Di chuyển chéo về tâm (Tuyến tính) ---
                    float percentage = (float) timer / PHASE_1_END; // 0.0 -> 1.0 trong 40 frame

                    // Di chuyển tuyến tính từ Start đến Mid
                    newX = startX + (midX - startX) * percentage;
                    newY = startY + (midY - startY) * percentage;

                } else if (timer <= PHASE_2_END) {
                    // --- PHA 2: Dừng/Giữ vị trí ở tâm (20 frame) ---
                    newX = midX;
                    newY = midY;

                } else {
                    // --- PHA 3: Bay thẳng lên khỏi màn hình (Tăng tốc nhanh dần đều) ---

                    // Tính thời gian đã trôi qua trong Pha 3 (0 -> 60)
                    float timerPhase3 = timer - PHASE_2_END;
                    final int DURATION_PHASE_3 = PHASE_3_END - PHASE_2_END; // 60 frame

                    // Percentage cho Pha 3 (0.0 -> 1.0 trong 60 frame)
                    float percentage = timerPhase3 / DURATION_PHASE_3;

                    // Gia tốc: Sử dụng hàm bậc 2 (Quadratic) để mô phỏng nhanh dần đều (t^2)
                    float acceleratedPercentage = percentage * percentage;

                    // X: Giữ nguyên ở giữa màn hình
                    newX = midX;

                    // Y: Bay từ midY đến endY. Sử dụng gia tốc.
                    newY = midY + (endY - midY) * acceleratedPercentage;
                }

                // Cập nhật vị trí paddle
                paddle.setX((int) newX);
                paddle.setY((int) newY);

                if (timer >= TOTAL_DURATION) { // Kiểm tra với tổng thời gian
                    // Chuyển sang pha tiếp theo
                    currentState = State.FADE_TO_BLACK;
                    timer = 0;
                    maxDuration = 120;
                }
                break;

            case FADE_TO_BLACK:
                alpha = (float) timer / maxDuration; // Tăng alpha từ 0.0 lên 1.0 (tối dần)
                if (timer >= maxDuration) {
                    // Đã tối hoàn toàn. Tải level mới (việc này sẽ xảy ra trong GameManager)
                    currentState = State.FADE_FROM_BLACK;
                    timer = 0;
                    maxDuration = 100;

                    // Reset paddle về vị trí ban đầu (đã được level mới tải lại)
                    if (paddle != null) {
                        paddle.setY(670);
                    }
                }
                break;

            case FADE_FROM_BLACK:
                alpha = 1.0f - (float) timer / maxDuration; // Giảm alpha từ 1.0 về 0.0 (sáng dần)
                if (timer >= maxDuration) {
                    currentState = State.BRICK_SPAWN;
                    timer = 0;
                    // Max duration của BRICK_SPAWN sẽ được xác định trong GameManager
                }
                break;

            case BRICK_SPAWN:
                timer++;
                int bricksPerFrame = 1;
                break;

            case FINISH:
                // Reset mọi thứ và chờ lệnh tiếp theo
                paddle.setY(670); // Đảm bảo paddle ở đúng vị trí
                currentState = State.IDLE;
                break;
        }
    }

    /**
     * Phương thức vẽ hiệu ứng chuyển cảnh.
     */
    public void render(Graphics g) {
        if (currentState == State.IDLE) return;

        Graphics2D g2d = (Graphics2D) g;

        // Vẽ hiệu ứng tối/sáng (Chỉ khi alpha > 0)
        if (currentState == State.FADE_TO_BLACK || currentState == State.FADE_FROM_BLACK) {
            if (alpha > 0.0f) {
                // Đặt độ trong suốt
                g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
                // Vẽ một hình chữ nhật đen bao phủ toàn màn hình
                g2d.setColor(Color.BLACK);
                g2d.fillRect(0, 0, GamePanel.WIDTH, GamePanel.HEIGHT);
                // Khôi phục độ trong suốt (RẤT QUAN TRỌNG)
                g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
            }
        }

        // **LƯU Ý QUAN TRỌNG**: Việc vẽ gạch hiện tại sẽ được thực hiện trong GamePanel
        // dựa trên trạng thái của LevelTransition.
    }

    public void startInstantFade(Paddle paddle) {
        // Khởi động chuyển cảnh bỏ qua PADDLE_FLY_UP
        this.paddle = paddle;
        currentState = State.FADE_TO_BLACK; // <-- Bắt đầu ngay với FADE_TO_BLACK
        timer = 0;
        maxDuration = 1; // Đặt maxDuration nhỏ để FADE_TO_BLACK hoàn thành nhanh.

        // Đảm bảo paddle ở vị trí chơi khi bắt đầu FADE (sau đó sẽ bị màn hình đen che)
        if (paddle != null) {
            paddle.setY(670);
        }

        // Vì Level 1 không có hiệu ứng PADDLE_FLY_UP, ta cần tối màn hình ngay lập tức
        // để chuyển sang pha load level mới (FADE_FROM_BLACK).
        // Thay vì dùng maxDuration = 1, chúng ta nên đặt logic để nó chuyển thẳng
        // nếu đang là Level 1.

        // Tối ưu hóa: Thay vì chạy qua FADE_TO_BLACK nhanh, ta chuyển thẳng sang FADE_FROM_BLACK
        currentState = State.FADE_FROM_BLACK;
        timer = 0;
        maxDuration = 100; // Thời gian sáng lên
        alpha = 1.0f; // Bắt đầu từ tối hoàn toàn (alpha = 1.0)
        brickSpawnCount = 0;
    }

    public State getCurrentState() {
        return currentState;
    }

    public boolean isTransitioning() {
        return currentState != State.IDLE && currentState != State.FINISH;
    }

    public void setBrickSpawnCount(int count) {
        this.brickSpawnCount = count;
    }

    public int getBrickSpawnCount() {
        return brickSpawnCount;
    }

    public void finishTransition() {
        currentState = State.FINISH;
    }
}