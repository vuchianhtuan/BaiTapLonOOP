package com.mygame.arkanoid.systems.menu;

import com.mygame.arkanoid.core.GameManager;
import com.mygame.arkanoid.engine.AssetManager;
import com.mygame.arkanoid.engine.InputHandler;
import com.mygame.arkanoid.systems.helper.ScalingManager;

import java.awt.*;
import java.awt.geom.RoundRectangle2D; // Import để vẽ hình chữ nhật bo tròn đẹp hơn
import java.awt.image.BufferedImage;

public class MenuManager {
    private final GameManager gameManager;
    private final InputHandler inputHandler;
    private final String[] options = {"New Game", "High Scores", "Select Level", "Settings", "Exit"};
    private int selectedOption = -1; // Index của nút đang được hover
    private boolean continueAvailable = false;
    private Rectangle[] optionBounds; // Khu vực logic để click
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

    public MenuManager(GameManager gameManager, InputHandler inputHandler) {
        this.gameManager = gameManager;
        this.inputHandler = inputHandler;
        this.optionBounds = new Rectangle[options.length];
        AssetManager am = AssetManager.getInstance();
        this.backgroundImage = am.getImage("menuBackground");
    }

    public void setContinueAvailable(boolean continueAvailable) {
        this.continueAvailable = continueAvailable;
    }

    public void onStartButtonClicked() {
        if (continueAvailable) {
            gameManager.continueGame();
        } else {
            gameManager.startGame();
        }
    }

    public void update() {
        int virtualMouseX = inputHandler.getVirtualMouseX();
        int virtualMouseY = inputHandler.getVirtualMouseY();
        selectedOption = -1;

        // Kiểm tra hover dựa trên khu vực logic đã lưu
        for (int i = 0; i < optionBounds.length; i++) {
            if (optionBounds[i] != null && optionBounds[i].contains(virtualMouseX, virtualMouseY)) {
                selectedOption = i;
                break;
            }
        }

        // Xử lý click
        if (inputHandler.isMouseClicked() && selectedOption != -1) {
            selectOption();
        }
    }

    private void selectOption() {
        switch (selectedOption) {
            case 0: onStartButtonClicked(); break;
            case 1: gameManager.setGameState("HIGH_SCORES"); break;
            case 2: gameManager.setGameState("LEVEL_SELECT"); break;
            case 3: gameManager.setGameState("SETTING"); break;
            case 4: System.exit(0); break;
        }
    }

    // --- HÀM RENDER ĐƯỢC THIẾT KẾ LẠI ---
    public void render(Graphics g) {
        ScalingManager sm = ScalingManager.getInstance();
        Graphics2D g2d = (Graphics2D) g; // Dùng Graphics2D

        // Bật khử răng cưa
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // 1. Vẽ nền (Đã sửa letterboxing)
        if (backgroundImage != null) {
            g.drawImage(backgroundImage,
                    sm.scaleX(0), sm.scaleY(0),
                    sm.scaleWidth(sm.NATIVE_WIDTH), sm.scaleHeight(sm.NATIVE_HEIGHT),
                    null);
        } else {
            g.setColor(Color.BLACK);
            g.fillRect(
                    sm.scaleX(0), sm.scaleY(0),
                    sm.scaleWidth(sm.NATIVE_WIDTH), sm.scaleHeight(sm.NATIVE_HEIGHT));
        }

        // 2. Chuẩn bị Font
        Font baseFont = new Font("Calibri", Font.BOLD, 28); // Giảm cỡ chữ 1 chút
        Font scaledFont = baseFont.deriveFont((float)(baseFont.getSize() * sm.getScale()));
        g.setFont(scaledFont);
        FontMetrics fm = g.getFontMetrics(); // Lấy metrics TỪ FONT ĐÃ SCALE

        int startY = 250; // Dịch nút lên cao hơn một chút

        // 3. Vẽ từng nút
        for (int i = 0; i < options.length; i++) {
            String optionText = options[i];
            if (i == 0) { // Xử lý nút New Game / Continue
                optionText = continueAvailable ? "Continue Game" : "New Game";
            }

            // Tính toán vị trí LOGIC để nút căn giữa theo chiều ngang
            int logicX = (sm.NATIVE_WIDTH - buttonWidth) / 2;
            int logicY = startY + i * buttonSpacing;

            // Lưu khu vực logic để click
            optionBounds[i] = new Rectangle(logicX, logicY, buttonWidth, buttonHeight);

            // Tọa độ và kích thước ĐÃ SCALE để vẽ
            int drawX = sm.scaleX(logicX);
            int drawY = sm.scaleY(logicY);
            int drawW = sm.scaleWidth(buttonWidth);
            int drawH = sm.scaleHeight(buttonHeight);
            int drawArc = sm.scaleWidth(buttonArc); // Scale cả độ bo tròn

            // 4. Vẽ nền nút (có hiệu ứng hover)
            if (i == selectedOption) {
                g.setColor(buttonHoverColor);
            } else {
                g.setColor(buttonColor);
            }
            // Vẽ nền bo tròn
            g2d.fill(new RoundRectangle2D.Float(drawX, drawY, drawW, drawH, drawArc, drawArc));

            // 5. Vẽ viền nút
            g.setColor(buttonBorderColor);
            g2d.setStroke(new BasicStroke(sm.scaleWidth(3))); // Độ dày viền
            // Vẽ viền bo tròn
            g2d.draw(new RoundRectangle2D.Float(drawX, drawY, drawW, drawH, drawArc, drawArc));
            g2d.setStroke(new BasicStroke(1)); // Reset nét vẽ

            int textWidth = fm.stringWidth(optionText);
            // Tọa độ X, Y của chữ chính (căn giữa nút)
            int mainTextX = drawX + (drawW - textWidth) / 2;
            int mainTextY = drawY + (drawH - fm.getHeight()) / 2 + fm.getAscent();

            // --- THÊM VIỀN CHỮ ---
            Color outlineColor = Color.BLACK; // Chọn màu viền (ví dụ: đen)
            int outlineOffset = sm.scaleWidth(2); // Độ dày viền (đã scale)

            // Vẽ chữ lệch ở 4 góc với màu viền
            g.setColor(outlineColor);
            g.drawString(optionText, mainTextX - outlineOffset, mainTextY - outlineOffset);
            g.drawString(optionText, mainTextX + outlineOffset, mainTextY - outlineOffset);
            g.drawString(optionText, mainTextX - outlineOffset, mainTextY + outlineOffset);
            g.drawString(optionText, mainTextX + outlineOffset, mainTextY + outlineOffset);
            // (Bạn có thể thêm 4 hướng nữa: trên, dưới, trái, phải nếu muốn viền dày hơn)
             g.drawString(optionText, mainTextX - outlineOffset, mainTextY);
             g.drawString(optionText, mainTextX + outlineOffset, mainTextY);
             g.drawString(optionText, mainTextX, mainTextY - outlineOffset);
             g.drawString(optionText, mainTextX, mainTextY + outlineOffset);
            // --- KẾT THÚC VIỀN CHỮ ---

            // Vẽ chữ chính (màu gốc) đè lên trên viền
            g.setColor(textColor); // Màu chữ trắng (hoặc màu bạn đã định nghĩa)
            g.drawString(optionText, mainTextX, mainTextY);
        }
    }
}