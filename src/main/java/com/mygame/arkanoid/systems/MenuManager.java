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
        int mouseX = inputHandler.getMouseX();
        int mouseY = inputHandler.getMouseY();
        selectedOption = -1;

        for (int i = 0; i < optionBounds.length; i++) {
            if (optionBounds[i] != null && optionBounds[i].contains(mouseX, mouseY)) {
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
                gameManager.setGameState("PLAYING"); // Chuyển trạng thái
                gameManager.startGame();
                break;
            case 1: // High Scores
                System.out.println("High Scores selected - (chưa cài đặt)");
                break;
            case 2: // Exit
                System.exit(0);
                break;
        }
    }

    public void render(Graphics g) {
        if (backgroundImage != null) {
            g.drawImage(backgroundImage, 0, 0, GamePanel.WIDTH, GamePanel.HEIGHT, null);
        } else {
            // Nếu không có ảnh, vẽ nền đen dự phòng
            g.setColor(Color.BLACK);
            g.fillRect(0, 0, GamePanel.WIDTH, GamePanel.HEIGHT);
        }

        // 2. Vẽ tiêu đề game
        /*g.setColor(Color.GREEN);
        g.setFont(new Font("Arial", Font.BOLD, 72));
        String title = "ARKANOID";
        int titleWidth = g.getFontMetrics().stringWidth(title);
        g.drawString(title, (GamePanel.WIDTH - titleWidth) / 2, 150);
         */

        // 3. Vẽ các lựa chọn menu với khung bao quanh
        g.setFont(new Font("Arial", Font.PLAIN, 36));
        for (int i = 0; i < options.length; i++) {
            String optionText = options[i];
            int optionWidth = g.getFontMetrics().stringWidth(optionText);
            int height = g.getFontMetrics().getHeight();

            int x = (GamePanel.WIDTH - optionWidth) / 2;
            int y = 300 + i * 60;

            optionBounds[i] = new Rectangle(x - 20, y - height + 10, optionWidth + 40, height + 10);

            // Vẽ khung và chữ dựa trên việc chuột có đang trỏ vào hay không
            if (i == selectedOption) {
                // Khi được chọn (hover), vẽ nền vàng nhạt và khung vàng đậm
                g.setColor(new Color(255, 255, 0, 100));
                g.fillRoundRect(optionBounds[i].x, optionBounds[i].y, optionBounds[i].width, optionBounds[i].height, 15, 15);

                g.setColor(Color.RED); // Đặt màu cho cả khung và chữ
                g.drawRoundRect(optionBounds[i].x, optionBounds[i].y, optionBounds[i].width, optionBounds[i].height, 15, 15);
                g.drawString(optionText, x, y);
            } else {
                g.setColor(Color.YELLOW);
                g.drawRoundRect(optionBounds[i].x, optionBounds[i].y, optionBounds[i].width, optionBounds[i].height, 15, 15);
                g.drawString(optionText, x, y);
            }
        }
    }
}
