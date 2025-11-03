package com.mygame.arkanoid.level;

import com.mygame.arkanoid.core.GameManager;
import com.mygame.arkanoid.objects.Paddle;
import com.mygame.arkanoid.systems.ScalingManager;
import com.mygame.arkanoid.config.GameConstants;

import java.awt.*;

/**
 * Quản lý máy trạng thái (state machine) cho các hiệu ứng chuyển cảnh
 * phức tạp giữa các màn chơi (ví dụ: paddle bay đi, mờ dần, đếm ngược).
 */
public class LevelTransition {

    /**
     * Các trạng thái (pha) của hiệu ứng chuyển cảnh.
     * Được thực thi tuần tự.
     */
    public enum State {
        IDLE,             // Không làm gì
        PADDLE_FLY_UP,    // Paddle cũ bay lên và thoát khỏi màn hình
        FADE_TO_BLACK,    // Màn hình mờ dần sang màu đen
        FADE_FROM_BLACK,  // Màn hình sáng dần từ màu đen
        PADDLE_FLY_IN,    // Paddle mới bay vào vị trí
        BRICK_SPAWN,      // Gạch bắt đầu xuất hiện (logic này được xử lý trong GameManager)
        COUNTDOWN,        // Đếm ngược "LEVEL X", "3", "2", "1", "START!"
        FINISH            // Hoàn tất, chờ để chuyển sang IDLE
    }

    // Biến trạng thái và bộ đếm thời gian
    private State currentState = State.IDLE;
    /** Bộ đếm frame chung, được reset ở mỗi trạng thái. */
    private int timer = 0;
    /** Thời lượng tối đa (tính bằng frame) cho trạng thái hiện tại. */
    private int maxDuration = 0;

    // Biến cho paddle bay lên
    private Paddle flyingPaddle;
    private GameManager gameManager;


    /** Độ mờ (alpha) cho hiệu ứng FADE_TO_BLACK và FADE_FROM_BLACK (0.0f đến 1.0f). */
    private float alpha = 0.0f;
    /** Cờ (flag) dùng để đảm bảo logic tải level chỉ chạy 1 lần. */
    private int brickSpawnCount = 0;
    // Tọa độ để nội suy (interpolate) chuyển động của paddle
    private int startPaddleX, startPaddleY;
    private int finalPaddleX, finalPaddleY;

    // Biến cho font chữ
    private java.awt.Font countdownFont;

    /**
     * Khởi tạo trình quản lý chuyển cảnh.
     * @param gameManager Tham chiếu đến GameManager để lấy paddle mới và thông tin level.
     */
    public LevelTransition(GameManager gameManager) {
        this.gameManager = gameManager;
        this.currentState = State.IDLE;
        this.countdownFont = new java.awt.Font("SansSerif", Font.BOLD, 72);
    }

    /**
     * Bắt đầu hiệu ứng chuyển cảnh đầy đủ (dùng khi chuyển level).
     * Bắt đầu bằng việc paddle cũ bay lên.
     *
     * @param oldPaddle Paddle của level trước đó (sẽ được điều khiển bay đi).
     */
    public void startTransition(Paddle oldPaddle) {
        this.flyingPaddle = oldPaddle; // Lưu paddle cũ
        currentState = State.PADDLE_FLY_UP;
        timer = 0;
        maxDuration = 120; // 2 giây (ở 60FPS)
        brickSpawnCount = 0;
    }

