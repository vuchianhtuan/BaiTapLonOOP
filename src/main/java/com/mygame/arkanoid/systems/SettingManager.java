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
import java.util.Arrays;
import java.util.List;

public class SettingManager {
    private InputHandler inputHandler;
    private GameManager gameManager;
    private SoundManager soundManager;
    private BackButton backButton;
    private Image BackgroundImage;

    // --- Thanh trượt âm lượng (Giữ nguyên) ---
    private Thumb thumbMaster, thumbMusic, thumbSfx;
    private Track trackMaster, trackMusic, trackSfx;
    private Rectangle muteButtonRect;
    private String draggingThumb = null;

    private int titleX = 150;
    private int titleY = 150;
    private int trackWidth = 300;
    private int trackHeight = 20;
    private int thumbWidth = 40;
    private int thumbHeight = 40;
    private int labelX = 150;
    private int trackX = 150;
    private int masterLabelY = 220;
    private int masterTrackY = 260;
    private int musicLabelY = 320;
    private int musicTrackY = 360;
    private int sfxLabelY = 420;
    private int sfxTrackY = 460;
    private int muteLabelY = 540;
    private int muteButtonX = 150;
    private int muteButtonY = 530;
    private int muteButtonSize = 30;

    // --- BỔ SUNG: Skin Selector ---
    private List<String> ballSkinKeys;
    private List<String> paddleSkinKeys;

    private int currentBallSkinIndex = 0;
    private int currentPaddleSkinIndex = 0;

    // Tọa độ UI cho Skin Selector
    private int selectorX = 600; // X chung
    private int arrowSize = 40;
    private int arrowPadding = 10;

    // UI Box cho Ball
    private int ballLabelY = 220;
    private Rectangle ballDisplayBox;
    private Rectangle ballArrowLeft, ballArrowRight;

    // UI Box cho Paddle
    private int paddleLabelY = 420;
    private Rectangle paddleDisplayBox;
    private Rectangle paddleArrowLeft, paddleArrowRight;

    // Animation
    private static final int SLIDE_SPEED = 20; // Tốc độ trượt (pixel mỗi frame)
    private float ballSlideOffset = 0;
    private int ballSlideDirection = 0; // -1 (trái), 1 (phải), 0 (đứng yên)
    private int prevBallSkinIndex = 0;

    private float paddleSlideOffset = 0;
    private int paddleSlideDirection = 0;
    private int prevPaddleSkinIndex = 0;


