package com.mygame.arkanoid.ui.hud;

import com.mygame.arkanoid.core.GameManager;
import com.mygame.arkanoid.engine.InputHandler;
import com.mygame.arkanoid.systems.ScalingManager;

import java.awt.*;

import static com.mygame.arkanoid.core.GameManager.GAMESTATE_PAUSED;

/**
 * Quản lý toàn bộ giao diện người dùng (UI) nằm ở thanh bên (Sidebar).
 * <p>
 * Lớp này không phải là một component Swing (như JPanel), mà là một trình
 * quản lý (manager) logic. Nó có các trách nhiệm chính:
 * <ol>
 * <li><b>Tổng hợp (Composition):</b> Chứa và quản lý các thành phần con
 * ({@link HeartUI}, {@link ScoreUI}).</li>
 * <li><b>Xử lý Tương tác (Interaction):</b> Quản lý và xử lý logic
 * nhấn chuột (click) cho các nút (Pause, Resume, Menu).</li>
 * <li><b>Vẽ (Rendering):</b> Điều phối việc vẽ tất cả các thành phần
 * của nó (bao gồm các nút và thông tin như Level, Time).</li>
 * </ol>
 * Logic {@link #update()} của nó được gọi bởi GameManager khi ở
 * trạng thái "PLAYING" hoặc "PAUSED".
 */
public class Sidebar {
    private GameManager gameManager;
    private InputHandler inputHandler;
    private HeartUI heartUI;
    private ScoreUI scoreUI;

    // Vùng (bounds) logic cho các nút
    private Rectangle pauseButtonRect;
    private Rectangle resumeButtonRect;
    private Rectangle menuButtonRect;
    /**
     * Cờ (flag) dùng để chống dội (debounce)
     * Ngăn chặn việc nút được nhấn liên tục 60 lần/giây khi giữ chuột.
     */
    private boolean pauseCooldown = false;

    /**
     * Khởi tạo trình quản lý Sidebar.
     * <p>
     * Khởi tạo các thành phần con ({@link HeartUI}, {@link ScoreUI})
     * và tính toán, lưu trữ tọa độ logic (native coordinates)
     * cho các nút tương tác (Pause, Resume, Menu).
     *
     * @param gm           Tham chiếu đến {@link GameManager} (để lấy trạng thái, chỉ số).
     * @param inputHandler Tham chiếu đến {@link InputHandler} (để kiểm tra click chuột).
     */
    public Sidebar(GameManager gm, InputHandler inputHandler) {
        this.gameManager = gm;
        this.inputHandler = inputHandler;
        this.heartUI = new HeartUI(gm);
        this.scoreUI = new ScoreUI(gm);

        // Tính toán vị trí logic (native coordinates) cho các nút
        int gameAreaWidth = ScalingManager.getInstance().GAME_AREA_WIDTH; // 960
        int sidebarWidth = ScalingManager.getInstance().NATIVE_WIDTH - gameAreaWidth; // 160
        // Căn giữa các nút trong khu vực sidebar
        int buttonLogicX = gameAreaWidth + (sidebarWidth - 120) / 2; // (960 + (160-120)/2) = 980
        int buttonWidth = 120;
        int buttonHeight = 40;
        int buttonLogicY_Pause = 650; // Vị trí nút Pause/Resume
        int buttonLogicY_Menu = 590;  // Vị trí nút Menu (cao hơn)

        // Lưu trữ vùng bounds logic (dùng tọa độ native)
        pauseButtonRect = new Rectangle(buttonLogicX, buttonLogicY_Pause, buttonWidth, buttonHeight);
        resumeButtonRect = new Rectangle(buttonLogicX, buttonLogicY_Pause, buttonWidth, buttonHeight);
        menuButtonRect = new Rectangle(buttonLogicX, buttonLogicY_Menu, buttonWidth, buttonHeight);
    }