    /**
     * Cập nhật logic cho trạng thái chuyển cảnh hiện tại (được gọi mỗi frame).
     */
    public void update() {
        if (currentState == State.IDLE || currentState == State.FINISH) {
            return;
        }

        timer++;
        int gameAreaWidth = ScalingManager.getInstance().GAME_AREA_WIDTH;

        // Cập nhật logic dựa trên trạng thái hiện tại
        switch (currentState) {
            case PADDLE_FLY_UP: {
                if (flyingPaddle == null) {
                    // Nếu không có paddle (ví dụ: đang ở menu), bỏ qua và mờ dần
                    currentState = State.FADE_TO_BLACK;
                    timer = 0;
                    maxDuration = 120;
                    break;
                }

                // --- Logic phức tạp cho paddle bay (3 giai đoạn) ---
                int startX = flyingPaddle.getX();
                int startY = 670; // Vị trí chơi game (logic)
                int midX = gameAreaWidth / 2 - flyingPaddle.getWidth() / 2; // Giữa màn hình
                int midY = 400; // Điểm dừng tạm thời
                int endY = -flyingPaddle.getHeight(); // Bay ra khỏi đỉnh màn hình

                final int TOTAL_DURATION = maxDuration;
                final int PHASE_1_END = 60; // Giai đoạn 1: Bay từ vị trí chơi -> giữa (60f)
                final int PHASE_2_END = 70; // Giai đoạn 2: Dừng 10f
                final int PHASE_3_END = 120;// Giai đoạn 3: Bay từ giữa -> ra ngoài (50f)

                float newX = midX, newY = midY;

                if (timer <= PHASE_1_END) {
                    // Giai đoạn 1: Nội suy tuyến tính (Lerp) đến điểm giữa
                    float percentage = (float) timer / PHASE_1_END;
                    newX = startX + (midX - startX) * percentage;
                    newY = startY + (midY - startY) * percentage;
                } else if (timer <= PHASE_2_END) {
                    // Giai đoạn 2: Giữ nguyên
                    newX = midX;
                    newY = midY;
                } else {
                    // Giai đoạn 3: Bay ra ngoài (có gia tốc - ease-in)
                    float timerPhase3 = timer - PHASE_2_END;
                    final int DURATION_PHASE_3 = PHASE_3_END - PHASE_2_END;
                    float percentage = timerPhase3 / DURATION_PHASE_3;
                    // Dùng (percentage * percentage) để tạo hiệu ứng tăng tốc
                    float acceleratedPercentage = percentage * percentage;
                    newX = midX;
                    newY = midY + (endY - midY) * acceleratedPercentage;
                }

                flyingPaddle.setX((int) newX);
                flyingPaddle.setY((int) newY);

                if (timer >= TOTAL_DURATION) {
                    currentState = State.FADE_TO_BLACK;
                    timer = 0;
                    maxDuration = 120;
                    this.flyingPaddle = null; // Xóa tham chiếu paddle cũ
                }
                break;
            }

            case FADE_TO_BLACK:
                // Tăng alpha tuyến tính từ 0.0 -> 1.0
                alpha = (float) timer / maxDuration;
                if (timer >= maxDuration) {
                    currentState = State.FADE_FROM_BLACK;
                    timer = 0;
                    maxDuration = 100;
                }
                break;

            case FADE_FROM_BLACK:
                // Giảm alpha tuyến tính từ 1.0 -> 0.0
                alpha = 1.0f - (float) timer / maxDuration;
                if (timer >= maxDuration) {
                    currentState = State.PADDLE_FLY_IN;
                    timer = 0;
                    maxDuration = 60;
                }
                break;

            case PADDLE_FLY_IN: {
                Paddle newPaddle = gameManager.getPaddle(); // Lấy paddle mới

                if (newPaddle == null) {
                    // Bỏ qua nếu paddle chưa được tạo
                    currentState = State.BRICK_SPAWN;
                    timer = 0;
                    break;
                }

                // Chạy 1 lần duy nhất để lấy tọa độ
                if (timer == 1) {
                    this.startPaddleX = newPaddle.getX();
                    this.startPaddleY = newPaddle.getY(); // Vị trí mặc định (thường ở giữa)

                    this.finalPaddleX = (gameAreaWidth / 2) - newPaddle.getWidth() / 2;
                    this.finalPaddleY = 670; // Vị trí chơi game
                }

                // Sử dụng hàm Easing (EaseOutCubic) để paddle bay vào mượt mà
                float percentage = (float) timer / maxDuration;
                float t = percentage;
                float easedPercentage = 1 - (float) Math.pow(1 - t, 3); // (1 - (1-t)^3)

                float newX = startPaddleX + (finalPaddleX - startPaddleX) * easedPercentage;
                float newY = startPaddleY + (finalPaddleY - startPaddleY) * easedPercentage;

                newPaddle.setX((int) newX);
                newPaddle.setY((int) newY);

                if (timer >= maxDuration) {
                    // Đảm bảo paddle ở đúng vị trí cuối cùng
                    newPaddle.setX(finalPaddleX);
                    newPaddle.setY(finalPaddleY);
                    currentState = State.BRICK_SPAWN;
                    timer = 0;
                }
                break;
            }

            case BRICK_SPAWN:
                // Không làm gì, logic được xử lý bởi GameManager
                // GameManager sẽ gọi finishTransition() khi spawn gạch xong
                break;

            case COUNTDOWN:
                // Chờ cho đến khi hết thời gian đếm ngược
                if (timer >= maxDuration) {
                    currentState = State.FINISH;
                }
                break;

            case FINISH:
                // Hoàn tất, reset về trạng thái chờ
                currentState = State.IDLE;
                timer = 0;
                alpha = 0.0f;
                break;
        }
    }

