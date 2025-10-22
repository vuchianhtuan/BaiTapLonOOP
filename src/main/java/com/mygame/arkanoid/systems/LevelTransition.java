// Đặt trong com.mygame.arkanoid.systems
package com.mygame.arkanoid.systems;

import com.mygame.arkanoid.core.GameManager;
import com.mygame.arkanoid.core.GamePanel;
import com.mygame.arkanoid.objects.Paddle;

import java.awt.*;
// THÊM DÒNG NÀY: Để bật chế độ khử răng cưa


public class LevelTransition {
    // Trạng thái chuyển đổi
    public enum State {
        IDLE,
        PADDLE_FLY_UP,
        FADE_TO_BLACK,
        FADE_FROM_BLACK,
        PADDLE_FLY_IN,
        BRICK_SPAWN,
        COUNTDOWN,
        FINISH
    }

    private State currentState = State.IDLE;
    private int timer = 0;
    private int maxDuration = 0;

    private Paddle flyingPaddle;
    private GameManager gameManager;


    // Biến cho các hiệu ứng
    private float alpha = 0.0f; // Độ trong suốt cho hiệu ứng tối/sáng
    private int brickSpawnCount = 0; // Số gạch đã xuất hiện
    private int startPaddleX, startPaddleY;
    private int finalPaddleX, finalPaddleY;

    // Biến cho font chữ
    private java.awt.Font countdownFont;

    public LevelTransition(GameManager gameManager) {
        this.gameManager = gameManager;
        this.currentState = State.IDLE;

        // SỬA: Đổi font sang "SansSerif" (tối giản) và dùng kiểu PLAIN (không đậm)
        this.countdownFont = new java.awt.Font("SansSerif", Font.BOLD, 72);
    }

    public void startTransition(Paddle oldPaddle) {
        this.flyingPaddle = oldPaddle; // Lưu paddle cũ
        currentState = State.PADDLE_FLY_UP;
        timer = 0;
        maxDuration = 120;
        brickSpawnCount = 0;
    }

    public void update() {
        if (currentState == State.IDLE || currentState == State.FINISH) {
            return;
        }

        timer++;

        switch (currentState) {
            case PADDLE_FLY_UP: {
                if (flyingPaddle == null) {
                    break;
                }

                int startX = flyingPaddle.getX();
                int startY = 670;
                int midX = GamePanel.WIDTH / 2 - flyingPaddle.getWidth() / 2;
                int midY = 400;
                int endY = -flyingPaddle.getHeight();

                final int TOTAL_DURATION = maxDuration;
                final int PHASE_1_END = 60;
                final int PHASE_2_END = 70;
                final int PHASE_3_END = 120;

                float newX = midX, newY = midY;

                if (timer <= PHASE_1_END) {
                    float percentage = (float) timer / PHASE_1_END;
                    newX = startX + (midX - startX) * percentage;
                    newY = startY + (midY - startY) * percentage;
                } else if (timer <= PHASE_2_END) {
                    newX = midX;
                    newY = midY;
                } else {
                    float timerPhase3 = timer - PHASE_2_END;
                    final int DURATION_PHASE_3 = PHASE_3_END - PHASE_2_END;
                    float percentage = timerPhase3 / DURATION_PHASE_3;
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
                    this.flyingPaddle = null;
                }
                break;
            }

            case FADE_TO_BLACK:
                alpha = (float) timer / maxDuration;
                if (timer >= maxDuration) {
                    currentState = State.FADE_FROM_BLACK;
                    timer = 0;
                    maxDuration = 100;

                }
                break;

            case FADE_FROM_BLACK:
                alpha = 1.0f - (float) timer / maxDuration;
                if (timer >= maxDuration) {
                    currentState = State.PADDLE_FLY_IN;
                    timer = 0;
                    maxDuration = 60;

                }
                break;

            case PADDLE_FLY_IN: {
                Paddle newPaddle = gameManager.getPaddle();

                if (newPaddle == null) {
                    currentState = State.BRICK_SPAWN;
                    timer = 0;
                    break;
                }

                if (timer == 1) {
                    this.startPaddleX = newPaddle.getX();
                    this.startPaddleY = newPaddle.getY();

                    this.finalPaddleX = (GamePanel.WIDTH / 2) - newPaddle.getWidth() / 2;
                    this.finalPaddleY = 670; // Vị trí chơi game
                }

                float percentage = (float) timer / maxDuration;
                float t = percentage;
                float easedPercentage = 1 - (float) Math.pow(1 - t, 3);

                float newX = startPaddleX + (finalPaddleX - startPaddleX) * easedPercentage;
                float newY = startPaddleY + (finalPaddleY - startPaddleY) * easedPercentage;

                newPaddle.setX((int) newX);
                newPaddle.setY((int) newY);

                if (timer >= maxDuration) {
                    newPaddle.setX(finalPaddleX);
                    newPaddle.setY(finalPaddleY);
                    currentState = State.BRICK_SPAWN;
                    timer = 0;
                }
                break;
            }

            case BRICK_SPAWN:
                timer++;
                break;

            case COUNTDOWN:
                if (timer >= maxDuration) {
                    currentState = State.FINISH;
                }
                break;

            case FINISH:
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

        // Logic FADE
        if (currentState == State.FADE_TO_BLACK || currentState == State.FADE_FROM_BLACK) {
            if (alpha > 0.0f) {
                g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
                g2d.setColor(Color.BLACK);
                g2d.fillRect(0, 0, GamePanel.WIDTH, GamePanel.HEIGHT);
                g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
            }
        }
        // Logic vẽ COUNTDOWN
        else if (currentState == State.COUNTDOWN) {

            g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            g2d.setFont(this.countdownFont);
            java.awt.FontMetrics metrics = g2d.getFontMetrics(this.countdownFont);

            String textToShow = "";
            int phaseDuration = 65; // 1 giây mỗi pha
            int phase = timer / phaseDuration; // 0, 1, 2, 3, 4

            switch (phase) {
                case 0: // "Level X"
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

            int timerInPhase = timer % phaseDuration;
            float halfPhase = phaseDuration / 2.0f;
            float textAlpha = 0.0f;

            if (timerInPhase < halfPhase) {
                textAlpha = (float)timerInPhase / halfPhase;
            } else {
                textAlpha = 1.0f - ((float)(timerInPhase - halfPhase) / halfPhase);
            }
            textAlpha = Math.max(0.0f, Math.min(1.0f, textAlpha));

            int x = (GamePanel.WIDTH - metrics.stringWidth(textToShow)) / 2;
            int y = (GamePanel.HEIGHT / 2) - (metrics.getHeight() / 2) + metrics.getAscent();

            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, textAlpha));

            g2d.setColor(Color.WHITE);
            g2d.drawString(textToShow, x, y);

            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
        }
    }

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
        currentState = State.COUNTDOWN;
        timer = 0;
        maxDuration = 300;
    }
}