    /**
     * Cập nhật logic của Sidebar (được gọi mỗi frame bởi GameManager).
     * <p>
     * Cập nhật các thành phần con (ví dụ: hiệu ứng nhấp nháy của {@link HeartUI})
     * và xử lý logic nhấn chuột cho các nút (Pause, Resume, Menu).
     * <p>
     * Logic này có trạng thái (stateful), tùy thuộc vào {@code gameManager.getGameState()}.
     * Nó cũng triển khai một cơ chế 'cooldown' đơn giản ({@code pauseCooldown})
     * để ngăn chặn việc nhấn nút nhiều lần trong một lần click (debouncing).
     */
    public void update() {
        // Cập nhật các thành phần con (ví dụ: HeartUI nhấp nháy)
        heartUI.update();
        scoreUI.update();

        // --- Xử lý Input (Click chuột) cho các nút ---
        ScalingManager sm = ScalingManager.getInstance();
        int screenMouseX = inputHandler.getMouseX();
        int screenMouseY = inputHandler.getMouseY();
        // Chuyển đổi tọa độ chuột (màn hình) về tọa độ logic (game)
        int mx = sm.unscaleX(screenMouseX);
        int my = sm.unscaleY(screenMouseY);

        String gameState = gameManager.getGameState();

        // --- Logic khi đang chơi (PLAYING) ---
        if ("PLAYING".equals(gameState)) {
            // Kiểm tra nút PAUSE
            if (pauseButtonRect.contains(mx, my) && inputHandler.isMousePressed()) {
                if (!pauseCooldown) { // Chỉ kích hoạt 1 lần
                    gameManager.setGameState(GAMESTATE_PAUSED);
                    pauseCooldown = true; // Kích hoạt cooldown
                }
                return; // Thoát sớm
            }

            // Kiểm tra nút MENU
            if (menuButtonRect.contains(mx, my) && inputHandler.isMousePressed()) {
                if (!pauseCooldown) {
                    gameManager.goToMenuAndEnableContinue(); // Yêu cầu GameManager xử lý
                    pauseCooldown = true;
                    return;
                }
            }
            // --- Logic khi đang tạm dừng (PAUSED) ---
        } else if (GAMESTATE_PAUSED.equals(gameState)) {
            // Kiểm tra nút RESUME
            if (resumeButtonRect.contains(mx, my) && inputHandler.isMousePressed()) {
                if (!pauseCooldown) {
                    gameManager.setGameState("PLAYING");
                    // Cập nhật lại thời gian để tránh delta time khổng lồ
                    gameManager.getPlayerStats().setLastUpdateTime(System.nanoTime());
                    pauseCooldown = true;
                }
            }

            // Kiểm tra nút MENU
            if (menuButtonRect.contains(mx, my) && inputHandler.isMousePressed()) {
                if (!pauseCooldown) {
                    gameManager.goToMenuAndEnableContinue(); // Yêu cầu GameManager xử lý
                    return;
                }
            }
        }

        // Reset cooldown khi người dùng nhả chuột
        if (!inputHandler.isMousePressed()) {
            pauseCooldown = false;
        }
    }

    /**
     * Vẽ (render) toàn bộ Sidebar.
     * <p>
     * Được gọi từ {@code GamePanel.paintComponent()}.
     * Phương thức này ủy quyền (delegate) việc vẽ cho:
     * <ul>
     * <li>{@link HeartUI} (vẽ trái tim)</li>
     * <li>{@link ScoreUI} (vẽ điểm số)</li>
     * <li>{@link #drawSidebarExtras} (vẽ Level, Time, và các nút)</li>
     * </ul>
     *
     * @param g Đối tượng Graphics để vẽ.
     */
    public void draw(Graphics g) {
        // 1. Vẽ các thành phần con
        heartUI.draw(g);
        // ScoreUI có thể dùng Graphics2D nếu cần (ví dụ: anti-aliasing)
        // Mặc dù hiện tại nó chỉ dùng Graphics, nhưng cast sang G2D an toàn hơn
        scoreUI.draw(g); // Ghi chú: Code gốc của bạn cast sang (Graphics2D) g

        // 2. Vẽ các thành phần "Extras" (Level, Time, Nút)
        drawSidebarExtras(g);
    }

