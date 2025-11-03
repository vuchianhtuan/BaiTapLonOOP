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

/**
 * Quản lý logic, trạng thái, và hiển thị (render) cho toàn bộ màn hình Cài đặt (Settings).
 * <p>
 * Lớp này chịu trách nhiệm cho nhiều chức năng:
 * <ul>
 * <li>Xử lý input (kéo, thả, click) cho các thanh trượt (slider) âm lượng và nút Mute.</li>
 * <li>Xử lý input (click) cho các nút mũi tên (arrow) để chọn skin (Ball và Paddle).</li>
 * <li>Quản lý trạng thái (state) của các lựa chọn (âm lượng, skin, v.v.).</li>
 * <li>Vẽ (render) tất cả các thành phần UI, bao gồm các hộp (box) nhóm,
 * các thanh trượt (track/thumb), và hiệu ứng trượt (slide animation) khi đổi skin.</li>
 * <li>Giao tiếp ngược (callback) với {@link GameManager} và {@link SoundManager}
 * để áp dụng các thay đổi ngay lập tức.</li>
 * </ul>
 */
public class SettingManager {
    // --- KHAI BÁO BIẾN ---

    private InputHandler inputHandler;
    private GameManager gameManager;
    private SoundManager soundManager;
    private BackButton backButton;
    private Image BackgroundImage;
    private String selectedBallSkinKey = "skin_ball_1";
    private String selectedPaddleSkinKey = "skin_paddle_1";


    // --- Thanh trượt âm lượng (Volume Sliders) ---
    private Thumb thumbMaster, thumbMusic, thumbSfx;
    private Track trackMaster, trackMusic, trackSfx;
    private Rectangle muteButtonRect;
    /** Trạng thái kéo: "MASTER", "MUSIC", "SFX", hoặc null (nếu không kéo). */
    private String draggingThumb = null;

    // Tọa độ logic (native) cho layout
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

    /** Vùng (bounds) logic bao quanh nhóm UI Âm lượng (để vẽ hộp). */
    private Rectangle volumeGroupBounds;

    // --- Bộ chọn Skin (Skin Selectors) ---
    private List<String> ballSkinKeys;
    private List<String> paddleSkinKeys;

    private int currentBallSkinIndex = 0;
    private int currentPaddleSkinIndex = 0;

    // Tọa độ logic (native) cho layout
    private int selectorX = 650;
    private int arrowSize = 40;
    private int arrowPadding = 10;

    // UI Box cho Ball
    private int ballLabelY = 220;
    private Rectangle ballDisplayBox; // Vùng hiển thị (clipping) skin
    private Rectangle ballArrowLeft, ballArrowRight;
    private Rectangle ballGroupBounds; // Vùng (bounds) logic bao quanh nhóm UI Ball

    // UI Box cho Paddle
    private int paddleLabelY = 420;
    private Rectangle paddleDisplayBox;
    private Rectangle paddleArrowLeft, paddleArrowRight;
    private Rectangle paddleGroupBounds; // Vùng (bounds) logic bao quanh nhóm UI Paddle

    // Tọa độ X (logic) để căn giữa label
    private int ballLabelCenterX;
    private int paddleLabelCenterX;

    // --- Biến trạng thái cho Hiệu ứng Trượt (Slide Animation) ---
    private static final int SLIDE_SPEED = 20; // Tốc độ trượt (pixel logic mỗi frame)
    private float ballSlideOffset = 0;
    private int ballSlideDirection = 0; // -1 (trái), 1 (phải), 0 (đứng yên)
    private int prevBallSkinIndex = 0;

    private float paddleSlideOffset = 0;
    private int paddleSlideDirection = 0;
    private int prevPaddleSkinIndex = 0;


