package com.mygame.arkanoid.ui.screens;

import com.mygame.arkanoid.core.GameManager;
import com.mygame.arkanoid.engine.AssetManager;
import com.mygame.arkanoid.engine.InputHandler;
import com.mygame.arkanoid.engine.SoundManager;
import com.mygame.arkanoid.ui.controls.BackButton;
import com.mygame.arkanoid.ui.controls.Thumb;
import com.mygame.arkanoid.ui.controls.Track;
import com.mygame.arkanoid.systems.ScalingManager;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.List;

public class SettingManager {
    // --- KHAI BÁO BIẾN ---

    private InputHandler inputHandler;
    private GameManager gameManager;
    private SoundManager soundManager;
    private BackButton backButton;
    private Image BackgroundImage;
    private String selectedBallSkinKey = "skin_ball_1"; // XÓA DÒNG NÀY
    private String selectedPaddleSkinKey = "skin_paddle_1"; // XÓA DÒNG NÀY


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

    // --- BỔ SUNG: Vùng bao phủ cho nhóm Volume ---
    private Rectangle volumeGroupBounds;

    // --- BỔ SUNG: Skin Selector ---
    private List<String> ballSkinKeys;
    private List<String> paddleSkinKeys;

    private int currentBallSkinIndex = 0;
    private int currentPaddleSkinIndex = 0;

    // THAY ĐỔI: Đẩy X chung sang phải để tạo 2 cột
    private int selectorX = 650; // X chung mới
    private int arrowSize = 40;
    private int arrowPadding = 10;

    // UI Box cho Ball
    private int ballLabelY = 220;
    private Rectangle ballDisplayBox;
    private Rectangle ballArrowLeft, ballArrowRight;
    private Rectangle ballGroupBounds; // Vùng bao phủ cho nhóm Ball

    // UI Box cho Paddle
    private int paddleLabelY = 420;
    private Rectangle paddleDisplayBox;
    private Rectangle paddleArrowLeft, paddleArrowRight;
    private Rectangle paddleGroupBounds; // Vùng bao phủ cho nhóm Paddle

    // Bổ sung để căn giữa label
    private int ballLabelCenterX;
    private int paddleLabelCenterX;

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

        // --- BỔ SUNG: Tính toán vùng bao phủ cho nhóm Volume ---
        int groupPadding = 30;
        volumeGroupBounds = new Rectangle(
                Math.min(trackMaster.getX(), muteButtonRect.x) - groupPadding,
                masterLabelY - 40,
                (trackMaster.getX() + trackMaster.getWidth()) - (Math.min(trackMaster.getX(), muteButtonRect.x)) + groupPadding * 2,
                (muteButtonRect.y + muteButtonRect.height) - (masterLabelY - 40) + groupPadding
        );


        // 2. BỔ SUNG: Khởi tạo Skin Selector
        ballSkinKeys = Arrays.asList("skin_ball_1", "skin_ball_2", "skin_ball_3", "skin_ball_4", "skin_ball_5", "skin_ball_6");
        paddleSkinKeys = Arrays.asList("skin_paddle_1", "skin_paddle_2", "skin_paddle_3", "skin_paddle_4");

        currentBallSkinIndex = Math.max(0, ballSkinKeys.indexOf(this.selectedBallSkinKey));
        currentPaddleSkinIndex = Math.max(0, paddleSkinKeys.indexOf(this.selectedPaddleSkinKey));
        prevBallSkinIndex = currentBallSkinIndex;
        prevPaddleSkinIndex = currentPaddleSkinIndex;

        // Tọa độ Box Ball
        int ballBoxY = masterTrackY;
        int ballBoxWidth = 150;
        int ballBoxHeight = 80;
        ballDisplayBox = new Rectangle(selectorX, ballBoxY, ballBoxWidth, ballBoxHeight);
        ballArrowLeft = new Rectangle(selectorX - arrowSize - arrowPadding, ballBoxY + (ballBoxHeight - arrowSize) / 2, arrowSize, arrowSize);
        ballArrowRight = new Rectangle(selectorX + ballBoxWidth + arrowPadding, ballBoxY + (ballBoxHeight - arrowSize) / 2, arrowSize, arrowSize);