    /**
     * Phương thức vẽ các hiệu ứng chuyển cảnh (ví dụ: lớp phủ đen, đếm ngược).
     * @param g Đối tượng Graphics để vẽ.
     */
    public void render(Graphics g) {
        if (currentState == State.IDLE) return;

        Graphics2D g2d = (Graphics2D) g;
        ScalingManager sm = ScalingManager.getInstance();

        // Logic FADE (vẽ lớp phủ đen mờ)
        if (currentState == State.FADE_TO_BLACK || currentState == State.FADE_FROM_BLACK) {
            if (alpha > 0.0f) {
                // Đặt độ mờ (alpha)
                g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
                g2d.setColor(Color.BLACK);

                // Vẽ hình chữ nhật đen che toàn bộ cửa sổ (đã scale)
                g2d.fillRect(
                        sm.scaleX(0), sm.scaleY(0),
                        sm.scaleWidth(sm.NATIVE_WIDTH),
                        sm.scaleHeight(sm.NATIVE_HEIGHT));

                // Reset độ mờ về mặc định
                g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
            }
        }
        else if (currentState == State.COUNTDOWN) {
            // Vẽ chữ đếm ngược "LEVEL X", "3", "2", "1"...

            g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            // --- Logic vẽ đếm ngược ---

            // 1. Scale font chữ dựa trên tỷ lệ màn hình
            Font scaledFont = this.countdownFont.deriveFont((float)(this.countdownFont.getSize() * sm.getScale()));
            g2d.setFont(scaledFont);
            java.awt.FontMetrics metrics = g2d.getFontMetrics(scaledFont);

            // 2. Quyết định văn bản (text) để hiển thị dựa trên thời gian
            String textToShow = "";
            int phaseDuration = 65; // Thời gian cho mỗi số (khoảng 1.1 giây)
            int phase = timer / phaseDuration; // Đang ở pha nào (0, 1, 2, 3...)
            switch (phase) {
                case 0: // Pha 0: Hiển thị "LEVEL X"
                    int level = gameManager.getLevelManager().getCurrentLevelIndex() + 1;
                    textToShow = "LEVEL " + level;
                    break;
                case 1: textToShow = "3"; break;
                case 2: textToShow = "2"; break;
                case 3: textToShow = "1"; break;
                case 4: textToShow = "START!"; break;
                default:
                    textToShow = "START!";
                    break;
            }

            // 3. Tính toán độ mờ (alpha) cho văn bản (fade in -> fade out)
            int timerInPhase = timer % phaseDuration;
            float halfPhase = phaseDuration / 2.0f;
            float textAlpha = 0.0f;
            if (timerInPhase < halfPhase) {
                // Nửa đầu: Fade in (0.0 -> 1.0)
                textAlpha = (float)timerInPhase / halfPhase;
            } else {
                // Nửa sau: Fade out (1.0 -> 0.0)
                textAlpha = 1.0f - ((float)(timerInPhase - halfPhase) / halfPhase);
            }
            textAlpha = Math.max(0.0f, Math.min(1.0f, textAlpha)); // Đảm bảo trong [0, 1]


            // 4. Tính toán tọa độ vẽ (đã scale) để căn giữa
            int stringWidth_screen = metrics.stringWidth(textToShow);
            // Lấy tọa độ X (màn hình) của TÂM khu vực chơi game
            int game_area_center_screen_x = sm.scaleX(sm.GAME_AREA_WIDTH / 2);
            // Tọa độ X để bắt đầu vẽ (căn giữa)
            int drawX_screen = game_area_center_screen_x - (stringWidth_screen / 2);

            // Tọa độ Y (căn giữa theo chiều dọc)
            // (Tính toán trong tọa độ logic trước, sau đó scale)
            int logicY = (sm.NATIVE_HEIGHT / 2) - (metrics.getHeight() / 2) + metrics.getAscent();
            int drawY_screen = sm.scaleY(logicY);


            // 5. Vẽ chữ với độ mờ và màu
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, textAlpha));
            g2d.setColor(Color.WHITE);
            g2d.drawString(textToShow, drawX_screen, drawY_screen);

            // Reset alpha
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
        }
    }

    /**
     * Bắt đầu hiệu ứng chuyển cảnh tức thì (dùng cho level đầu tiên).
     * Bắt đầu ngay từ trạng thái FADE_FROM_BLACK (sáng dần lên).
     */
    public void startInstantFade() {
        this.flyingPaddle = null;

        currentState = State.FADE_FROM_BLACK;
        timer = 0;
        maxDuration = 100; // Thời gian sáng lên
        alpha = 1.0f; // Bắt đầu từ tối hoàn toàn (alpha = 1.0)
        brickSpawnCount = 0;
    }

    public State getCurrentState() {
        return currentState;
    }

    /**
     * Kiểm tra xem một hiệu ứng chuyển cảnh có đang diễn ra hay không.
     * @return true nếu không phải IDLE hoặc FINISH.
     */
    public boolean isTransitioning() {
        return currentState != State.IDLE && currentState != State.FINISH;
    }

    public void setBrickSpawnCount(int count) {
        this.brickSpawnCount = count;
    }

    public int getBrickSpawnCount() {
        return brickSpawnCount;
    }

    /**
     * Được gọi bởi GameManager sau khi giai đoạn BRICK_SPAWN hoàn tất
     * để bắt đầu giai đoạn COUNTDOWN.
     */
    public void finishTransition() {
        currentState = State.COUNTDOWN;
        timer = 0;
        maxDuration = GameConstants.GAME_START_TIMER_FRAMES; // Lấy thời gian từ hằng số
    }
}