    public SettingManager(InputHandler inputHandler, GameManager gameManager, SoundManager soundManager) {
        this.inputHandler = inputHandler;
        this.gameManager = gameManager;
        this.soundManager = soundManager;

        this.BackgroundImage = AssetManager.getInstance().getImage("settingBackground");
        this.backButton = new BackButton(10, 10, 40, 40);

        // 1. Khởi tạo thanh Âm lượng (Giữ nguyên)
        this.trackMaster = new Track(trackX, masterTrackY, trackWidth, trackHeight);
        float masterVol = soundManager.getMasterVolume();
        int masterThumbX = calculateThumbX(trackMaster, masterVol);
        int masterThumbY = masterTrackY + (trackHeight / 2) - (thumbHeight / 2);
        this.thumbMaster = new Thumb(masterThumbX, masterThumbY, thumbWidth, thumbHeight);

        this.trackMusic = new Track(trackX, musicTrackY, trackWidth, trackHeight);
        float musicVol = soundManager.getMusicVolume();
        int musicThumbX = calculateThumbX(trackMusic, musicVol);
        int musicThumbY = musicTrackY + (trackHeight / 2) - (thumbHeight / 2);
        this.thumbMusic = new Thumb(musicThumbX, musicThumbY, thumbWidth, thumbHeight);

        this.trackSfx = new Track(trackX, sfxTrackY, trackWidth, trackHeight);
        float sfxVol = soundManager.getSfxVolume();
        int sfxThumbX = calculateThumbX(trackSfx, sfxVol);
        int sfxThumbY = sfxTrackY + (trackHeight / 2) - (thumbHeight / 2);
        this.thumbSfx = new Thumb(sfxThumbX, sfxThumbY, thumbWidth, thumbHeight);

        this.muteButtonRect = new Rectangle(muteButtonX, muteButtonY, muteButtonSize, muteButtonSize);

        // 2. BỔ SUNG: Khởi tạo Skin Selector
        // (Tên key phải khớp với tên bạn đặt trong GameManager.loadAssets())
        ballSkinKeys = Arrays.asList("skin_ball_1", "skin_ball_2");
        paddleSkinKeys = Arrays.asList("skin_paddle_1", "skin_paddle_2");

        // Tìm index skin hiện tại (nếu game được load)
        currentBallSkinIndex = Math.max(0, ballSkinKeys.indexOf(gameManager.getSelectedBallSkinKey()));
        currentPaddleSkinIndex = Math.max(0, paddleSkinKeys.indexOf(gameManager.getSelectedPaddleSkinKey()));
        prevBallSkinIndex = currentBallSkinIndex;
        prevPaddleSkinIndex = currentPaddleSkinIndex;

        // Tọa độ Box Ball
        int ballBoxY = masterTrackY; // Căn theo thanh trượt master
        int ballBoxWidth = 150;
        int ballBoxHeight = 80;
        ballDisplayBox = new Rectangle(selectorX, ballBoxY, ballBoxWidth, ballBoxHeight);
        ballArrowLeft = new Rectangle(selectorX - arrowSize - arrowPadding, ballBoxY + (ballBoxHeight - arrowSize) / 2, arrowSize, arrowSize);
        ballArrowRight = new Rectangle(selectorX + ballBoxWidth + arrowPadding, ballBoxY + (ballBoxHeight - arrowSize) / 2, arrowSize, arrowSize);

        // Tọa độ Box Paddle
        int paddleBoxY = sfxTrackY; // Căn theo thanh trượt sfx
        int paddleBoxWidth = 200; // Paddle rộng hơn
        int paddleBoxHeight = 80;
        paddleDisplayBox = new Rectangle(selectorX - (paddleBoxWidth - ballBoxWidth)/2, paddleBoxY, paddleBoxWidth, paddleBoxHeight); // Căn giữa với box trên
        paddleArrowLeft = new Rectangle(selectorX - (paddleBoxWidth - ballBoxWidth)/2 - arrowSize - arrowPadding, paddleBoxY + (paddleBoxHeight - arrowSize) / 2, arrowSize, arrowSize);
        paddleArrowRight = new Rectangle(selectorX - (paddleBoxWidth - ballBoxWidth)/2 + paddleBoxWidth + arrowPadding, paddleBoxY + (paddleBoxHeight - arrowSize) / 2, arrowSize, arrowSize);
    }

    // Hàm helper để tính toán vị trí X của Thumb
    private int calculateThumbX(Track track, float volume) {
        int x = track.getX() + (int) (track.getWidth() * volume) - (thumbWidth / 2);
        return Math.max(track.getX() - thumbWidth / 2, Math.min(x, track.getX() + track.getWidth() - thumbWidth / 2));
    }

