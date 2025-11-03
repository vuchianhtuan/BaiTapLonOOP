package com.mygame.arkanoid.ui.screens;

import com.mygame.arkanoid.core.GameManager;
import com.mygame.arkanoid.engine.AssetManager;
import com.mygame.arkanoid.engine.InputHandler;
import com.mygame.arkanoid.systems.ScalingManager;

import java.awt.*;
import java.awt.geom.RoundRectangle2D; // Import để vẽ hình chữ nhật bo tròn đẹp hơn
import java.awt.image.BufferedImage;

/**
 * Lớp MenuManager quản lý logic và hiển thị (render) của menu chính.
 * <p>
 * Chịu trách nhiệm vẽ các nút, xử lý việc di chuột (hover)
 * và nhấp chuột (click) của người dùng để điều hướng
 * và chuyển đổi trạng thái trò chơi (GameState).
 */
public class MenuManager {
    private final GameManager gameManager;
    private final InputHandler inputHandler;
    private final String[] options = {"New Game", "High Scores", "Select Level", "Settings", "Exit"};
    /** Index của nút đang được di chuột qua (hover), -1 nếu không có. */
    private int selectedOption = -1;
    /** Cờ (flag) quyết định nút đầu tiên là "New Game" hay "Continue Game". */
    private boolean continueAvailable = false;
    /**
     * Mảng lưu trữ tọa độ logic (unscaled) của các nút.
     * Được dùng để kiểm tra va chạm chuột (hit-testing).
     * Mảng này được cập nhật lại mỗi khi gọi {@link #render}.
     */
    private Rectangle[] optionBounds;
    private BufferedImage backgroundImage;

    // --- Màu sắc và kích thước cho nút mới ---
    private Color buttonColor = new Color(50, 150, 50); // Xanh lá cây
    private Color buttonHoverColor = new Color(80, 180, 80); // Xanh lá cây sáng hơn khi hover
    private Color buttonBorderColor = new Color(20, 80, 20); // Viền xanh lá cây tối
    private Color textColor = Color.WHITE; // Màu chữ
    private int buttonWidth = 300; // Chiều rộng logic của nút
    private int buttonHeight = 55; // Chiều cao logic của nút
    private int buttonArc = 25; // Độ bo tròn góc
    private int buttonSpacing = 70; // Khoảng cách Y giữa các nút
    // --- Kết thúc ---

    /**
     * Khởi tạo trình quản lý menu.
     *
     * @param gameManager  Tham chiếu đến GameManager (để chuyển trạng thái).
     * @param inputHandler Tham chiếu đến InputHandler (để lấy input chuột).
     */
    public MenuManager(GameManager gameManager, InputHandler inputHandler) {
        this.gameManager = gameManager;
        this.inputHandler = inputHandler;
        this.optionBounds = new Rectangle[options.length];
        AssetManager am = AssetManager.getInstance();
        this.backgroundImage = am.getImage("menuBackground");
    }

    /**
     * Đặt cờ (flag) cho biết nút "Continue" có nên được hiển thị hay không.
     * <p>
     * Được gọi bởi {@link GameManager} khi game được tạm dừng (pause)
     * hoặc một file save được tải (load).
     *
     * @param continueAvailable {@code true} nếu có thể tiếp tục, {@code false} nếu không.
     */
    public void setContinueAvailable(boolean continueAvailable) {
        this.continueAvailable = continueAvailable;
    }

    /**
     * Xử lý logic khi nút đầu tiên (index 0) được nhấn.
     * Kiểm tra cờ {@code continueAvailable} để quyết định
     * gọi {@code continueGame()} hay {@code startGame()}.
     */
    public void onStartButtonClicked() {
        if (continueAvailable) {
            gameManager.continueGame();
        } else {
            gameManager.startGame();
        }
    }

    /**
     * Cập nhật logic của menu (được gọi mỗi frame bởi GameManager
     * khi {@code gameState == "MENU"}).
     * <p>
     * Chịu trách nhiệm:
     * <ol>
     * <li>Lấy tọa độ chuột (đã unscale).</li>
     * <li>Kiểm tra (hit-test) xem chuột đang di (hover) trên nút nào
     * và cập nhật {@code selectedOption}.</li>
     * <li>Kiểm tra (poll) {@code isMouseClicked} và gọi {@link #selectOption()}
     * nếu có click.</li>
     * </ol>
     */
    public void update() {
        // Lấy tọa độ logic (ảo) của chuột
        int virtualMouseX = inputHandler.getVirtualMouseX();
        int virtualMouseY = inputHandler.getVirtualMouseY();
        selectedOption = -1; // Đặt lại trạng thái hover

        // Kiểm tra hover dựa trên khu vực logic đã lưu (optionBounds)
        for (int i = 0; i < optionBounds.length; i++) {
            if (optionBounds[i] != null && optionBounds[i].contains(virtualMouseX, virtualMouseY)) {
                selectedOption = i;
                break; // Chỉ có thể hover 1 nút mỗi lần
            }
        }

        // Xử lý click (chỉ khi click được phát hiện VÀ đang hover 1 nút)
        if (inputHandler.isMouseClicked() && selectedOption != -1) {
            selectOption();
        }
    }

    /**
     * Thực thi hành động (action) dựa trên {@code selectedOption} (nút đã được click).
     * Thường là gọi {@code gameManager.setGameState()} để chuyển màn hình.
     */
    private void selectOption() {
        switch (selectedOption) {
            case 0: onStartButtonClicked(); break;
            case 1: gameManager.setGameState("HIGH_SCORES"); break;
            case 2: gameManager.setGameState("LEVEL_SELECT"); break;
            case 3: gameManager.setGameState("SETTING"); break;
            case 4: System.exit(0); break;
        }
    }

