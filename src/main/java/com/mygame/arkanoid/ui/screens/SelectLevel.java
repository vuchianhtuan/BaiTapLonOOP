package com.mygame.arkanoid.ui.screens;

import com.mygame.arkanoid.core.GameManager;
import com.mygame.arkanoid.engine.AssetManager;
import com.mygame.arkanoid.engine.InputHandler;
import com.mygame.arkanoid.ui.controls.BackButton;
import com.mygame.arkanoid.systems.ScalingManager;

import java.awt.*;
import java.awt.Image;

/**
 * Quản lý logic và hiển thị (render) cho màn hình "Chọn Level" (Select Level).
 * <p>
 * Lớp này chịu trách nhiệm:
 * <ul>
 * <li>Vẽ (render) các hộp (button) đại diện cho từng level.</li>
 * <li>Hiển thị ảnh xem trước (preview) cho mỗi level,
 * tự động cắt (crop) ảnh để vừa với khung (duy trì tỷ lệ khung hình - aspect ratio).</li>
 * <li>Xử lý input (hover và click) để cho phép người dùng
 * bắt đầu game tại một level cụ thể ({@code gameManager.startGameAtLevel})
 * hoặc quay lại menu ({@code BackButton}).</li>
 * </ul>
 */
public class SelectLevel {
    private InputHandler inputHandler;
    private Image BackgroundImage;
    /** Mảng các vùng (bounds) logic (unscaled) cho từng nút chọn level. */
    private Rectangle[] levelButtons = new Rectangle[3];
    /** Mảng các ảnh xem trước (preview) cho từng level,
     * được tải từ AssetManager. */
    private Image[] levelPreviews = new Image[3];
    /** Chỉ số (index) của nút đang được di chuột qua (hover), -1 nếu không có. */
    private int hoveredButton = -1;
    private GameManager gameManager;
    private BackButton backButton;

    /**
     * Khởi tạo trình quản lý màn hình "Chọn Level".
     * <p>
     * Tải các tài sản (assets) cần thiết (nền, ảnh preview)
     * và tính toán vị trí, kích thước logic (unscaled)
     * cho các hộp (button) chọn level, đảm bảo chúng
     * được căn giữa theo chiều ngang (với khoảng cách padding).
     *
     * @param inputHandler Tham chiếu đến InputHandler (để lấy input chuột).
     * @param gameManager  Tham chiếu đến GameManager (để chuyển trạng thái).
     */
    public SelectLevel(InputHandler inputHandler, GameManager gameManager) {
        this.inputHandler = inputHandler;
        this.BackgroundImage = AssetManager.getInstance().getImage("selectLevelBackground");
        this.gameManager = gameManager;
        this.backButton = new BackButton(10, 10, 40, 40);

        int boxWidth = 300;  // Chiều rộng logic của mỗi hộp
        int boxHeight = 400; // Chiều cao logic của mỗi hộp

        // Tính toán khoảng cách để 3 hộp nằm giữa
        int totalBoxWidth = boxWidth * 3;
        int totalPadding = (ScalingManager.getInstance().NATIVE_WIDTH - totalBoxWidth);
        int padding = totalPadding / 4; // 4 khoảng trống (trái, giữa 1-2, giữa 2-3, phải)

        // Căn giữa theo chiều dọc
        int yPos = (ScalingManager.getInstance().NATIVE_HEIGHT - boxHeight) / 2;

        // Lưu trữ vùng bounds logic (dùng tọa độ native)
        this.levelButtons[0] = new Rectangle(padding, yPos, boxWidth, boxHeight);
        this.levelButtons[1] = new Rectangle(padding * 2 + boxWidth, yPos, boxWidth, boxHeight);
        this.levelButtons[2] = new Rectangle(padding * 3 + (boxWidth * 2), yPos, boxWidth, boxHeight);

        // Tải ảnh preview cho mỗi level
        AssetManager am = AssetManager.getInstance();
        this.levelPreviews[0] = am.getImage("level1_preview");
        this.levelPreviews[1] = am.getImage("level2_preview");
        this.levelPreviews[2] = am.getImage("level3_preview");
    }