    public void update() {
        int virtualMouseX = inputHandler.getVirtualMouseX();
        int virtualMouseY = inputHandler.getVirtualMouseY();

        // --- THAY ĐỔI: GỌI isMouseClicked() MỘT LẦN DUY NHẤT ---
        boolean isClicked = inputHandler.isMouseClicked();

        // 1. Xử lý nút Back
        if (isClicked && backButton.contains(virtualMouseX, virtualMouseY)) { // <-- Dùng 'isClicked'
            gameManager.setGameState("MENU");
            return;
        }

        // 2. Xử lý nút Mute
        if (isClicked && muteButtonRect.contains(virtualMouseX, virtualMouseY)) { // <-- Dùng 'isClicked'
            soundManager.setMuted(!soundManager.isMuted());
            return;
        }

        // 3. Xử lý kéo thả thanh trượt Âm lượng (Giữ nguyên)
        if (inputHandler.isMousePressed()) {
            if (draggingThumb == null) { // Chỉ kiểm tra khi chưa kéo
                if (thumbMaster.getBounds().contains(virtualMouseX, virtualMouseY) || trackMaster.getBounds().contains(virtualMouseX, virtualMouseY)) {
                    draggingThumb = "MASTER";
                } else if (thumbMusic.getBounds().contains(virtualMouseX, virtualMouseY) || trackMusic.getBounds().contains(virtualMouseX, virtualMouseY)) {
                    draggingThumb = "MUSIC";
                } else if (thumbSfx.getBounds().contains(virtualMouseX, virtualMouseY) || trackSfx.getBounds().contains(virtualMouseX, virtualMouseY)) {
                    draggingThumb = "SFX";
                }
            }
        }

        if (draggingThumb != null) {
            Track currentTrack = null;
            Thumb currentThumb = null;
            if ("MASTER".equals(draggingThumb)) { currentTrack = trackMaster; currentThumb = thumbMaster; }
            else if ("MUSIC".equals(draggingThumb)) { currentTrack = trackMusic; currentThumb = thumbMusic; }
            else if ("SFX".equals(draggingThumb)) { currentTrack = trackSfx; currentThumb = thumbSfx; }

            float percentage = (virtualMouseX - currentTrack.getX()) / (float) currentTrack.getWidth();
            percentage = Math.max(0f, Math.min(1f, percentage));

            int thumbX = calculateThumbX(currentTrack, percentage);
            currentThumb.setX(thumbX);

            if ("MASTER".equals(draggingThumb)) soundManager.setMasterVolume(percentage);
            else if ("MUSIC".equals(draggingThumb)) soundManager.setMusicVolume(percentage);
            else if ("SFX".equals(draggingThumb)) soundManager.setSfxVolume(percentage);
        }

        if (!inputHandler.isMousePressed()) {
            draggingThumb = null;
        }

        // 4. BỔ SUNG: Xử lý Animation trượt (Giữ nguyên)
        if (ballSlideDirection != 0) {
            ballSlideOffset += SLIDE_SPEED;
            if (ballSlideOffset >= ballDisplayBox.width) {
                ballSlideOffset = 0;
                ballSlideDirection = 0;
            }
        }
        if (paddleSlideDirection != 0) {
            paddleSlideOffset += SLIDE_SPEED;
            if (paddleSlideOffset >= paddleDisplayBox.width) {
                paddleSlideOffset = 0;
                paddleSlideDirection = 0;
            }
        }

        // 5. BỔ SUNG: Xử lý Click nút mũi tên
        if (isClicked) { // <-- Dùng 'isClicked'
            if (ballSlideDirection == 0) { // Chỉ cho phép click khi không trượt
                if (ballArrowLeft.contains(virtualMouseX, virtualMouseY)) {
                    prevBallSkinIndex = currentBallSkinIndex;
                    currentBallSkinIndex = (currentBallSkinIndex - 1 + ballSkinKeys.size()) % ballSkinKeys.size();
                    ballSlideDirection = -1; // Trượt sang trái
                    ballSlideOffset = 0;
                    gameManager.setSelectedBallSkinKey(ballSkinKeys.get(currentBallSkinIndex));
                } else if (ballArrowRight.contains(virtualMouseX, virtualMouseY)) {
                    prevBallSkinIndex = currentBallSkinIndex;
                    currentBallSkinIndex = (currentBallSkinIndex + 1) % ballSkinKeys.size();
                    ballSlideDirection = 1; // Trượt sang phải
                    ballSlideOffset = 0;
                    gameManager.setSelectedBallSkinKey(ballSkinKeys.get(currentBallSkinIndex));
                }
            }
            if (paddleSlideDirection == 0) { // Chỉ cho phép click khi không trượt
                if (paddleArrowLeft.contains(virtualMouseX, virtualMouseY)) {
                    prevPaddleSkinIndex = currentPaddleSkinIndex;
                    currentPaddleSkinIndex = (currentPaddleSkinIndex - 1 + paddleSkinKeys.size()) % paddleSkinKeys.size();
                    paddleSlideDirection = -1;
                    paddleSlideOffset = 0;
                    gameManager.setSelectedPaddleSkinKey(paddleSkinKeys.get(currentPaddleSkinIndex));
                } else if (paddleArrowRight.contains(virtualMouseX, virtualMouseY)) {
                    prevPaddleSkinIndex = currentPaddleSkinIndex;
                    currentPaddleSkinIndex = (currentPaddleSkinIndex + 1) % paddleSkinKeys.size();
                    paddleSlideDirection = 1;
                    paddleSlideOffset = 0;
                    gameManager.setSelectedPaddleSkinKey(paddleSkinKeys.get(currentPaddleSkinIndex));
                }
            }
        }
    }
    public void render(Graphics g) {
        ScalingManager sm = ScalingManager.getInstance();
        Graphics2D g2d = (Graphics2D) g;

        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // 1. Vẽ nền, nút Back, Tiêu đề (Giữ nguyên)
        if (BackgroundImage != null) {
            g.drawImage(BackgroundImage, sm.scaleX(0), sm.scaleY(0), sm.scaleWidth(sm.NATIVE_WIDTH), sm.scaleHeight(sm.NATIVE_HEIGHT), null);
        }
        backButton.draw(g, sm);
        Font titleFont = new Font("Arial", Font.BOLD, 40);
        Font scaledTitleFont = titleFont.deriveFont((float)(titleFont.getSize() * sm.getScale()));
        g.setFont(scaledTitleFont);
        g.setColor(Color.WHITE);
        g.drawString("SETTINGS", sm.scaleX(titleX), sm.scaleY(titleY));

        // 2. Vẽ Âm lượng (Giữ nguyên)
        Font labelFont = new Font("Arial", Font.BOLD, 24);
        Font scaledLabelFont = labelFont.deriveFont((float)(labelFont.getSize() * sm.getScale()));
        g.setFont(scaledLabelFont);
        g.setColor(Color.WHITE);

        g.drawString("MASTER VOLUME", sm.scaleX(labelX), sm.scaleY(masterLabelY));
        trackMaster.render(g, sm);
        thumbMaster.render(g, sm);
        g.drawString("MUSIC VOLUME", sm.scaleX(labelX), sm.scaleY(musicLabelY));
        trackMusic.render(g, sm);
        thumbMusic.render(g, sm);
        g.drawString("SFX VOLUME", sm.scaleX(labelX), sm.scaleY(sfxLabelY));
        trackSfx.render(g, sm);
        thumbSfx.render(g, sm);

        g.drawString("MUTE ALL", sm.scaleX(muteButtonX + muteButtonSize + 10), sm.scaleY(muteLabelY + 2));
        g.setColor(Color.WHITE);
        g.drawRect(sm.scaleX(muteButtonRect.x), sm.scaleY(muteButtonRect.y), sm.scaleWidth(muteButtonRect.width), sm.scaleHeight(muteButtonRect.height));
        if (soundManager.isMuted()) {
            g.setFont(scaledLabelFont);
            g.setColor(Color.RED);
            g.drawString("X", sm.scaleX(muteButtonRect.x + 7), sm.scaleY(muteButtonRect.y + 24));
        }

        // 3. BỔ SUNG: Vẽ Skin Selector
        g.setFont(scaledLabelFont);
        g.setColor(Color.WHITE);

        // --- Cụm Ball ---
        g.drawString("BALL SKIN", sm.scaleX(selectorX), sm.scaleY(ballLabelY));
        // Vẽ mũi tên
        g.drawImage(AssetManager.getInstance().getImage("arrow_left"), sm.scaleX(ballArrowLeft.x), sm.scaleY(ballArrowLeft.y), sm.scaleWidth(ballArrowLeft.width), sm.scaleHeight(ballArrowLeft.height), null);
        g.drawImage(AssetManager.getInstance().getImage("arrow_right"), sm.scaleX(ballArrowRight.x), sm.scaleY(ballArrowRight.y), sm.scaleWidth(ballArrowRight.width), sm.scaleHeight(ballArrowRight.height), null);
        // Vẽ khung
        g.drawRect(sm.scaleX(ballDisplayBox.x), sm.scaleY(ballDisplayBox.y), sm.scaleWidth(ballDisplayBox.width), sm.scaleHeight(ballDisplayBox.height));

        // --- Cụm Paddle ---
        g.drawString("PADDLE SKIN", sm.scaleX(selectorX), sm.scaleY(paddleLabelY));
        // Vẽ mũi tên
        g.drawImage(AssetManager.getInstance().getImage("arrow_left"), sm.scaleX(paddleArrowLeft.x), sm.scaleY(paddleArrowLeft.y), sm.scaleWidth(paddleArrowLeft.width), sm.scaleHeight(paddleArrowLeft.height), null);
        g.drawImage(AssetManager.getInstance().getImage("arrow_right"), sm.scaleX(paddleArrowRight.x), sm.scaleY(paddleArrowRight.y), sm.scaleWidth(paddleArrowRight.width), sm.scaleHeight(paddleArrowRight.height), null);
        // Vẽ khung
        g.drawRect(sm.scaleX(paddleDisplayBox.x), sm.scaleY(paddleDisplayBox.y), sm.scaleWidth(paddleDisplayBox.width), sm.scaleHeight(paddleDisplayBox.height));

        // 4. BỔ SUNG: Vẽ Skin với Clipping
        Shape oldClip = g.getClip(); // Lưu lại vùng clip cũ (toàn màn hình)

        // Vẽ Ball (với clipping)
        Rectangle scaledBallBox = new Rectangle(sm.scaleX(ballDisplayBox.x), sm.scaleY(ballDisplayBox.y), sm.scaleWidth(ballDisplayBox.width), sm.scaleHeight(ballDisplayBox.height));
        g.setClip(scaledBallBox); // Chỉ cho phép vẽ BÊN TRONG khung này
        renderSlidingImage(g, sm, ballSkinKeys, currentBallSkinIndex, prevBallSkinIndex, ballDisplayBox, ballSlideOffset, ballSlideDirection, 30); // Giả sử bóng 30x30

        // Vẽ Paddle (với clipping)
        Rectangle scaledPaddleBox = new Rectangle(sm.scaleX(paddleDisplayBox.x), sm.scaleY(paddleDisplayBox.y), sm.scaleWidth(paddleDisplayBox.width), sm.scaleHeight(paddleDisplayBox.height));
        g.setClip(scaledPaddleBox); // Chỉ cho phép vẽ BÊN TRONG khung này
        renderSlidingImage(g, sm, paddleSkinKeys, currentPaddleSkinIndex, prevPaddleSkinIndex, paddleDisplayBox, paddleSlideOffset, paddleSlideDirection, 60); // Giả sử paddle 60x18

        g.setClip(oldClip); // Trả lại vùng clip toàn màn hình
    }

