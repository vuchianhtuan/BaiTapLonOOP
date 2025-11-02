package com.mygame.arkanoid.systems.menu;

import com.mygame.arkanoid.core.GameManager;
import com.mygame.arkanoid.engine.AssetManager;
import com.mygame.arkanoid.engine.InputHandler;
import com.mygame.arkanoid.objects.ui.BackButton;
import com.mygame.arkanoid.systems.level.LevelManager;
import com.mygame.arkanoid.systems.helper.ScalingManager;

import java.awt.*;
import java.awt.Image;

public class SelectLevel {
    private InputHandler inputHandler;
    private Image BackgroundImage;
    private Rectangle[] levelButtons = new Rectangle[3];
    private Image[] levelPreviews = new Image[3]; // <-- MỚI: Dành cho ảnh preview
    private int hoveredButton = -1; // <-- MỚI: Dành cho hiệu ứng hover
    private GameManager gameManager;
    private BackButton backButton;

    public SelectLevel(InputHandler inputHandler, GameManager gameManager) {
        this.inputHandler = inputHandler;
        this.BackgroundImage = AssetManager.getInstance().getImage("selectLevelBackground");
        this.gameManager = gameManager;
        this.backButton = new BackButton(10, 10, 40, 40);

        // --- BẮT ĐẦU THIẾT KẾ LAYOUT MỚI ---

        int boxWidth = 300;  // Chiều rộng logic của mỗi hộp
        int boxHeight = 400; // Chiều cao logic của mỗi hộp

        // Tính toán khoảng cách để 3 hộp nằm giữa
        int totalBoxWidth = boxWidth * 3;
        int totalPadding = (ScalingManager.getInstance().NATIVE_WIDTH - totalBoxWidth);
        int padding = totalPadding / 4; // 4 khoảng trống (trái, giữa 1-2, giữa 2-3, phải)

        // Căn giữa theo chiều dọc
        int yPos = (ScalingManager.getInstance().NATIVE_HEIGHT - boxHeight) / 2;

        this.levelButtons[0] = new Rectangle(padding, yPos, boxWidth, boxHeight);
        this.levelButtons[1] = new Rectangle(padding * 2 + boxWidth, yPos, boxWidth, boxHeight);
        this.levelButtons[2] = new Rectangle(padding * 3 + (boxWidth * 2), yPos, boxWidth, boxHeight);

        // --- KẾT THÚC LAYOUT MỚI ---

        // (Tùy chọn) Load ảnh preview
        // Để dùng, bạn cần thêm 3 ảnh này vào hàm loadAssets() của GameManager
        AssetManager am = AssetManager.getInstance();
        this.levelPreviews[0] = am.getImage("level1_preview");
        this.levelPreviews[1] = am.getImage("level2_preview");
        this.levelPreviews[2] = am.getImage("level3_preview");
    }

    public void update() {
        int virtualMouseX = inputHandler.getVirtualMouseX();
        int virtualMouseY = inputHandler.getVirtualMouseY();

        hoveredButton = -1; // Reset hiệu ứng hover

        for (int i = 0; i < 3; i++) {
            if (levelButtons[i] != null && levelButtons[i].contains(virtualMouseX, virtualMouseY)) {
                hoveredButton = i; // Đang hover

                if (inputHandler.isMouseClicked()) {
                    gameManager.startGameAtLevel(i); // Bắt đầu màn 0, 1, hoặc 2
                    return; // Thoát sau khi click
                }
            }
        }

        if (inputHandler.isMouseClicked() && backButton.contains(virtualMouseX, virtualMouseY)) {
            gameManager.setGameState("MENU");
        }
    }