    /**
     * Khởi tạo trình quản lý Cài đặt.
     * <ul>
     * <li>Lưu trữ các tham chiếu (dependencies) đến GameManager, InputHandler, SoundManager.</li>
     * <li>Tải tài sản (assets) nền và khởi tạo BackButton.</li>
     * <li>Khởi tạo các thành phần UI cho nhóm Âm lượng (Track, Thumb)
     * và tính toán vị trí Thumb ban đầu dựa trên cài đặt hiện tại từ SoundManager.</li>
     * <li>Khởi tạo các thành phần UI cho nhóm chọn Skin (Ball, Paddle),
     * bao gồm các hộp hiển thị (display box) và các nút mũi tên (arrow).</li>
     * <li>Tính toán các vùng bao phủ (group bounds) cho mục đích vẽ (render).</li>
     * </ul>
     *
     * @param inputHandler Trình xử lý đầu vào (chuột).
     * @param gameManager  Trình quản lý game (để áp dụng skin).
     * @param soundManager Trình quản lý âm thanh (để đọc/ghi cài đặt âm lượng).
     */
    public SettingManager(InputHandler inputHandler, GameManager gameManager, SoundManager soundManager) {
        this.inputHandler = inputHandler;
        this.gameManager = gameManager;
        this.soundManager = soundManager;

        this.BackgroundImage = AssetManager.getInstance().getImage("settingBackground");
        this.backButton = new BackButton(10, 10, 40, 40);

        // 1. Khởi tạo thanh Âm lượng
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

        // Tính toán vùng bao phủ (bounding box) logic cho nhóm Volume
        int groupPadding = 30;
        volumeGroupBounds = new Rectangle(
                Math.min(trackMaster.getX(), muteButtonRect.x) - groupPadding,
                masterLabelY - 40,
                (trackMaster.getX() + trackMaster.getWidth()) - (Math.min(trackMaster.getX(), muteButtonRect.x)) + groupPadding * 2,
                (muteButtonRect.y + muteButtonRect.height) - (masterLabelY - 40) + groupPadding
        );


        // 2. Khởi tạo Skin Selector
        ballSkinKeys = Arrays.asList("skin_ball_1", "skin_ball_2", "skin_ball_3", "skin_ball_4", "skin_ball_5", "skin_ball_6");
        paddleSkinKeys = Arrays.asList("skin_paddle_1", "skin_paddle_2", "skin_paddle_3", "skin_paddle_4");

        // Đặt index ban đầu (mặc định là 0 nếu không tìm thấy key)
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

        // Tính X (logic) để căn giữa nhãn Ball
        ballLabelCenterX = ballArrowLeft.x + ( (ballArrowRight.x + ballArrowRight.width) - ballArrowLeft.x) / 2;

        // Tính toán vùng bao phủ cho nhóm Ball (bao gồm cả label)
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

        // Tính X (logic) để căn giữa nhãn Paddle
        paddleLabelCenterX = paddleArrowLeft.x + ( (paddleArrowRight.x + paddleArrowRight.width) - paddleArrowLeft.x) / 2;

        // Tính toán vùng bao phủ cho nhóm Paddle
        int paddleGroupTopY = paddleLabelY - 30;
        paddleGroupBounds = new Rectangle(
                paddleArrowLeft.x,
                paddleGroupTopY,
                (paddleArrowRight.x + paddleArrowRight.width) - paddleArrowLeft.x,
                Math.max(paddleArrowLeft.y + paddleArrowLeft.height, paddleDisplayBox.y + paddleDisplayBox.height) - paddleGroupTopY
        );
        // padding cho viền bao phủ
        paddleGroupBounds.x -= groupPadding;
        paddleGroupBounds.y -= groupPadding;
        paddleGroupBounds.width += groupPadding * 2;
        paddleGroupBounds.height += groupPadding * 2;
    }

    /**
     * Hàm tiện ích (helper) để tính toán tọa độ X (logic)
     * của Thumb (nút kéo) dựa trên giá trị âm lượng (0.0f - 1.0f).
     * <p>
     * Đảm bảo Thumb được căn giữa trên giá trị
     * và được kẹp (clamp) trong phạm vi của Track.
     *
     * @param track Đường ray (Track) mà Thumb thuộc về.
     * @param volume Giá trị âm lượng (từ 0.0f đến 1.0f).
     * @return Tọa độ X (logic) đã được tính toán cho Thumb.
     */
    private int calculateThumbX(Track track, float volume) {
        // Tính vị trí tâm
        int x = track.getX() + (int) (track.getWidth() * volume) - (thumbWidth / 2);
        // Kẹp (clamp) giá trị
        return Math.max(track.getX() - thumbWidth / 2, Math.min(x, track.getX() + track.getWidth() - thumbWidth / 2));
    }