        // Tính X căn giữa cho nhãn Ball
        ballLabelCenterX = ballArrowLeft.x + ( (ballArrowRight.x + ballArrowRight.width) - ballArrowLeft.x) / 2;

        // Tính toán vùng bao phủ cho nhóm Ball (ĐÃ CHỈNH SỬA bao gồm chữ)
        int ballGroupTopY = ballLabelY - 30;

        ballGroupBounds = new Rectangle(
                ballArrowLeft.x,
                ballGroupTopY,
                (ballArrowRight.x + ballArrowRight.width) - ballArrowLeft.x,
                Math.max(ballArrowLeft.y + ballArrowLeft.height, ballDisplayBox.y + ballDisplayBox.height) - ballGroupTopY
        );
        // Thêm padding cho viền bao phủ
        ballGroupBounds.x -= groupPadding;
        ballGroupBounds.y -= groupPadding;
        ballGroupBounds.width += groupPadding * 2;
        ballGroupBounds.height += groupPadding * 2;


        // Tọa độ Box Paddle
        int paddleBoxY = sfxTrackY;
        int paddleBoxWidth = 200;
        int paddleBoxHeight = 80;
        paddleDisplayBox = new Rectangle(selectorX - (paddleBoxWidth - ballBoxWidth)/2, paddleBoxY, paddleBoxWidth, paddleBoxHeight);
        paddleArrowLeft = new Rectangle(selectorX - (paddleBoxWidth - ballBoxWidth)/2 - arrowSize - arrowPadding, paddleBoxY + (paddleBoxHeight - arrowSize) / 2, arrowSize, arrowSize);
        paddleArrowRight = new Rectangle(selectorX - (paddleBoxWidth - ballBoxWidth)/2 + paddleBoxWidth + arrowPadding, paddleBoxY + (paddleBoxHeight - arrowSize) / 2, arrowSize, arrowSize);

        // Tính X căn giữa cho nhãn Paddle
        paddleLabelCenterX = paddleArrowLeft.x + ( (paddleArrowRight.x + paddleArrowRight.width) - paddleArrowLeft.x) / 2;

        // Tính toán vùng bao phủ cho nhóm Paddle (ĐÃ CHỈNH SỬA bao gồm chữ)
        int paddleGroupTopY = paddleLabelY - 30;