    /**
     * Hàm nội bộ (private) để vẽ các thành phần "tĩnh" và tương tác
     * của Sidebar (Level, Time, các nút).
     * <p>
     * Chịu trách nhiệm:
     * <ul>
     * <li>Vẽ văn bản "LEVEL" và "TIME" cùng giá trị của chúng (đã định dạng).</li>
     * <li>Vẽ nút "MENU".</li>
     * <li>Vẽ nút "PAUSE" hoặc "RESUME" tùy thuộc vào trạng thái game hiện tại.</li>
     * <li>Căn giữa (center) văn bản bên trong các nút.</li>
     * </ul>
     *
     * @param g Đối tượng Graphics để vẽ.
     */
    private void drawSidebarExtras(Graphics g) {
        ScalingManager sm = ScalingManager.getInstance();
        int logicX = 980; // 960 (game area) + 20 (padding)
        int currentY = 320; // Vị trí Y (logic) bắt đầu

        // --- 1. Vẽ LEVEL ---
        Font titleFont = new Font("Arial", Font.BOLD, 24);
        Font scaledTitleFont = titleFont.deriveFont((float)(titleFont.getSize() * sm.getScale()));

        Font valueFont = new Font("Arial", Font.PLAIN, 22);
        Font scaledValueFont = valueFont.deriveFont((float)(valueFont.getSize() * sm.getScale()));

        g.setFont(scaledTitleFont);
        g.setColor(Color.WHITE);
        g.drawString("LEVEL", sm.scaleX(logicX), sm.scaleY(currentY));

        currentY += 30; // Tăng Y để vẽ giá trị

        // Lấy số level (index + 1)
        int levelIndex = gameManager.getLevelManager().getCurrentLevelIndex();
        String levelText = "N/A"; // Mặc định

        if (levelIndex == 2) { // Logic cứng: Level 3 (index 2) là màn Boss
            levelText = "Boss";
        } else if (levelIndex >= 0) { // Các level khác
            levelText = String.valueOf(levelIndex + 1);
        }

        g.setFont(scaledValueFont);
        g.drawString(levelText, sm.scaleX(logicX), sm.scaleY(currentY));

        currentY += 50; // Thêm khoảng cách trước khi vẽ TIME

        // --- 2. Vẽ Thời gian (TIME) ---
        g.setFont(scaledTitleFont);
        g.setColor(Color.WHITE);
        g.drawString("TIME", sm.scaleX(logicX), sm.scaleY(currentY));

        currentY += 30;

        g.setFont(scaledValueFont); // Dùng font giá trị

        // Định dạng thời gian của màn chơi hiện tại (MM:SS)
        long totalSeconds = gameManager.getCurrentLevelPlaytimeMillis() / 1000;
        long minutes = totalSeconds / 60;
        long seconds = totalSeconds % 60;
        g.drawString(String.format("%02d:%02d", minutes, seconds), sm.scaleX(logicX), sm.scaleY(currentY));

        // --- 3. Vẽ nút MENU ---
        g.setColor(Color.GRAY); // Nền nút
        g.fillRect(sm.scaleX(menuButtonRect.x), sm.scaleY(menuButtonRect.y),
                sm.scaleWidth(menuButtonRect.width), sm.scaleHeight(menuButtonRect.height));

        g.setColor(Color.WHITE); // Viền nút
        g.drawRect(sm.scaleX(menuButtonRect.x), sm.scaleY(menuButtonRect.y),
                sm.scaleWidth(menuButtonRect.width), sm.scaleHeight(menuButtonRect.height));

        // Căn giữa chữ "MENU"
        g.setFont(scaledValueFont);
        FontMetrics fm = g.getFontMetrics();
        String menuText = "MENU";
        int textWidthScaled = fm.stringWidth(menuText);
        // (Vị trí X nút + (Chiều rộng nút - Chiều rộng chữ) / 2)
        int textX_screen = sm.scaleX(menuButtonRect.x) + (sm.scaleWidth(menuButtonRect.width) - textWidthScaled) / 2;
        // (Vị trí Y nút + (Chiều cao nút - Chiều cao chữ) / 2 + Chiều cao (Ascent) của font)
        int textY_screen = sm.scaleY(menuButtonRect.y) + (sm.scaleHeight(menuButtonRect.height) - fm.getHeight()) / 2 + fm.getAscent();
        g.drawString(menuText, textX_screen, textY_screen);

        // --- 4. VẼ nút PAUSE/RESUME (tùy theo trạng thái) ---
        Rectangle buttonRect;
        String buttonText;
        if (GAMESTATE_PAUSED.equals(gameManager.getGameState())) {
            buttonRect = resumeButtonRect;
            buttonText = "RESUME";
        } else {
            buttonRect = pauseButtonRect;
            buttonText = "PAUSE";
        }

        // Vẽ nền và viền nút
        g.setColor(Color.GRAY);
        g.fillRect(sm.scaleX(buttonRect.x), sm.scaleY(buttonRect.y),
                sm.scaleWidth(buttonRect.width), sm.scaleHeight(buttonRect.height));

        g.setColor(Color.WHITE);
        g.drawRect(sm.scaleX(buttonRect.x), sm.scaleY(buttonRect.y),
                sm.scaleWidth(buttonRect.width), sm.scaleHeight(buttonRect.height));

        // Căn giữa chữ (PAUSE/RESUME)
        textWidthScaled = fm.stringWidth(buttonText);
        textX_screen = sm.scaleX(buttonRect.x) + (sm.scaleWidth(buttonRect.width) - textWidthScaled) / 2;
        textY_screen = sm.scaleY(buttonRect.y) + (sm.scaleHeight(buttonRect.height) - fm.getHeight()) / 2 + fm.getAscent();
        g.drawString(buttonText, textX_screen, textY_screen);
    }

    public Rectangle getPauseButtonRect() { return pauseButtonRect; }
    public Rectangle getResumeButtonRect() { return resumeButtonRect; }
    public Rectangle getMenuButtonRect() { return menuButtonRect; }
}