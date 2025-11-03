package com.mygame.arkanoid.core;

import com.mygame.arkanoid.engine.InputHandler;
import com.mygame.arkanoid.engine.Renderer;
import com.mygame.arkanoid.systems.ScalingManager;
import com.mygame.arkanoid.level.LevelTransition;
import com.mygame.arkanoid.ui.hud.Sidebar;

import static com.mygame.arkanoid.core.GameManager.GAMESTATE_PAUSED;

import javax.swing.JPanel;
import java.awt.*;

/**
 * Bảng điều khiển (JPanel) chính của game.
 * Đây là "bức tranh" (canvas) nơi tất cả các thành phần đồ họa được vẽ (render)
 * lên màn hình.
 * Lớp này cũng chịu trách nhiệm nhận (listen) tất cả các sự kiện input từ người dùng.
 */
public class GamePanel extends JPanel {

    /**
     * Kích thước 'gốc' (native) hoặc 'ưu tiên' (preferred) của game.
     * ScalingManager sẽ sử dụng các giá trị này làm cơ sở để tính toán
     * tỷ lệ co giãn (scaling) cho các kích thước cửa sổ khác nhau.
     */
    public static final int WIDTH = 1120;
    public static final int HEIGHT = 720;

    private final GameManager gameManager;
    private final Sidebar sidebar;
    private final LevelTransition levelTransition;
    private final Renderer renderer;

    /**
     * Khởi tạo GamePanel.
     * @param gameManager Đối tượng GameManager (Singleton) để truy cập tất cả các hệ thống con.
     */
    public GamePanel(GameManager gameManager) {
        // Thiết lập kích thước mong muốn ban đầu cho cửa sổ.
        this.setPreferredSize(new Dimension(WIDTH, HEIGHT));
        // Rất quan trọng: Cho phép panel nhận được 'focus' để KeyListener hoạt động.
        this.setFocusable(true);

        this.gameManager = gameManager;
        this.renderer = new Renderer();
        this.sidebar = gameManager.getUIManager();
        this.levelTransition = gameManager.getLevelTransition();
        InputHandler inputHandler = gameManager.getInputHandler();

        // Thêm các trình nghe sự kiện input (bàn phím, chuột)
        this.addKeyListener(inputHandler); // Gắn bộ lắng nghe bàn phím
        this.addMouseMotionListener(inputHandler); // Gắn bộ lắng nghe di chuyển/kéo chuột
        this.addMouseListener(inputHandler); // Gắn bộ lắng nghe nhấn/nhả/click chuột
    }

    /**
     * Phương thức vẽ (render) chính, được Swing gọi tự động.
     * Ghi đè (override) từ JPanel để vẽ toàn bộ trạng thái game.
     * Đây là một máy trạng thái (state machine) về đồ họa.
     *
     * @param g Đối tượng Graphics do Swing cung cấp để vẽ.
     */
    @Override
    protected void paintComponent(Graphics g) {
        // Cần thiết: Xóa nội dung của frame trước đó (xóa màn hình).
        super.paintComponent(g);

        // Cập nhật tỷ lệ co giãn (scale) MỖI FRAME dựa trên kích thước cửa sổ hiện tại.
        ScalingManager sm = ScalingManager.getInstance();
        sm.update(getWidth(), getHeight());

        String currentState = gameManager.getGameState();

        // Vẽ một lớp nền đen "letterbox" (viền đen) cho toàn bộ cửa sổ.
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, getWidth(), getHeight());