    /**
     * Cập nhật logic (được gọi mỗi frame bởi GameManager
     * khi {@code gameState == "LEVEL_SELECT"}).
     * <p>
     * Lấy tọa độ chuột (đã unscale) và kiểm tra (hit-test)
     * xem chuột có đang di (hover) hoặc nhấp (click)
     * vào các nút chọn level hoặc nút "Back" hay không.
     */
    public void update() {
        int virtualMouseX = inputHandler.getVirtualMouseX();
        int virtualMouseY = inputHandler.getVirtualMouseY();

        hoveredButton = -1; // Reset hiệu ứng hover mỗi frame

        // Kiểm tra hover và click cho các nút level
        for (int i = 0; i < 3; i++) {
            if (levelButtons[i] != null && levelButtons[i].contains(virtualMouseX, virtualMouseY)) {
                hoveredButton = i; // Đánh dấu đang hover

                if (inputHandler.isMouseClicked()) {
                    gameManager.startGameAtLevel(i); // Bắt đầu màn 0, 1, hoặc 2
                    return; // Thoát sau khi click
                }
            }
        }

        // Kiểm tra click nút "Back"
        if (inputHandler.isMouseClicked() && backButton.contains(virtualMouseX, virtualMouseY)) {
            gameManager.setGameState("MENU");
        }
    }