    // --- HÀM HELPER MỚI: Để vẽ hiệu ứng trượt ---
    private void renderSlidingImage(Graphics g, ScalingManager sm, List<String> skins, int currentIndex, int prevIndex, Rectangle box, float slideOffset, int slideDirection, int imageSize) {

        String currentSkinKey = skins.get(currentIndex);
        BufferedImage currentImg = AssetManager.getInstance().getImage(currentSkinKey);

        // Tính toán vị trí X, Y (căn giữa trong box)
        int scaledBoxX = sm.scaleX(box.x);
        int scaledBoxY = sm.scaleY(box.y);
        int scaledBoxWidth = sm.scaleWidth(box.width);
        int scaledBoxHeight = sm.scaleHeight(box.height);

        int scaledImgWidth = sm.scaleWidth(imageSize * 2); // Phóng to skin lên cho dễ nhìn
        int scaledImgHeight = scaledImgWidth;
        if (currentImg.getHeight() < currentImg.getWidth()) { // Xử lý paddle (nó rộng)
            scaledImgWidth = sm.scaleWidth(imageSize * 2);
            scaledImgHeight = sm.scaleHeight( (int) ( (float) currentImg.getHeight() / currentImg.getWidth() * (imageSize * 2) ) );
        }

        int drawY = scaledBoxY + (scaledBoxHeight - scaledImgHeight) / 2;

        if (slideDirection == 0) {
            // Đứng yên: Chỉ vẽ skin hiện tại
            int drawX = scaledBoxX + (scaledBoxWidth - scaledImgWidth) / 2;
            g.drawImage(currentImg, drawX, drawY, scaledImgWidth, scaledImgHeight, null);
        } else {
            // Đang trượt
            String prevSkinKey = skins.get(prevIndex);
            BufferedImage prevImg = AssetManager.getInstance().getImage(prevSkinKey);

            int scaledOffset = sm.scaleWidth((int)slideOffset);

            // 1. Skin MỚI (current)
            int newX = scaledBoxX + (scaledBoxWidth - scaledImgWidth) / 2; // Vị trí cuối
            if (slideDirection == 1) { // Trượt từ phải sang
                newX += (scaledBoxWidth - scaledOffset);
            } else { // Trượt từ trái sang
                newX -= (scaledBoxWidth - scaledOffset);
            }
            g.drawImage(currentImg, newX, drawY, scaledImgWidth, scaledImgHeight, null);

            // 2. Skin CŨ (previous)
            int oldX = scaledBoxX + (scaledBoxWidth - scaledImgWidth) / 2; // Vị trí đầu
            if (slideDirection == 1) { // Bị đẩy sang trái
                oldX -= scaledOffset;
            } else { // Bị đẩy sang phải
                oldX += scaledOffset;
            }
            g.drawImage(prevImg, oldX, drawY, scaledImgWidth, scaledImgHeight, null);
        }
    }
}