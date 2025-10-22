package com.mygame.arkanoid.systems;

import com.mygame.arkanoid.core.GameManager;
import com.mygame.arkanoid.engine.AssetManager;
import com.mygame.arkanoid.engine.InputHandler;
import com.mygame.arkanoid.objects.BackButton;

import java.awt.*;
import java.awt.image.BufferedImage;

public class SelectLevel {
    private InputHandler inputHandler;
    private LevelManager levelManager;
    private Image BackgroundImage;
    private Rectangle[] levelButtons = new Rectangle[4];
    private GameManager gameManager;
    private BackButton backButton;

    public SelectLevel(InputHandler inputHandler, GameManager gameManager, LevelManager levelManager) {
        this.inputHandler = inputHandler;
        this.levelManager = levelManager;
        this.BackgroundImage = AssetManager.getInstance().getImage("selectLevelBackground");
        this.gameManager = gameManager;
        this.backButton = new BackButton(10, 10, 40, 40);
        this.levelButtons[0] = new Rectangle(100, 50, 500, 300); // Placeholder
        this.levelButtons[1] = new Rectangle(680, 50, 500, 300); // Placeholder
        this.levelButtons[2] = new Rectangle(100, 370, 500, 300); // Placeholder
        this.levelButtons[3] = new Rectangle(680, 370, 500, 300); // Placeholder
    }

    public void update() {
        int virtualMouseX = inputHandler.getVirtualMouseX();
        int virtualMouseY = inputHandler.getVirtualMouseY();

        for (int i = 0; i < 4; i++) {
            if (levelButtons[i] != null && levelButtons[i].contains(virtualMouseX, virtualMouseY)) {
                if (inputHandler.isMouseClicked()) {
                    gameManager.startGameAtLevel(i);
                }
            }
        }

        if (inputHandler.isMouseClicked() && backButton.contains(virtualMouseX, virtualMouseY)) {
            gameManager.setGameState("MENU");
        }
    }

    public void render(Graphics g) {
        ScalingManager sm = ScalingManager.getInstance();

        if (BackgroundImage != null) {
            g.drawImage(BackgroundImage, 0, 0, sm.scaleWidth(sm.NATIVE_WIDTH), sm.scaleHeight(sm.NATIVE_HEIGHT), null);
        }

        // Vẽ các nút chọn level
        for (int i = 0; i < levelButtons.length; i++) {
            Rectangle virtualRect = levelButtons[i];

            int x = sm.scaleX(virtualRect.x);
            int y = sm.scaleY(virtualRect.y);
            int width = sm.scaleWidth(virtualRect.width);
            int height = sm.scaleHeight(virtualRect.height);

            g.setColor(Color.LIGHT_GRAY);
            g.fillRect(x, y, width, height);
            g.setColor(Color.BLACK);
            g.drawRect(x, y, width, height);

            g.drawString("Level " + (i + 1), x + sm.scaleWidth(50), y + sm.scaleHeight(55));
        }

        backButton.draw(g, sm);
    }
}