        // --- Logic vẽ khi ở trong màn hình chơi (Game Area + Sidebar) ---
        if ("PLAYING".equals(currentState) || GAMESTATE_PAUSED.equals(currentState)
                || "GAME_OVER".equals(currentState) || "TRANSITION".equals(currentState)
                || "GAME_WIN".equals(currentState)) {

            // Lấy và vẽ nền của level
            Image bg = gameManager.getCurrentBackground();
            if (bg != null) {
                // Vẽ nền, nhưng chỉ trong khu vực Game Area (ví dụ: 960px rộng)
                g.drawImage(bg,
                        sm.scaleX(0), sm.scaleY(0),
                        sm.scaleWidth(sm.GAME_AREA_WIDTH), // <-- Chỉ rộng 960px
                        sm.scaleHeight(sm.NATIVE_HEIGHT),
                        null);
            } else {
                // Nền dự phòng (màu đen) nếu không có ảnh
                g.setColor(Color.BLACK);
                g.fillRect(sm.scaleX(0), sm.scaleY(0),
                        sm.scaleWidth(sm.GAME_AREA_WIDTH),
                        sm.scaleHeight(sm.NATIVE_HEIGHT));
            }

            // Vẽ Nền Sidebar (màu xám tối)
            g.setColor(new Color(30, 30, 30));
            // Vẽ nền cho Sidebar, bắt đầu ngay BÊN PHẢI Game Area
            g.fillRect(sm.scaleX(sm.GAME_AREA_WIDTH), sm.scaleY(0), // <-- Bắt đầu từ 960px
                    sm.scaleWidth(sm.NATIVE_WIDTH - sm.GAME_AREA_WIDTH),
                    sm.scaleHeight(sm.NATIVE_HEIGHT));

            // Ủy quyền cho Renderer vẽ tất cả các thực thể
            // (bóng, gạch, paddle, power-ups, mảnh vỡ...)
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

            sidebar.draw(g); // Vẽ các thành phần UI (điểm, mạng) LÊN TRÊN sidebar
            levelTransition.render(g); // Vẽ hiệu ứng chuyển cảnh (ví dụ: mờ dần) LÊN TRÊN TẤT CẢ

            // --- Xử lý vẽ các trạng thái đặc biệt (GAME_WIN / GAME_OVER) ---
            if ("GAME_WIN".equals(currentState)) {
                // Tạo màu đen mờ (Alpha = 128)
                Color overlayColor = new Color(0, 0, 0, 128); // 50% mờ
                g.setColor(overlayColor);
                // Vẽ lớp phủ mờ LÊN TRÊN game
                g.fillRect(sm.scaleX(0), sm.scaleY(0),
                        sm.scaleWidth(sm.NATIVE_WIDTH),
                        sm.scaleHeight(sm.NATIVE_HEIGHT));

                // Vẽ bảng thống kê "YOU WIN!"
                gameManager.getGameSummaryPanel().draw(g,
                        "YOU WIN!",
                        gameManager.getFinalScore(),
                        gameManager.getFinalPlaytimeMillis()
                );

            } else if ("GAME_OVER".equals(currentState)) {
                // Lớp phủ mờ tương tự
                Color overlayColor = new Color(0, 0, 0, 128);
                g.setColor(overlayColor);
                g.fillRect(sm.scaleX(0), sm.scaleY(0),
                        sm.scaleWidth(sm.NATIVE_WIDTH),
                        sm.scaleHeight(sm.NATIVE_HEIGHT));

                // Vẽ bảng thống kê "GAME OVER"
                gameManager.getGameSummaryPanel().draw(g,
                        "GAME OVER",
                        gameManager.getFinalScore(),
                        gameManager.getFinalPlaytimeMillis()
                );
            }

            // --- Logic vẽ khi ở các màn hình Menu ---
        } else if ("MENU".equals(currentState)) {
            // Ủy quyền vẽ cho MenuManager
            gameManager.getMenuManager().render(g);
        } else if ("HIGH_SCORES".equals(currentState)) {
            gameManager.getScoreManager().render(g);
        } else if ("SETTING".equals(currentState)) {
            gameManager.getSettingManager().render(g);
        } else if ("LEVEL_SELECT".equals(currentState)) {
            gameManager.getSelectLevel().render(g);
        }
    }
}