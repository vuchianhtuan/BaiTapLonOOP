package com.mygame.arkanoid.systems;

import com.mygame.arkanoid.engine.Renderer;
import com.mygame.arkanoid.engine.InputHandler;
import com.mygame.arkanoid.core.GameManager;
import com.mygame.arkanoid.core.GamePanel;
import com.mygame.arkanoid.engine.AssetManager;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.Image;

public class MenuManager {
    private final GameManager gameManager;
    private final InputHandler inputHandler;
    private final String[] options = {"Start Game", "High Scores", "Exit"};
    private int selectedOption = -1;
    private Rectangle[] optionBounds;
    private Image backgroundImage;

    public MenuManager(GameManager gameManager, InputHandler inputHandler) {
        this.gameManager = gameManager;
        this.inputHandler = inputHandler;
        this.optionBounds = new Rectangle[options.length];
        this.backgroundImage = AssetManager.getInstance().getImage("menuBackground");
    }

    public void update() {
        int virtualMouseX = inputHandler.getVirtualMouseX();
        int virtualMouseY = inputHandler.getVirtualMouseY();
        selectedOption = -1;

        // So sánh chuột ảo với khu vực bấm ảo
        for (int i = 0; i < optionBounds.length; i++) {
            if (optionBounds[i] != null && optionBounds[i].contains(virtualMouseX, virtualMouseY)) {
                selectedOption = i;
                break;
            }
        }

        if (inputHandler.isMouseClicked() && selectedOption != -1) {
            selectOption();
        }
    }

    private void selectOption() {
        switch (selectedOption) {
            case 0: // Start Game
                gameManager.startGame();
                break;
            case 1: // High Scores
                gameManager.setGameState("HIGH_SCORES");
                System.out.println("High Scores selected - (chưa cài đặt)");
                break;
            case 2: // Exit
                System.exit(0);
                break;
        }
    }

    public void render(Graphics g) {
        ScalingManager sm = ScalingManager.getInstance();
        if (backgroundImage != null) {
            g.drawImage(backgroundImage, 0, 0, sm.scaleWidth(sm.NATIVE_WIDTH), sm.scaleHeight(sm.NATIVE_HEIGHT), null);
        } else {
            // Nếu không có ảnh, vẽ nền đen dự phòng
            g.setColor(Color.BLACK);
            g.fillRect(0, 0, sm.scaleWidth(sm.NATIVE_WIDTH), sm.scaleHeight(sm.NATIVE_HEIGHT));
        }

        g.setFont(new Font("Arial", Font.PLAIN, 36));
        for (int i = 0; i < options.length; i++) {
            String optionText = options[i];
            int optionWidth = g.getFontMetrics().stringWidth(optionText);
            int height = g.getFontMetrics().getHeight();

            // Tính toán tọa độ và kích thước trong thế giới LOGIC (ảo) 1280x720
            int logicX = (sm.NATIVE_WIDTH - optionWidth) / 2;
            int logicY = 300 + i * 60;
            int logicRectX = logicX - 20;
            int logicRectY = logicY - height + 10;
            int logicRectWidth = optionWidth + 40;
            int logicRectHeight = height + 10;

            // Lưu lại khu vực bấm LOGIC
            optionBounds[i] = new Rectangle(logicRectX, logicRectY, logicRectWidth, logicRectHeight);

            // Chỉ khi VẼ, chúng ta mới "dịch" các giá trị logic ra màn hình thật
            if (i == selectedOption) {
                g.setColor(new Color(255, 255, 0, 100)); // Màu nền khi hover
                g.fillRoundRect(sm.scaleX(logicRectX), sm.scaleY(logicRectY), sm.scaleWidth(logicRectWidth), sm.scaleHeight(logicRectHeight), 15, 15);

                g.setColor(Color.RED); // Màu chữ và viền khi hover
            } else {
                g.setColor(Color.YELLOW); // Màu chữ và viền mặc định
            }

            // Vẽ viền và chữ đã được scale
            g.drawRoundRect(sm.scaleX(logicRectX), sm.scaleY(logicRectY), sm.scaleWidth(logicRectWidth), sm.scaleHeight(logicRectHeight), 15, 15);
            g.drawString(optionText, sm.scaleX(logicX), sm.scaleY(logicY));
        }
    }
}