        paddleGroupBounds = new Rectangle(
                paddleArrowLeft.x,
                paddleGroupTopY,
                (paddleArrowRight.x + paddleArrowRight.width) - paddleArrowLeft.x,
                Math.max(paddleArrowLeft.y + paddleArrowLeft.height, paddleDisplayBox.y + paddleDisplayBox.height) - paddleGroupTopY
        );
        // Thêm padding cho viền bao phủ
        paddleGroupBounds.x -= groupPadding;
        paddleGroupBounds.y -= groupPadding;
        paddleGroupBounds.width += groupPadding * 2;
        paddleGroupBounds.height += groupPadding * 2;
    }

    // Hàm helper để tính toán vị trí X của Thumb (Giữ nguyên)
    private int calculateThumbX(Track track, float volume) {
        int x = track.getX() + (int) (track.getWidth() * volume) - (thumbWidth / 2);
        return Math.max(track.getX() - thumbWidth / 2, Math.min(x, track.getX() + track.getWidth() - thumbWidth / 2));
    }

    // Phương thức Update (Giữ nguyên)
    public void update() {
        int virtualMouseX = inputHandler.getVirtualMouseX();
        int virtualMouseY = inputHandler.getVirtualMouseY();

        boolean isClicked = inputHandler.isMouseClicked();

        // 1. Xử lý nút Back
        if (isClicked && backButton.contains(virtualMouseX, virtualMouseY)) {
            gameManager.setGameState("MENU");
            return;
        }

        // 2. Xử lý nút Mute
        if (isClicked && muteButtonRect.contains(virtualMouseX, virtualMouseY)) {
            soundManager.setMuted(!soundManager.isMuted());
            return;
        }

        // 3. Xử lý kéo thả thanh trượt Âm lượng (Giữ nguyên)
        if (inputHandler.isMousePressed()) {
            if (draggingThumb == null) {
                if (thumbMaster.getBounds().contains(virtualMouseX, virtualMouseY) || trackMaster.getBounds().contains(virtualMouseX, virtualMouseY)) {
                    draggingThumb = "MASTER";
                } else if (thumbMusic.getBounds().contains(virtualMouseX, virtualMouseY) || trackMusic.getBounds().contains(virtualMouseY, virtualMouseY)) {
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

        // 5. BỔ SUNG: Xử lý Click nút mũi tên (Giữ nguyên)
        if (isClicked) {
            if (ballSlideDirection == 0) {
                if (ballArrowLeft.contains(virtualMouseX, virtualMouseY)) {
                    prevBallSkinIndex = currentBallSkinIndex;
                    currentBallSkinIndex = (currentBallSkinIndex - 1 + ballSkinKeys.size()) % ballSkinKeys.size();
                    ballSlideDirection = -1;
                    ballSlideOffset = 0;
                    this.setSelectedBallSkinKey(ballSkinKeys.get(currentBallSkinIndex));
                } else if (ballArrowRight.contains(virtualMouseX, virtualMouseY)) {
                    prevBallSkinIndex = currentBallSkinIndex;
                    currentBallSkinIndex = (currentBallSkinIndex + 1) % ballSkinKeys.size();
                    ballSlideDirection = 1;
                    ballSlideOffset = 0;
                    this.setSelectedBallSkinKey(ballSkinKeys.get(currentBallSkinIndex));
                }
            }
            if (paddleSlideDirection == 0) {
                if (paddleArrowLeft.contains(virtualMouseX, virtualMouseY)) {
                    prevPaddleSkinIndex = currentPaddleSkinIndex;
                    currentPaddleSkinIndex = (currentPaddleSkinIndex - 1 + paddleSkinKeys.size()) % paddleSkinKeys.size();
                    paddleSlideDirection = -1;
                    paddleSlideOffset = 0;
                    this.setSelectedPaddleSkinKey(paddleSkinKeys.get(currentPaddleSkinIndex));
                } else if (paddleArrowRight.contains(virtualMouseX, virtualMouseY)) {
                    prevPaddleSkinIndex = currentPaddleSkinIndex;
                    currentPaddleSkinIndex = (currentPaddleSkinIndex + 1) % paddleSkinKeys.size();
                    paddleSlideDirection = 1;
                    paddleSlideOffset = 0;
                    this.setSelectedPaddleSkinKey(paddleSkinKeys.get(currentPaddleSkinIndex));
                }
            }
        }
    }

    // --- PHƯƠNG THỨC: Vẽ khung nền hiện đại (Chữ nhật) ---
    private void drawModernBox(Graphics g, ScalingManager sm, Rectangle box) {
        Color fillColor = new Color(0, 0, 0, 100);
        Color borderColor = new Color(200, 200, 200, 255);
        int borderThickness = sm.scaleWidth(2);

        int scaledBoxX = sm.scaleX(box.x);
        int scaledBoxY = sm.scaleY(box.y);
        int scaledBoxWidth = sm.scaleWidth(box.width);
        int scaledBoxHeight = sm.scaleHeight(box.height);

        Graphics2D g2d = (Graphics2D) g;

        // 1. Vẽ nền (fill)
        g.setColor(fillColor);
        g.fillRect(scaledBoxX, scaledBoxY, scaledBoxWidth, scaledBoxHeight);

        // 2. Vẽ viền (draw)
        g.setColor(borderColor);
        g2d.setStroke(new BasicStroke(borderThickness));
        g.drawRect(scaledBoxX, scaledBoxY, scaledBoxWidth, scaledBoxHeight);
        g2d.setStroke(new BasicStroke(1));
    }

    // BỔ SUNG: Phương thức vẽ hình ảnh nút với hiệu ứng hover (SÁNG LÊN)
    private void drawButtonImageWithHover(Graphics g, ScalingManager sm, Rectangle bounds, String imageKey, int virtualMouseX, int virtualMouseY) {
        Graphics2D g2d = (Graphics2D) g.create(); // Sử dụng g.create() để thao tác trên bản sao

        int scaledX = sm.scaleX(bounds.x);
        int scaledY = sm.scaleY(bounds.y);
        int scaledWidth = sm.scaleWidth(bounds.width);
        int scaledHeight = sm.scaleHeight(bounds.height);

        boolean isHovering = bounds.contains(virtualMouseX, virtualMouseY);

        // Hiệu ứng dịch chuyển nhỏ (nhấn)
        int pressOffset = isHovering ? sm.scaleWidth(1) : 0;

        int drawX = scaledX + pressOffset;
        int drawY = scaledY + pressOffset;
        int drawWidth = scaledWidth - pressOffset * 2;
        int drawHeight = scaledHeight - pressOffset * 2;

        // 1. Vẽ hình ảnh gốc
        g2d.drawImage(AssetManager.getInstance().getImage(imageKey), drawX, drawY, drawWidth, drawHeight, null);

        // 2. Vẽ lớp phủ sáng lên khi hover
        if (isHovering) {
            // Đặt màu và độ trong suốt cho lớp phủ (ví dụ: Trắng, alpha 80/255)
            Color hoverColor = new Color(255, 255, 255, 80);
            g2d.setColor(hoverColor);

            // Sử dụng AlphaComposite để làm sáng (Lighten/Screen blend mode)
            // Tuy nhiên, cách đơn giản nhất trong Java2D là vẽ một hình chữ nhật trong suốt
            // LƯU Ý: Đây là cách mô phỏng hiệu ứng sáng lên đơn giản nhất.
            g2d.fillRect(scaledX, scaledY, scaledWidth, scaledHeight);
        }

        g2d.dispose(); // Giải phóng tài nguyên Graphics2D
    }


    public void render(Graphics g) {
        ScalingManager sm = ScalingManager.getInstance();
        Graphics2D g2d = (Graphics2D) g;

        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // Lấy vị trí chuột ảo (để xử lý hover)
        int virtualMouseX = inputHandler.getVirtualMouseX();
        int virtualMouseY = inputHandler.getVirtualMouseY();

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


        Font labelFont = new Font("Arial", Font.BOLD, 24);
        Font scaledLabelFont = labelFont.deriveFont((float)(labelFont.getSize() * sm.getScale()));
        g.setFont(scaledLabelFont);
        g.setColor(Color.WHITE);

        // --- 1. VẼ KHUNG CHỮ NHẬT LỚN CHO VOLUME (BÊN TRÁI) ---
        drawModernBox(g, sm, volumeGroupBounds);

        // 2. Vẽ Âm lượng (Đặt bên trong khung Volume)
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

        // --- 2. VẼ KHUNG CHỮ NHẬT LỚN CHO SKIN SELECTOR (BÊN PHẢI) ---
        // Lấy khung từ Ball đến Paddle
        Rectangle skinGroupBounds = new Rectangle(
                ballGroupBounds.x,
                ballGroupBounds.y,
                ballGroupBounds.width,
                (paddleGroupBounds.y + paddleGroupBounds.height) - ballGroupBounds.y
        );

        drawModernBox(g, sm, skinGroupBounds);


        // 3. BỔ SUNG: Vẽ Skin Selector (Bên trong khung Skin)

        // Cụm Ball
        FontMetrics fm = g.getFontMetrics(scaledLabelFont);
        int ballLabelWidth = fm.stringWidth("BALL SKIN");
        int ballLabelDrawX = sm.scaleX(ballLabelCenterX) - ballLabelWidth/2;
        g.drawString("BALL SKIN", ballLabelDrawX, sm.scaleY(ballLabelY));


        // Vẽ mũi tên Ball với HOVER EFFECT
        drawButtonImageWithHover(g, sm, ballArrowLeft, "arrow_left", virtualMouseX, virtualMouseY);
        drawButtonImageWithHover(g, sm, ballArrowRight, "arrow_right", virtualMouseX, virtualMouseY);

        // Cụm Paddle
        int paddleLabelWidth = fm.stringWidth("PADDLE SKIN");
        int paddleLabelDrawX = sm.scaleX(paddleLabelCenterX) - paddleLabelWidth/2;
        g.drawString("PADDLE SKIN", paddleLabelDrawX, sm.scaleY(paddleLabelY));

        // Vẽ mũi tên Paddle với HOVER EFFECT
        drawButtonImageWithHover(g, sm, paddleArrowLeft, "arrow_left", virtualMouseX, virtualMouseY);
        drawButtonImageWithHover(g, sm, paddleArrowRight, "arrow_right", virtualMouseX, virtualMouseY);

        // 4. BỔ SUNG: Vẽ Skin với Clipping (Giữ nguyên)
        Shape oldClip = g.getClip();

        // Vẽ Ball (với clipping)
        Rectangle scaledBallBox = new Rectangle(sm.scaleX(ballDisplayBox.x), sm.scaleY(ballDisplayBox.y), sm.scaleWidth(ballDisplayBox.width), sm.scaleHeight(ballDisplayBox.height));
        g.setClip(scaledBallBox);
        renderSlidingImage(g, sm, ballSkinKeys, currentBallSkinIndex, prevBallSkinIndex, ballDisplayBox, ballSlideOffset, ballSlideDirection, 30);

        // Vẽ Paddle (với clipping)
        Rectangle scaledPaddleBox = new Rectangle(sm.scaleX(paddleDisplayBox.x), sm.scaleY(paddleDisplayBox.y), sm.scaleWidth(paddleDisplayBox.width), sm.scaleHeight(paddleDisplayBox.height));
        g.setClip(scaledPaddleBox);
        renderSlidingImage(g, sm, paddleSkinKeys, currentPaddleSkinIndex, prevPaddleSkinIndex, paddleDisplayBox, paddleSlideOffset, paddleSlideDirection, 60);

        g.setClip(oldClip);
    }

    // --- HÀM HELPER MỚI: Để vẽ hiệu ứng trượt (Giữ nguyên) ---
    private void renderSlidingImage(Graphics g, ScalingManager sm, List<String> skins, int currentIndex, int prevIndex, Rectangle box, float slideOffset, int slideDirection, int imageSize) {

        String currentSkinKey = skins.get(currentIndex);
        BufferedImage currentImg = AssetManager.getInstance().getImage(currentSkinKey);

        int scaledBoxX = sm.scaleX(box.x);
        int scaledBoxY = sm.scaleY(box.y);
        int scaledBoxWidth = sm.scaleWidth(box.width);
        int scaledBoxHeight = sm.scaleHeight(box.height);

        int scaledImgWidth = sm.scaleWidth(imageSize * 2);
        int scaledImgHeight = scaledImgWidth;
        if (currentImg.getHeight() < currentImg.getWidth()) {
            scaledImgWidth = sm.scaleWidth(imageSize * 2);
            scaledImgHeight = sm.scaleHeight( (int) ( (float) currentImg.getHeight() / currentImg.getWidth() * (imageSize * 2) ) );
        }

        int drawY = scaledBoxY + (scaledBoxHeight - scaledImgHeight) / 2;

        if (slideDirection == 0) {
            int drawX = scaledBoxX + (scaledBoxWidth - scaledImgWidth) / 2;
            g.drawImage(currentImg, drawX, drawY, scaledImgWidth, scaledImgHeight, null);
        } else {
            String prevSkinKey = skins.get(prevIndex);
            BufferedImage prevImg = AssetManager.getInstance().getImage(prevSkinKey);

            int scaledOffset = sm.scaleWidth((int)slideOffset);

            // 1. Skin MỚI (current)
            int newX = scaledBoxX + (scaledBoxWidth - scaledImgWidth) / 2;
            if (slideDirection == 1) {
                newX += (scaledBoxWidth - scaledOffset);
            } else {
                newX -= (scaledBoxWidth - scaledOffset);
            }
            g.drawImage(currentImg, newX, drawY, scaledImgWidth, scaledImgHeight, null);

            // 2. Skin CŨ (previous)
            int oldX = scaledBoxX + (scaledBoxWidth - scaledImgWidth) / 2;
            if (slideDirection == 1) {
                oldX -= scaledOffset;
            } else {
                oldX += scaledOffset;
            }
            g.drawImage(prevImg, oldX, drawY, scaledImgWidth, scaledImgHeight, null);
        }
    }

    public String getSelectedBallSkinKey() {
        return selectedBallSkinKey;
    }

    public String getSelectedPaddleSkinKey() {
        return selectedPaddleSkinKey;
    }
    public void setSelectedBallSkinKey(String selectedBallSkinKey) {
        this.selectedBallSkinKey = selectedBallSkinKey;
        gameManager.applyBallSkin(selectedBallSkinKey);
    }

    public void setSelectedPaddleSkinKey(String selectedPaddleSkinKey) {
        this.selectedPaddleSkinKey = selectedPaddleSkinKey;
        gameManager.applyPaddleSkin(selectedPaddleSkinKey);
    }
}