    /**
     * Cập nhật logic của màn hình Cài đặt (được gọi mỗi frame
     * bởi GameManager khi {@code gameState == "SETTING"}).
     * <p>
     * Hàm này xử lý toàn bộ logic tương tác của người dùng:
     * <ul>
     * <li>Kiểm tra click nút Back và Mute.</li>
     * <li>Xử lý logic kéo-thả (drag) cho 3 thanh trượt âm lượng:
     * Phát hiện bắt đầu kéo, cập nhật vị trí Thumb và
     * áp dụng âm lượng vào SoundManager theo thời gian thực.</li>
     * <li>Xử lý logic click cho các nút mũi tên (arrow) chọn skin:
     * Chỉ cho phép click khi không có hiệu ứng trượt (slide) đang chạy.
     * Cập nhật index skin, kích hoạt hiệu ứng trượt (đặt {@code ...SlideDirection}),
     * và áp dụng skin mới ngay lập tức.</li>
     * <li>Cập nhật tiến trình của hiệu ứng trượt ({@code ...SlideOffset})
     * nếu chúng đang hoạt động.</li>
     * </ul>
     */
    public void update() {
        int virtualMouseX = inputHandler.getVirtualMouseX();
        int virtualMouseY = inputHandler.getVirtualMouseY();

        boolean isClicked = inputHandler.isMouseClicked(); // Lấy trạng thái click "one-shot"

        // 1. Xử lý nút Back
        if (isClicked && backButton.contains(virtualMouseX, virtualMouseY)) {
            gameManager.setGameState("MENU");
            return;
        }

        // 2. Xử lý nút Mute
        if (isClicked && muteButtonRect.contains(virtualMouseX, virtualMouseY)) {
            soundManager.setMuted(!soundManager.isMuted());
            return; // Xử lý 1 click mỗi frame
        }

        // 3. Xử lý kéo thả thanh trượt Âm lượng
        if (inputHandler.isMousePressed()) {
            // Nếu chuột đang được nhấn VÀ chưa xác định kéo thanh nào
            if (draggingThumb == null) {
                // Kiểm tra xem có bắt đầu kéo thanh MASTER không
                if (thumbMaster.getBounds().contains(virtualMouseX, virtualMouseY) || trackMaster.getBounds().contains(virtualMouseX, virtualMouseY)) {
                    draggingThumb = "MASTER";
                } else if (thumbMusic.getBounds().contains(virtualMouseX, virtualMouseY) || trackMusic.getBounds().contains(virtualMouseX, virtualMouseY)) {
                    draggingThumb = "MUSIC";
                } else if (thumbSfx.getBounds().contains(virtualMouseX, virtualMouseY) || trackSfx.getBounds().contains(virtualMouseX, virtualMouseY)) {
                    draggingThumb = "SFX";
                }
            }
        }

        // Nếu đang trong trạng thái kéo (dragging)
        if (draggingThumb != null) {
            Track currentTrack = null;
            Thumb currentThumb = null;
            if ("MASTER".equals(draggingThumb)) { currentTrack = trackMaster; currentThumb = thumbMaster; }
            else if ("MUSIC".equals(draggingThumb)) { currentTrack = trackMusic; currentThumb = thumbMusic; }
            else if ("SFX".equals(draggingThumb)) { currentTrack = trackSfx; currentThumb = thumbSfx; }

            // Tính toán giá trị % (0.0 - 1.0) dựa trên vị trí chuột
            float percentage = (virtualMouseX - currentTrack.getX()) / (float) currentTrack.getWidth();
            percentage = Math.max(0f, Math.min(1f, percentage)); // Kẹp (clamp)

            // Cập nhật vị trí X của Thumb
            int thumbX = calculateThumbX(currentTrack, percentage);
            currentThumb.setX(thumbX);

            // Áp dụng âm lượng
            if ("MASTER".equals(draggingThumb)) soundManager.setMasterVolume(percentage);
            else if ("MUSIC".equals(draggingThumb)) soundManager.setMusicVolume(percentage);
            else if ("SFX".equals(draggingThumb)) soundManager.setSfxVolume(percentage);
        }

        // Nếu nhả chuột, dừng trạng thái kéo
        if (!inputHandler.isMousePressed()) {
            draggingThumb = null;
        }

        // --- Cập nhật hiệu ứng trượt (Slide Animation) ---
        // (Đây là logic cập nhật, không phải logic input)
        if (ballSlideDirection != 0) {
            ballSlideOffset += SLIDE_SPEED;
            if (ballSlideOffset >= ballDisplayBox.width) {
                ballSlideOffset = 0; // Hoàn thành
                ballSlideDirection = 0; // Dừng
            }
        }
        if (paddleSlideDirection != 0) {
            paddleSlideOffset += SLIDE_SPEED;
            if (paddleSlideOffset >= paddleDisplayBox.width) {
                paddleSlideOffset = 0;
                paddleSlideDirection = 0;
            }
        }

        // 4. Xử lý click chọn Skin Ball và Paddle
        if (isClicked) {
            // Chỉ xử lý click Ball nếu không đang trượt
            if (ballSlideDirection == 0) {
                if (ballArrowLeft.contains(virtualMouseX, virtualMouseY)) {
                    prevBallSkinIndex = currentBallSkinIndex; // Lưu index cũ
                    currentBallSkinIndex = (currentBallSkinIndex - 1 + ballSkinKeys.size()) % ballSkinKeys.size();
                    ballSlideDirection = -1; // Kích hoạt trượt sang trái
                    ballSlideOffset = 0;
                    this.setSelectedBallSkinKey(ballSkinKeys.get(currentBallSkinIndex));
                } else if (ballArrowRight.contains(virtualMouseX, virtualMouseY)) {
                    prevBallSkinIndex = currentBallSkinIndex;
                    currentBallSkinIndex = (currentBallSkinIndex + 1) % ballSkinKeys.size();
                    ballSlideDirection = 1; // Kích hoạt trượt sang phải
                    ballSlideOffset = 0;
                    this.setSelectedBallSkinKey(ballSkinKeys.get(currentBallSkinIndex));
                }
            }
            // Chỉ xử lý click Paddle nếu không đang trượt
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

    /**
     * Hàm tiện ích (helper) để vẽ một hộp (box) UI "hiện đại".
     * Vẽ một hình chữ nhật nền (màu đen bán trong suốt)
     * và một hình chữ nhật viền (màu sáng).
     *
     * @param g Graphics context.
     * @param sm ScalingManager.
     * @param box Vùng (Rectangle) logic (unscaled) của hộp cần vẽ.
     */
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
        g2d.setStroke(new BasicStroke(1)); // Reset nét vẽ
    }

    /**
     * Hàm tiện ích (helper) để vẽ một nút bằng hình ảnh (ví dụ: mũi tên)
     * với các hiệu ứng tương tác (hover).
     * <ul>
     * <li>Vẽ hình ảnh.</li>
     * <li>Nếu chuột đang hover (di qua): Áp dụng hiệu ứng 'nhấn'
     * (dịch chuyển 1px) và vẽ một lớp phủ (overlay)
     * màu trắng bán trong suốt để làm sáng nút.</li>
     * </ul>
     *
     * @param g Graphics context.
     * @param sm ScalingManager.
     * @param bounds Vùng (Rectangle) logic (unscaled) của nút.
     * @param imageKey Asset key của hình ảnh nút.
     * @param virtualMouseX Tọa độ X (logic) của chuột (để kiểm tra hover).
     * @param virtualMouseY Tọa độ Y (logic) của chuột (để kiểm tra hover).
     */
    private void drawButtonImageWithHover(Graphics g, ScalingManager sm, Rectangle bounds, String imageKey, int virtualMouseX, int virtualMouseY) {
        Graphics2D g2d = (Graphics2D) g.create(); // Sử dụng g.create() để thao tác trên bản sao

        int scaledX = sm.scaleX(bounds.x);
        int scaledY = sm.scaleY(bounds.y);
        int scaledWidth = sm.scaleWidth(bounds.width);
        int scaledHeight = sm.scaleHeight(bounds.height);

        boolean isHovering = bounds.contains(virtualMouseX, virtualMouseY);

        // Hiệu ứng dịch chuyển nhỏ (nhấn) khi hover
        int pressOffset = isHovering ? sm.scaleWidth(1) : 0;

        int drawX = scaledX + pressOffset;
        int drawY = scaledY + pressOffset;
        int drawWidth = scaledWidth - pressOffset * 2;
        int drawHeight = scaledHeight - pressOffset * 2;

        // 1. Vẽ hình ảnh gốc
        g2d.drawImage(AssetManager.getInstance().getImage(imageKey), drawX, drawY, drawWidth, drawHeight, null);

        // 2. Vẽ lớp phủ sáng lên khi hover
        if (isHovering) {
            Color hoverColor = new Color(255, 255, 255, 80); // Trắng, 80/255 alpha
            g2d.setColor(hoverColor);
            g2d.fillRect(scaledX, scaledY, scaledWidth, scaledHeight);
        }

        g2d.dispose(); // Giải phóng tài nguyên Graphics2D
    }


    /**
     * Vẽ (render) toàn bộ màn hình Cài đặt
     * (được gọi mỗi frame bởi GamePanel).
     * <p>
     * Quy trình vẽ được chia thành nhiều phần:
     * Nền, Tiêu đề, Nhóm Âm lượng, và Nhóm Chọn Skin.
     * <p>
     * Sử dụng các hàm helper như {@link #drawModernBox}
     * và {@link #drawButtonImageWithHover}.
     * <p>
     * Phần vẽ Skin sử dụng kỹ thuật "clipping" ({@code g.setClip})
     * để đảm bảo hiệu ứng trượt (slide) chỉ hiển thị
     * bên trong hộp (display box) của nó.
     *
     * @param g Đối tượng Graphics để vẽ.
     */
    public void render(Graphics g) {
        ScalingManager sm = ScalingManager.getInstance();
        Graphics2D g2d = (Graphics2D) g;

        // Bật khử răng cưa
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // Lấy vị trí chuột ảo (để xử lý hover)
        int virtualMouseX = inputHandler.getVirtualMouseX();
        int virtualMouseY = inputHandler.getVirtualMouseY();

        // 1. Vẽ nền, nút Back, Tiêu đề
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

        // --- 2. Vẽ nhóm Âm lượng ---
        drawModernBox(g, sm, volumeGroupBounds); // Vẽ hộp nền

        // Vẽ các thanh trượt
        g.drawString("MASTER VOLUME", sm.scaleX(labelX), sm.scaleY(masterLabelY));
        trackMaster.render(g, sm);
        thumbMaster.render(g, sm);
        g.drawString("MUSIC VOLUME", sm.scaleX(labelX), sm.scaleY(musicLabelY));
        trackMusic.render(g, sm);
        thumbMusic.render(g, sm);
        g.drawString("SFX VOLUME", sm.scaleX(labelX), sm.scaleY(sfxLabelY));
        trackSfx.render(g, sm);
        thumbSfx.render(g, sm);

        // Vẽ nút Mute
        g.drawString("MUTE ALL", sm.scaleX(muteButtonX + muteButtonSize + 10), sm.scaleY(muteLabelY + 2));
        g.setColor(Color.WHITE);
        g.drawRect(sm.scaleX(muteButtonRect.x), sm.scaleY(muteButtonRect.y), sm.scaleWidth(muteButtonRect.width), sm.scaleHeight(muteButtonRect.height));
        if (soundManager.isMuted()) {
            g.setFont(scaledLabelFont);
            g.setColor(Color.RED);
            g.drawString("X", sm.scaleX(muteButtonRect.x + 7), sm.scaleY(muteButtonRect.y + 24));
        }

        // --- 3. Vẽ nhóm Chọn Skin ---

        // Tạo một vùng bao phủ (bounds) chung cho cả 2 nhóm Ball và Paddle
        Rectangle skinGroupBounds = new Rectangle(
                ballGroupBounds.x,
                ballGroupBounds.y,
                ballGroupBounds.width,
                (paddleGroupBounds.y + paddleGroupBounds.height) - ballGroupBounds.y
        );
        drawModernBox(g, sm, skinGroupBounds); // Vẽ hộp nền chung

        // Cụm Ball
        FontMetrics fm = g.getFontMetrics(scaledLabelFont);
        int ballLabelWidth = fm.stringWidth("BALL SKIN");
        int ballLabelDrawX = sm.scaleX(ballLabelCenterX) - ballLabelWidth/2; // Căn giữa
        g.drawString("BALL SKIN", ballLabelDrawX, sm.scaleY(ballLabelY));
        // Vẽ mũi tên Ball (với hiệu ứng hover)
        drawButtonImageWithHover(g, sm, ballArrowLeft, "arrow_left", virtualMouseX, virtualMouseY);
        drawButtonImageWithHover(g, sm, ballArrowRight, "arrow_right", virtualMouseX, virtualMouseY);

        // Cụm Paddle
        int paddleLabelWidth = fm.stringWidth("PADDLE SKIN");
        int paddleLabelDrawX = sm.scaleX(paddleLabelCenterX) - paddleLabelWidth/2; // Căn giữa
        g.drawString("PADDLE SKIN", paddleLabelDrawX, sm.scaleY(paddleLabelY));
        // Vẽ mũi tên Paddle (với hiệu ứng hover)
        drawButtonImageWithHover(g, sm, paddleArrowLeft, "arrow_left", virtualMouseX, virtualMouseY);
        drawButtonImageWithHover(g, sm, paddleArrowRight, "arrow_right", virtualMouseX, virtualMouseY);

        // --- 4. Vẽ Skin Previews với Clipping và Animation ---
        Shape oldClip = g.getClip(); // Lưu lại vùng clipping cũ

        // Vẽ Ball (với clipping)
        Rectangle scaledBallBox = new Rectangle(sm.scaleX(ballDisplayBox.x), sm.scaleY(ballDisplayBox.y), sm.scaleWidth(ballDisplayBox.width), sm.scaleHeight(ballDisplayBox.height));
        g.setClip(scaledBallBox); // Áp dụng vùng clipping
        renderSlidingImage(g, sm, ballSkinKeys, currentBallSkinIndex, prevBallSkinIndex, ballDisplayBox, ballSlideOffset, ballSlideDirection, 30);

        // Vẽ Paddle (với clipping)
        Rectangle scaledPaddleBox = new Rectangle(sm.scaleX(paddleDisplayBox.x), sm.scaleY(paddleDisplayBox.y), sm.scaleWidth(paddleDisplayBox.width), sm.scaleHeight(paddleDisplayBox.height));
        g.setClip(scaledPaddleBox); // Áp dụng vùng clipping mới
        renderSlidingImage(g, sm, paddleSkinKeys, currentPaddleSkinIndex, prevPaddleSkinIndex, paddleDisplayBox, paddleSlideOffset, paddleSlideDirection, 60);

        g.setClip(oldClip); // Khôi phục vùng clipping cũ
    }

    /**
     * Hàm tiện ích (helper) phức tạp để vẽ (render)
     * hiệu ứng trượt (slide) khi đổi skin.
     * <p>
     * Nếu không có hiệu ứng ({@code slideDirection == 0}),
     * chỉ vẽ skin hiện tại ({@code currentIndex})
     * vào giữa hộp.
     * <p>
     * Nếu có hiệu ứng ({@code slideDirection != 0}),
     * hàm này sẽ vẽ cả skin cũ ({@code prevIndex})
     * và skin mới ({@code currentIndex})
     * và dịch chuyển (offset) cả hai dựa trên {@code slideOffset}
     * để tạo ảo giác skin cũ bị 'đẩy' ra bởi skin mới.
     * <p>
     * (Lưu ý: Hàm này phải được gọi
     * bên trong một vùng 'clipping' (g.setClip) đã được thiết lập từ trước).
     *
     * @param g Graphics context
     * @param sm ScalingManager
     * @param skins Danh sách các asset key (Ball hoặc Paddle)
     * @param currentIndex Index của skin mới (đang trượt vào)
     * @param prevIndex Index của skin cũ (đang trượt ra)
     * @param box Vùng (Rectangle) logic (unscaled) của display box
     * @param slideOffset Độ dịch chuyển (logic) hiện tại của hiệu ứng (0 -> box.width)
     * @param slideDirection Hướng trượt (-1 sang trái, 1 sang phải)
     * @param imageSize Kích thước (logic) mong muốn của hình ảnh (sẽ được scale)
     */
    private void renderSlidingImage(Graphics g, ScalingManager sm, List<String> skins, int currentIndex, int prevIndex, Rectangle box, float slideOffset, int slideDirection, int imageSize) {

        String currentSkinKey = skins.get(currentIndex);
        BufferedImage currentImg = AssetManager.getInstance().getImage(currentSkinKey);

        // Tọa độ (scaled) của hộp clipping
        int scaledBoxX = sm.scaleX(box.x);
        int scaledBoxY = sm.scaleY(box.y);
        int scaledBoxWidth = sm.scaleWidth(box.width);
        int scaledBoxHeight = sm.scaleHeight(box.height);

        // Tính toán kích thước (scaled) của ảnh (căn chỉnh theo chiều rộng/cao)
        int scaledImgWidth = sm.scaleWidth(imageSize * 2);
        int scaledImgHeight = scaledImgWidth;
        if (currentImg.getHeight() < currentImg.getWidth()) {
            scaledImgWidth = sm.scaleWidth(imageSize * 2);
            scaledImgHeight = sm.scaleHeight( (int) ( (float) currentImg.getHeight() / currentImg.getWidth() * (imageSize * 2) ) );
        }

        // Căn giữa ảnh theo chiều Y
        int drawY = scaledBoxY + (scaledBoxHeight - scaledImgHeight) / 2;

        if (slideDirection == 0) {
            // --- Không có hiệu ứng: Chỉ vẽ skin hiện tại ---
            int drawX = scaledBoxX + (scaledBoxWidth - scaledImgWidth) / 2;
            g.drawImage(currentImg, drawX, drawY, scaledImgWidth, scaledImgHeight, null);
        } else {
            // --- Đang có hiệu ứng trượt ---
            String prevSkinKey = skins.get(prevIndex);
            BufferedImage prevImg = AssetManager.getInstance().getImage(prevSkinKey);

            int scaledOffset = sm.scaleWidth((int)slideOffset);

            // 1. Vẽ Skin MỚI (current)
            int newX = scaledBoxX + (scaledBoxWidth - scaledImgWidth) / 2;
            if (slideDirection == 1) { // Trượt sang phải
                newX += (scaledBoxWidth - scaledOffset); // Bắt đầu từ phải, đi vào
            } else { // Trượt sang trái
                newX -= (scaledBoxWidth - scaledOffset); // Bắt đầu từ trái, đi vào
            }
            g.drawImage(currentImg, newX, drawY, scaledImgWidth, scaledImgHeight, null);

            // 2. Vẽ Skin CŨ (previous)
            int oldX = scaledBoxX + (scaledBoxWidth - scaledImgWidth) / 2;
            if (slideDirection == 1) {
                oldX -= scaledOffset; // Đi từ giữa, trượt ra trái
            } else {
                oldX += scaledOffset; // Đi từ giữa, trượt ra phải
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

    /**
     * Đặt skin bóng được chọn.
     * Ngay lập tức gọi {@code gameManager.applyBallSkin}
     * để áp dụng thay đổi (live preview).
     *
     * @param selectedBallSkinKey Key (mã) của skin mới.
     */
    public void setSelectedBallSkinKey(String selectedBallSkinKey) {
        this.selectedBallSkinKey = selectedBallSkinKey;
        gameManager.applyBallSkin(selectedBallSkinKey);
    }

    /**
     * Đặt skin paddle được chọn.
     * Ngay lập tức gọi {@code gameManager.applyPaddleSkin}
     * để áp dụng thay đổi (live preview).
     *
     * @param selectedPaddleSkinKey Key (mã) của skin mới.
     */
    public void setSelectedPaddleSkinKey(String selectedPaddleSkinKey) {
        this.selectedPaddleSkinKey = selectedPaddleSkinKey;
        gameManager.applyPaddleSkin(selectedPaddleSkinKey);
    }
}