    /**
     * Vẽ (render) toàn bộ màn hình chọn level.
     * <p>
     * Chịu trách nhiệm vẽ:
     * <ul>
     * <li>Ảnh nền và tiêu đề (căn giữa).</li>
     * <li>Mỗi hộp chọn level (lặp qua {@code levelButtons}).</li>
     * <li><b>Logic Cắt Ảnh (Cropping):</b> Tính toán và cắt
     * (crop) ảnh preview ({@code levelPreviews}) để vừa
     * vặn (fill) với khung hình chữ nhật mà không bị méo
     * (duy trì tỷ lệ khung hình - aspect ratio).</li>
     * <li>Văn bản ("Level 1") được căn giữa trên mỗi hộp.</li>
     * <li>Hiệu ứng viền (border) khi di chuột (hover).</li>
     * <li>Nút "Back".</li>
     * </ul>
     *
     * @param g Đối tượng Graphics để vẽ.
     */
    public void render(Graphics g) {
        ScalingManager sm = ScalingManager.getInstance();
        Graphics2D g2d = (Graphics2D) g; // Dùng Graphics2D để vẽ viền dày (Stroke)

        // 1. Vẽ nền
        if (BackgroundImage != null) {
            g.drawImage(BackgroundImage,
                    sm.scaleX(0), sm.scaleY(0),
                    sm.scaleWidth(sm.NATIVE_WIDTH), sm.scaleHeight(sm.NATIVE_HEIGHT),
                    null);
        }

        // 2. Vẽ Tiêu đề
        Font titleFont = new Font("Arial", Font.BOLD, 48);
        Font scaledTitleFont = titleFont.deriveFont((float)(titleFont.getSize() * sm.getScale()));
        g2d.setFont(scaledTitleFont);
        g2d.setColor(Color.WHITE);

        String title = "SELECT LEVEL";
        FontMetrics fmTitle = g2d.getFontMetrics();
        int titleWidth = fmTitle.stringWidth(title);
        // Căn giữa tiêu đề (dựa trên tọa độ logic)
        g2d.drawString(title,
                sm.scaleX((sm.NATIVE_WIDTH - titleWidth) / 2),
                sm.scaleY(100)); // Vẽ ở vị trí Y (logic) = 100

        // 3. Chuẩn bị Font cho các nút
        Font levelFont = new Font("Arial", Font.BOLD, 36);
        Font scaledLevelFont = levelFont.deriveFont((float)(levelFont.getSize() * sm.getScale()));
        FontMetrics fmLevel = g2d.getFontMetrics(scaledLevelFont); // Dùng metrics của font đã scale

        // 4. Vẽ các nút chọn level
        for (int i = 0; i < levelButtons.length; i++) {
            Rectangle virtualRect = levelButtons[i]; // Lấy vùng logic (unscaled)

            // Tọa độ và kích thước ĐÍCH (trên màn hình, đã scale)
            int x = sm.scaleX(virtualRect.x);
            int y = sm.scaleY(virtualRect.y);
            int width = sm.scaleWidth(virtualRect.width);
            int height = sm.scaleHeight(virtualRect.height);

            Image preview = levelPreviews[i];

            // Vẽ nền nút (ảnh preview hoặc hộp màu)
            if (preview != null && preview.getWidth(null) > 0 && preview.getHeight(null) > 0) {

                // --- Logic Cắt Ảnh (Cropping) để giữ tỷ lệ (Aspect Ratio Fill) ---
                double imgWidth = preview.getWidth(null);
                double imgHeight = preview.getHeight(null);
                double rectWidth = virtualRect.width;
                double rectHeight = virtualRect.height;

                double imgAspect = imgWidth / imgHeight; // Tỷ lệ ảnh
                double rectAspect = rectWidth / rectHeight; // Tỷ lệ khung

                // Tọa độ NGUỒN (Source) (để cắt từ ảnh gốc)
                int sx1 = 0;
                int sy1 = 0;
                int sx2 = (int) imgWidth;
                int sy2 = (int) imgHeight;

                if (imgAspect > rectAspect) {
                    // Ảnh rộng hơn khung -> Cắt bớt trái/phải của ảnh
                    double newWidth = imgHeight * rectAspect; // Chiều rộng mới (logic) của ảnh nguồn
                    sx1 = (int) ((imgWidth - newWidth) / 2); // Cắt 2 bên
                    sx2 = (int) (sx1 + newWidth);
                } else if (imgAspect < rectAspect) {
                    // Ảnh cao hơn khung -> Cắt bớt trên/dưới của ảnh
                    double newHeight = imgWidth / rectAspect; // Chiều cao mới (logic) của ảnh nguồn
                    sy1 = (int) ((imgHeight - newHeight) / 2); // Cắt trên/dưới
                    sy2 = (int) (sy1 + newHeight);
                }
                // (Nếu tỷ lệ bằng nhau, sx/sy giữ nguyên, vẽ toàn bộ ảnh)

                // Vẽ ảnh đã cắt (NGUỒN) vào vùng ĐÍCH (trên màn hình)
                g.drawImage(preview,
                        x, y, x + width, y + height, // Tọa độ ĐÍCH (màn hình)
                        sx1, sy1, sx2, sy2,           // Tọa độ NGUỒN (cắt từ ảnh)
                        null);
            } else {
                // Fallback: Hộp màu tối nếu không có ảnh
                g.setColor(new Color(30, 30, 30, 200));
                g.fillRect(x, y, width, height);
            }

            // Vẽ chữ (Level 1, Level 2...)
            g2d.setFont(scaledLevelFont);
            g2d.setColor(Color.WHITE);
            String text = "Level " + (i + 1);
            int textWidth = fmLevel.stringWidth(text);
            // Căn giữa chữ
            int textX = x + (width - textWidth) / 2;
            int textY = y + (height - fmLevel.getHeight()) / 2 + fmLevel.getAscent();
            g2d.drawString(text, textX, textY);

            // Vẽ viền (và hiệu ứng hover)
            if (i == hoveredButton) {
                g2d.setColor(Color.YELLOW); // Màu hover
                g2d.setStroke(new BasicStroke(sm.scaleWidth(4))); // Viền dày (đã scale)
            } else {
                g2d.setColor(Color.WHITE); // Màu mặc định
                g2d.setStroke(new BasicStroke(sm.scaleWidth(2))); // Viền mỏng (đã scale)
            }
            g.drawRect(x, y, width, height); // Vẽ viền
        }

        // Reset nét vẽ về mặc định
        g2d.setStroke(new BasicStroke(1));

        // 5. Vẽ nút Back
        backButton.draw(g, sm);
    }
}