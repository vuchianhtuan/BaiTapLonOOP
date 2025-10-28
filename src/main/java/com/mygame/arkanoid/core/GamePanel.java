package com.mygame.arkanoid.core;

import com.mygame.arkanoid.engine.InputHandler;
import com.mygame.arkanoid.engine.Renderer;
import com.mygame.arkanoid.objects.bricks.Shard;
import com.mygame.arkanoid.systems.MenuManager;
import com.mygame.arkanoid.systems.ScalingManager;
import com.mygame.arkanoid.systems.ScoreManager;
import com.mygame.arkanoid.systems.UIManager;

import static com.mygame.arkanoid.core.GameManager.GAMESTATE_PAUSED;

import javax.swing.JPanel;
import java.awt.*;

public class GamePanel extends JPanel {
    public static final int WIDTH = 1120;
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
        ScalingManager sm = ScalingManager.getInstance();
        sm.update(getWidth(), getHeight());
        String currentState = gameManager.getGameState();
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, getWidth(), getHeight());

        if ("PLAYING".equals(currentState) || GAMESTATE_PAUSED.equals(currentState) || "GAME_OVER".equals(currentState) || "TRANSITION".equals(currentState)) {
            Image bg = gameManager.getCurrentBackground();
            if (bg != null) {
                g.drawImage(bg,
                        sm.scaleX(0), sm.scaleY(0),
                        sm.scaleWidth(sm.GAME_AREA_WIDTH), // <-- Chỉ rộng 960px
                        sm.scaleHeight(sm.NATIVE_HEIGHT),
                        null);
            } else {
                g.setColor(Color.BLACK);
                g.fillRect(sm.scaleX(0), sm.scaleY(0),
                        sm.scaleWidth(sm.GAME_AREA_WIDTH),
                        sm.scaleHeight(sm.NATIVE_HEIGHT));
            }

            // 3. Vẽ Nền Sidebar
            g.setColor(new Color(30, 30, 30)); // Màu xám tối
            g.fillRect(sm.scaleX(sm.GAME_AREA_WIDTH), sm.scaleY(0), // <-- Bắt đầu từ 960px
                    sm.scaleWidth(sm.NATIVE_WIDTH - sm.GAME_AREA_WIDTH),
                    sm.scaleHeight(sm.NATIVE_HEIGHT));

            renderer.renderGame(g, gameManager.getPaddle(),
                    gameManager.getBall(),
                    gameManager.getBricks(),
                    gameManager.getPowerUps(),
                    gameManager.getBalls(),
                    gameManager.getBoss(),
                    gameManager.getLasers(),
                    gameManager.getLaserShooters(),
                    gameManager.getCurrentBackground(),
                    gameManager.getActiveShards());
            uiManager.draw(g);
            drawSidebarExtras(g, sm);
            gameManager.getLevelTransition().render(g);
        } else if ("MENU".equals(currentState)) {
            gameManager.getMenuManager().render(g);
        } else if ("HIGH_SCORES".equals(currentState)) {
            gameManager.getScoreManager().render(g);
        } else if ("SETTING".equals(currentState)) {
            gameManager.getSettingManager().render(g);
        } else if ("LEVEL_SELECT".equals(currentState)) {
            gameManager.getSelectLevel().render(g);
        }
    }

    /**
     * Hàm này vẽ các phần tử sidebar không được UIManager quản lý
     */
    private void drawSidebarExtras(Graphics g, ScalingManager sm) {
        int logicX = 980; // 960 + 20 padding
        int currentY = 320; // Vị trí logic Y (bạn có thể điều chỉnh)

        // 1. Vẽ Thời gian
        Font baseFont = new Font("Arial", Font.BOLD, 24);
        Font scaledFont = baseFont.deriveFont((float)(baseFont.getSize() * sm.getScale()));

        g.setFont(scaledFont);
        g.setColor(Color.WHITE);
        g.drawString("TIME", sm.scaleX(logicX), sm.scaleY(currentY));

        currentY += 30;

        baseFont = new Font("Arial", Font.PLAIN, 22);
        scaledFont = baseFont.deriveFont((float)(baseFont.getSize() * sm.getScale()));
        g.setFont(scaledFont);

        long totalSeconds = gameManager.getCurrentLevelPlaytimeMillis() / 1000;
        long minutes = totalSeconds / 60;
        long seconds = totalSeconds % 60;
        g.drawString(String.format("%02d:%02d", minutes, seconds), sm.scaleX(logicX), sm.scaleY(currentY));

        Rectangle menuRect = gameManager.getMenuButtonRect();
        String menuText = "MENU";

        // Vẽ nền nút
        g.setColor(Color.GRAY);
        g.fillRect(sm.scaleX(menuRect.x), sm.scaleY(menuRect.y),
                sm.scaleWidth(menuRect.width), sm.scaleHeight(menuRect.height));

        // Vẽ viền nút
        g.setColor(Color.WHITE);
        g.drawRect(sm.scaleX(menuRect.x), sm.scaleY(menuRect.y),
                sm.scaleWidth(menuRect.width), sm.scaleHeight(menuRect.height));

        FontMetrics fm = g.getFontMetrics(scaledFont);
        int textWidthScaled = fm.stringWidth(menuText);
        int textX_screen = sm.scaleX(menuRect.x) + (sm.scaleWidth(menuRect.width) - textWidthScaled) / 2;
        int textY_screen = sm.scaleY(menuRect.y) + (sm.scaleHeight(menuRect.height) - fm.getHeight()) / 2 + fm.getAscent();

        g.drawString(menuText, textX_screen, textY_screen);

        // 3. VẼ NÚT PAUSE/RESUME (CẬP NHẬT LOGIC CŨ)
        Rectangle buttonRect;
        String buttonText;
        if (GAMESTATE_PAUSED.equals(gameManager.getGameState())) {
            buttonRect = gameManager.getResumeButtonRect(); // <-- Đổi tên
            buttonText = "RESUME"; // <-- Đổi chữ
        } else {
            buttonRect = gameManager.getPauseButtonRect();
            buttonText = "PAUSE";
        }

        // Vẽ nền nút
        g.setColor(Color.GRAY);
        g.fillRect(sm.scaleX(buttonRect.x), sm.scaleY(buttonRect.y),
                sm.scaleWidth(buttonRect.width), sm.scaleHeight(buttonRect.height));

        // Vẽ viền nút
        g.setColor(Color.WHITE);
        g.drawRect(sm.scaleX(buttonRect.x), sm.scaleY(buttonRect.y),
                sm.scaleWidth(buttonRect.width), sm.scaleHeight(buttonRect.height));

        // Vẽ chữ (căn giữa)
        // (Font đã được set ở trên)
        textWidthScaled = fm.stringWidth(buttonText);
        textX_screen = sm.scaleX(buttonRect.x) + (sm.scaleWidth(buttonRect.width) - textWidthScaled) / 2;
        textY_screen = sm.scaleY(buttonRect.y) + (sm.scaleHeight(buttonRect.height) - fm.getHeight()) / 2 + fm.getAscent();

        g.drawString(buttonText, textX_screen, textY_screen);
    }
}