    public void render(Graphics g) {
        ScalingManager sm = ScalingManager.getInstance();
        Graphics2D g2d = (Graphics2D) g; // Dùng Graphics2D

        // 1. Vẽ nền (Đã sửa lỗi letterboxing)
        if (BackgroundImage != null) {
            g.drawImage(BackgroundImage,
                    sm.scaleX(0), sm.scaleY(0),
                    sm.scaleWidth(sm.NATIVE_WIDTH), sm.scaleHeight(sm.NATIVE_HEIGHT),
                    null);
        }

        // 2. Vẽ Tiêu đề (MỚI)
        Font titleFont = new Font("Arial", Font.BOLD, 48);
        Font scaledTitleFont = titleFont.deriveFont((float)(titleFont.getSize() * sm.getScale()));
        g2d.setFont(scaledTitleFont);
        g2d.setColor(Color.WHITE);

        String title = "SELECT LEVEL";
        FontMetrics fmTitle = g2d.getFontMetrics();
        int titleWidth = fmTitle.stringWidth(title);
        // Căn giữa tiêu đề
        g2d.drawString(title,
                sm.scaleX((sm.NATIVE_WIDTH - titleWidth) / 2),
                sm.scaleY(100)); // Vẽ ở vị trí Y=100

        // 3. Chuẩn bị Font cho các nút
        Font levelFont = new Font("Arial", Font.BOLD, 36);
        Font scaledLevelFont = levelFont.deriveFont((float)(levelFont.getSize() * sm.getScale()));
        FontMetrics fmLevel = g2d.getFontMetrics(scaledLevelFont);

        // 4. Vẽ các nút chọn level
        for (int i = 0; i < levelButtons.length; i++) {
            Rectangle virtualRect = levelButtons[i];

            // Tọa độ và kích thước ĐÍCH (trên màn hình)
            int x = sm.scaleX(virtualRect.x);
            int y = sm.scaleY(virtualRect.y);
            int width = sm.scaleWidth(virtualRect.width);
            int height = sm.scaleHeight(virtualRect.height);

            Image preview = levelPreviews[i];

            // Vẽ nền nút (ảnh preview hoặc hộp màu)
            if (preview != null && preview.getWidth(null) > 0 && preview.getHeight(null) > 0) {

                // --- BẮT ĐẦU LOGIC CẮT ẢNH (ASPECT FILL) ---

                double imgWidth = preview.getWidth(null);
                double imgHeight = preview.getHeight(null);
                double rectWidth = virtualRect.width;
                double rectHeight = virtualRect.height;

                double imgAspect = imgWidth / imgHeight;
                double rectAspect = rectWidth / rectHeight;

                int sx1 = 0;
                int sy1 = 0;
                int sx2 = (int) imgWidth;
                int sy2 = (int) imgHeight;

                if (imgAspect > rectAspect) {
                    // Ảnh rộng hơn khung -> Cắt trái/phải
                    double newWidth = imgHeight * rectAspect; // Chiều rộng mới của ảnh nguồn
                    sx1 = (int) ((imgWidth - newWidth) / 2);
                    sx2 = (int) (sx1 + newWidth);
                } else if (imgAspect < rectAspect) {
                    // Ảnh cao hơn khung -> Cắt trên/dưới
                    double newHeight = imgWidth / rectAspect; // Chiều cao mới của ảnh nguồn
                    sy1 = (int) ((imgHeight - newHeight) / 2);
                    sy2 = (int) (sy1 + newHeight);
                }

                // Thay thế hàm drawImage cũ
                g.drawImage(preview,
                        x, y, x + width, y + height, // Tọa độ ĐÍCH (trên màn hình)
                        sx1, sy1, sx2, sy2,           // Tọa độ NGUỒN (cắt từ ảnh gốc)
                        null);

                // --- KẾT THÚC LOGIC CẮT ẢNH ---

            } else {
                // Hộp màu tối nếu không có ảnh
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
                g2d.setStroke(new BasicStroke(sm.scaleWidth(4))); // Viền dày
            } else {
                g2d.setColor(Color.WHITE); // Màu mặc định
                g2d.setStroke(new BasicStroke(sm.scaleWidth(2))); // Viền mỏng
            }
            g.drawRect(x, y, width, height);
        }

        // Reset nét vẽ về mặc định
        g2d.setStroke(new BasicStroke(1));

        // 5. Vẽ nút Back
        backButton.draw(g, sm);
    }
}