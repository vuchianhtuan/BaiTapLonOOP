package com.mygame.arkanoid.systems;

import com.mygame.arkanoid.core.GameManager;
import com.mygame.arkanoid.engine.AssetManager;
import com.mygame.arkanoid.engine.InputHandler;
import com.mygame.arkanoid.engine.SoundManager;
import com.mygame.arkanoid.objects.BackButton;
import com.mygame.arkanoid.objects.Thumb;
import com.mygame.arkanoid.objects.Track;

import java.awt.*;
import java.awt.image.BufferedImage;

public class SetupVolume {
    private InputHandler inputHandler;
    private GameManager gameManager;
    private SoundManager soundManager;
    private Thumb thumb ;
    private Track track;
    private BackButton backButton;
    private Image BackgroundImage;


    public SetupVolume(InputHandler inputHandler, GameManager gameManager, SoundManager soundManager,Track track, Thumb thumb) {
        this.inputHandler = inputHandler;
        this.gameManager = gameManager;
        this.soundManager = soundManager;
        this.track = track;
        this.thumb = thumb;
        this.BackgroundImage = AssetManager.getInstance().getImage("setupVolumeBackground");
        this.backButton = new BackButton(10, 10, 40, 40);
    }

    public void update() {
        int virtualMouseX = inputHandler.getVirtualMouseX();
        int virtualMouseY = inputHandler.getVirtualMouseY();

        if (inputHandler.isMouseClicked() && backButton.contains(virtualMouseX, virtualMouseY)) {

            gameManager.setGameState("MENU");
        }

        thumb.update(inputHandler, soundManager, track);

    }

    public void render(Graphics g) {
        ScalingManager sm = ScalingManager.getInstance();

        // --- SỬA ĐỔI KHỐI NÀY ---
        if (BackgroundImage != null) {
            g.drawImage(BackgroundImage,
                    sm.scaleX(0), sm.scaleY(0), // <-- Bắt đầu từ offset
                    sm.scaleWidth(sm.NATIVE_WIDTH),
                    sm.scaleHeight(sm.NATIVE_HEIGHT),
                    null);
        }
        // --- KẾT THÚC SỬA ĐỔI --- (Không cần else, vì GamePanel đã vẽ nền đen)

        backButton.draw(g, sm);

        String text = "MUSIC VOLUME";
        g.setColor(Color.WHITE);

        // --- SỬA ĐỔI KHỐI FONT ---
        Font baseFont = new Font("Arial", Font.BOLD, 30);
        Font scaledFont = baseFont.deriveFont((float)(baseFont.getSize() * sm.getScale()));
        g.setFont(scaledFont); // <-- Dùng font đã scale
        // --- KẾT THÚC SỬA ĐỔI FONT ---

        g.drawString(text, sm.scaleX(150), sm.scaleY(150));

        track.render(g, sm);
        thumb.render(g, sm);
    }
}
