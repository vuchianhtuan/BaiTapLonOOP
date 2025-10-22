package com.mygame.arkanoid.systems;

import com.mygame.arkanoid.engine.Renderer;
import com.mygame.arkanoid.engine.InputHandler;
import com.mygame.arkanoid.core.GameManager;
import com.mygame.arkanoid.core.GamePanel;
import com.mygame.arkanoid.engine.AssetManager;

import java.awt.*;

public class MenuManager {
    private final GameManager gameManager;
    private final InputHandler inputHandler;
    private final String[] options = {"New Game", "High Scores", "Exit", "Setup", "Select Level"};
    private int selectedOption = -1;
    private boolean continueAvailable = false;
    private Rectangle[] optionBounds;
    private Image backgroundImage;

    public MenuManager(GameManager gameManager, InputHandler inputHandler) {
        this.gameManager = gameManager;
        this.inputHandler = inputHandler;
        this.optionBounds = new Rectangle[options.length];
        this.backgroundImage = AssetManager.getInstance().getImage("menuBackground");
    }

    public void setContinueAvailable(boolean continueAvailable) {
        this.continueAvailable = continueAvailable;
    }

    public String getStartButtonLabel() {
        return continueAvailable ? "Continue Game" : "New Game";
    }

    // Gọi khi người dùng click vào nút Start
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
                //gameManager.startGame();
                onStartButtonClicked();
                break;
            case 1: // High Scores
                gameManager.setGameState("HIGH_SCORES");
                break;
            case 2: // Exit
                System.exit(0);
                break;
            case 3: // Setup
                gameManager.setGameState("SETUP");
                break;
            case 4: // Level Select
                gameManager.setGameState("LEVEL_SELECT");
                break;
        }
    }

    public void render(Graphics g) {
        ScalingManager sm = ScalingManager.getInstance();

        if (backgroundImage != null) {
            g.drawImage(backgroundImage,
                    sm.scaleX(0), sm.scaleY(0),
                    sm.scaleWidth(sm.NATIVE_WIDTH),
                    sm.scaleHeight(sm.NATIVE_HEIGHT),
                    null);
        } else {
            g.setColor(Color.BLACK);
            g.fillRect(
                    sm.scaleX(0), sm.scaleY(0),
                    sm.scaleWidth(sm.NATIVE_WIDTH),
                    sm.scaleHeight(sm.NATIVE_HEIGHT));
        }

        Font baseFont = new Font("Arial", Font.BOLD, 36);
        Font scaledFont = baseFont.deriveFont((float)(baseFont.getSize() * sm.getScale()));
        g.setFont(scaledFont);

        for (int i = 0; i < options.length; i++) {
            if (i == 0 && continueAvailable) {
                options[0] = "Continue Game";
            } else if (i == 0) {
                options[0] = "New Game";
            }
            String optionText = options[i];

            FontMetrics fm = g.getFontMetrics(); // <-- LẤY METRICS TỪ FONT ĐÃ SET
            int optionWidth = fm.stringWidth(optionText);
            int height = fm.getHeight();

            // Tính toán tọa độ logic (đã chính xác)
            int logicX = (sm.NATIVE_WIDTH - optionWidth) / 2;
            int logicY = 300 + i * 60;

            // --- SỬA LOGIC TÍNH HÌNH CHỮ NHẬT ---
            // Căn lề hình chữ nhật dựa trên font metrics
            int logicRectX = logicX - 20;
            int logicRectY = logicY - height + fm.getDescent(); // Căn chuẩn hơn
            int logicRectWidth = optionWidth + 40;
            int logicRectHeight = height + 10;
            // --- KẾT THÚC SỬA ---

            optionBounds[i] = new Rectangle(logicRectX, logicRectY, logicRectWidth, logicRectHeight);

            if (i == selectedOption) {
                g.setColor(new Color(255, 255, 0, 100));
                g.fillRoundRect(sm.scaleX(logicRectX), sm.scaleY(logicRectY), sm.scaleWidth(logicRectWidth), sm.scaleHeight(logicRectHeight), 15, 15);
                g.setColor(Color.RED);
            } else {
                g.setColor(Color.YELLOW);
            }

            g.drawRoundRect(sm.scaleX(logicRectX), sm.scaleY(logicRectY), sm.scaleWidth(logicRectWidth), sm.scaleHeight(logicRectHeight), 15, 15);
            g.drawString(optionText, sm.scaleX(logicX), sm.scaleY(logicY));
        }
    }
}
