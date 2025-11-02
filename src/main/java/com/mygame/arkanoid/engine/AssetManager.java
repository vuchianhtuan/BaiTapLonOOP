package com.mygame.arkanoid.engine;

import com.mygame.arkanoid.config.AssetDefinition;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.io.InputStream;

public class AssetManager {
    private static final AssetManager instance = new AssetManager();
    private final Map<String, BufferedImage> images = new HashMap<>();
    private static final String IMAGE_PATH_PREFIX = "/images/";

    private String currentTheme = "";

    private AssetManager() {}

    public static AssetManager getInstance() {
        return instance;
    }

    /**
     * Hàm nội bộ để thực hiện tải ảnh, trả về null nếu thất bại.
     */
    private BufferedImage internalLoadImage(String path) {
        try (InputStream is = getClass().getResourceAsStream(path)) {
            if (is != null) {
                return ImageIO.read(is);
            }
        } catch (IOException e) {
            System.err.println("Lỗi IO khi tải ảnh: " + path);
            e.printStackTrace();
        }
        return null; // Không tìm thấy hoặc lỗi
    }

    /**
     * Tải một ảnh toàn cục (global asset).
     * Chỉ tải nếu chưa có.
     * @param name Tên key (ví dụ: "heart")
     * @param path Đường dẫn đầy đủ (ví dụ: "/images/heart.png")
     */
    public void loadImage(String name, String path) {
        if (images.containsKey(name)) {
            return; // Đã tải rồi
        }

        BufferedImage image = internalLoadImage(path);

        if (image != null) {
            images.put(name, image);
        } else {
            System.err.println("Lỗi: Không thể tải ảnh: " + path);
        }
    }

    /**
     * Tải một ảnh theo chủ đề (themed asset) với cơ chế fallback.
     * Sẽ thử tải "themePrefix + baseFileName", nếu thất bại, sẽ tải "baseFileName".
     * @param name Tên key (ví dụ: "normalBrick")
     * @param themePrefix Tiền tố theme (ví dụ: "ice_")
     * @param baseFileName Tên file gốc (ví dụ: "normalBrick.png")
     */
    public void loadThemedImage(String name, String themePrefix, String baseFileName) {
        // Không cần kiểm tra containsKey, vì chúng ta có thể đang tải
        // một theme mới đè lên theme cũ (dùng chung key "normalBrick")

        String themedPath = IMAGE_PATH_PREFIX + themePrefix + baseFileName;
        String defaultPath = IMAGE_PATH_PREFIX + baseFileName;

        // 1. Thử tải ảnh chủ đề
        BufferedImage image = internalLoadImage(themedPath);

        if (image != null) {
            images.put(name, image);
        } else {
            // 2. Thử tải ảnh mặc định (fallback)
            BufferedImage defaultImage = internalLoadImage(defaultPath);
            if (defaultImage != null) {
                images.put(name, defaultImage);
            } else {
                // 3. Lỗi: Không tìm thấy cả hai
                System.err.println("Lỗi nghiêm trọng: Không thể tải " + themedPath + " hoặc " + defaultPath);
            }
        }
    }

    /**
     * Tải một chuỗi ảnh animation.
     * @param namePrefix Tiền tố key (ví dụ: "explosion_render")
     * @param fileNamePrefix Tiền tố file (ví dụ: "explosion_render")
     * @param frameCount Số lượng frame (ví dụ: 8)
     * @param fileExtension Đuôi file (ví dụ: ".png")
     */
    public void loadAnimation(String namePrefix, String fileNamePrefix, int frameCount, String fileExtension) {
        for (int i = 1; i <= frameCount; i++) {
            String name = namePrefix + i;
            String path = IMAGE_PATH_PREFIX + fileNamePrefix + i + fileExtension;
            loadImage(name, path); // Dùng lại hàm loadImage toàn cục
        }
    }

    public void loadGlobalAssets() {
        // 1. Tải tất cả tài sản từ bản kê khai
        for (AssetDefinition asset : AssetDefinition.values()) {
            if (!asset.isThemed()) {
                // Tải tất cả asset global (UI, Powerups, Skins, Misc)
                loadImage(asset.getKey(), asset.getFullPath());
            }
        }

        // 2. Tải các animation đặc biệt
        loadAnimation("explosion_render", "explosion_render", 8, ".png");
    }

    public void loadTheme(String prefix) {
        if (prefix == null || prefix.equals(currentTheme)) {
            return; // Không làm gì nếu theme đã được tải
        }
        currentTheme = prefix;

        // Tải tất cả asset được đánh dấu là "isThemed"
        for (AssetDefinition asset : AssetDefinition.values()) {
            if (asset.isThemed()) {
                loadThemedImage(asset.getKey(), prefix, asset.getFileName());
            }
        }
    }
    public BufferedImage getBackgroundImage(String bgName) {
        if (bgName == null || bgName.isEmpty()) {
            return this.getImage("defaultBackground");
        }
        String assetKey = "bg_" + bgName;
        String path = IMAGE_PATH_PREFIX + bgName;
        this.loadImage(assetKey, path);
        return this.getImage(assetKey);
    }

    public BufferedImage getImage(String name) {
        return images.get(name);
    }
}