    /**
     * Vẽ (render) toàn bộ màn hình menu.
     * <p>
     * Chịu trách nhiệm vẽ nền, sau đó lặp (loop) qua tất cả các {@code options}
     * để vẽ từng nút (bo góc, có viền, và hiệu ứng hover).
     * <p>
     * <b>Lưu ý:</b> Tọa độ logic {@code optionBounds} được tính toán lại
     * (re-calculated) bên trong vòng lặp render này.
     *
     * @param g Đối tượng Graphics (sẽ được cast sang Graphics2D).
     */
    public void render(Graphics g) {
        ScalingManager sm = ScalingManager.getInstance();
        Graphics2D g2d = (Graphics2D) g;

        // Bật khử răng cưa (anti-aliasing) để hình ảnh mượt mà
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // 1. Vẽ nền (đã được co giãn (scale) để vừa vặn)
        if (backgroundImage != null) {
            g.drawImage(backgroundImage,
                    sm.scaleX(0), sm.scaleY(0),
                    sm.scaleWidth(sm.NATIVE_WIDTH), sm.scaleHeight(sm.NATIVE_HEIGHT),
                    null);
        } else {
            // Fallback: Vẽ nền đen nếu ảnh lỗi
            g.setColor(Color.BLACK);
            g.fillRect(
                    sm.scaleX(0), sm.scaleY(0),
                    sm.scaleWidth(sm.NATIVE_WIDTH), sm.scaleHeight(sm.NATIVE_HEIGHT));
        }

        // 2. Chuẩn bị Font (lấy font gốc và scale nó)
        Font baseFont = new Font("Calibri", Font.BOLD, 28);
        Font scaledFont = baseFont.deriveFont((float)(baseFont.getSize() * sm.getScale()));
        g.setFont(scaledFont);
        FontMetrics fm = g.getFontMetrics(); // Lấy metrics TỪ FONT ĐÃ SCALE

        int startY = 250; // Vị trí Y (logic) của nút đầu tiên

        // 3. Vẽ từng nút
        for (int i = 0; i < options.length; i++) {
            String optionText = options[i];
            // Xử lý đặc biệt cho nút đầu tiên
            if (i == 0) {
                optionText = continueAvailable ? "Continue Game" : "New Game";
            }

            // Tính toán vị trí LOGIC để nút căn giữa theo chiều ngang
            int logicX = (sm.NATIVE_WIDTH - buttonWidth) / 2;
            int logicY = startY + i * buttonSpacing;

            // QUAN TRỌNG: Cập nhật lại vùng bounds logic (dùng để check click)
            optionBounds[i] = new Rectangle(logicX, logicY, buttonWidth, buttonHeight);

            // Tọa độ và kích thước ĐÃ SCALE (dùng để vẽ)
            int drawX = sm.scaleX(logicX);
            int drawY = sm.scaleY(logicY);
            int drawW = sm.scaleWidth(buttonWidth);
            int drawH = sm.scaleHeight(buttonHeight);
            int drawArc = sm.scaleWidth(buttonArc); // Scale cả độ bo tròn

            // 4. Vẽ nền nút (thay đổi màu nếu đang hover)
            if (i == selectedOption) {
                g.setColor(buttonHoverColor);
            } else {
                g.setColor(buttonColor);
            }
            // Vẽ nền (hình chữ nhật bo tròn)
            g2d.fill(new RoundRectangle2D.Float(drawX, drawY, drawW, drawH, drawArc, drawArc));

            // 5. Vẽ viền nút
            g.setColor(buttonBorderColor);
            g2d.setStroke(new BasicStroke(sm.scaleWidth(3))); // Độ dày viền (đã scale)
            g2d.draw(new RoundRectangle2D.Float(drawX, drawY, drawW, drawH, drawArc, drawArc));
            g2d.setStroke(new BasicStroke(1)); // Reset nét vẽ về mặc định

            // 6. Tính toán vị trí để căn giữa văn bản bên trong nút
            int textWidth = fm.stringWidth(optionText);
            int mainTextX = drawX + (drawW - textWidth) / 2;
            int mainTextY = drawY + (drawH - fm.getHeight()) / 2 + fm.getAscent();

            // --- 7. Kỹ thuật vẽ viền chữ (text outline) ---
            // Bằng cách vẽ chữ 8 lần với màu viền (đen) ở các vị trí
            // lân cận (offset) trước khi vẽ chữ chính (trắng).
            Color outlineColor = Color.BLACK;
            int outlineOffset = sm.scaleWidth(2); // Độ dày viền (đã scale)

            // Vẽ 8 hướng (trên, dưới, trái, phải, và 4 góc)
            g.setColor(outlineColor);
            g.drawString(optionText, mainTextX - outlineOffset, mainTextY - outlineOffset);
            g.drawString(optionText, mainTextX + outlineOffset, mainTextY - outlineOffset);
            g.drawString(optionText, mainTextX - outlineOffset, mainTextY + outlineOffset);
            g.drawString(optionText, mainTextX + outlineOffset, mainTextY + outlineOffset);
            g.drawString(optionText, mainTextX - outlineOffset, mainTextY);
            g.drawString(optionText, mainTextX + outlineOffset, mainTextY);
            g.drawString(optionText, mainTextX, mainTextY - outlineOffset);
            g.drawString(optionText, mainTextX, mainTextY + outlineOffset);
            // --- Kết thúc viền chữ ---

            // 8. Vẽ chữ chính (màu trắng) đè lên trên
            g.setColor(textColor);
            g.drawString(optionText, mainTextX, mainTextY);
        }
    }
}