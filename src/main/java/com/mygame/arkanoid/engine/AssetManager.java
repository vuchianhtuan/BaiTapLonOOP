package com.mygame.arkanoid.engine;

import com.mygame.arkanoid.util.config.AssetDefinition;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.io.InputStream;

/**
 * Quản lý tài sản (assets) trong game, bao gồm tải ảnh toàn cục và theo chủ đề.
 */
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
    private BufferedImage internalLoadImage(String path) throws LoadException {
        try (InputStream is = getClass().getResourceAsStream(path)) {
            if (is != null) {
                return ImageIO.read(is);
            } else {
                throw new LoadException("Không tìm thấy file ảnh: " + path);
            }
        } catch (IOException e) {
            throw new LoadException("Lỗi khi tải ảnh: " + path, e);
        }
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

        try {
            BufferedImage image = internalLoadImage(path);
            images.put(name, image);
        } catch (LoadException e) {
            System.err.println("Không thể tải ảnh: " + path);
            e.printStackTrace();
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
        // Các đường dẫn để thử tải
        String themedPath = IMAGE_PATH_PREFIX + themePrefix + baseFileName;
        String defaultPath = IMAGE_PATH_PREFIX + baseFileName;

        // Nếu đã tải rồi thì không làm gì, nếu không thì thử tải
        try {
            BufferedImage image = internalLoadImage(themedPath);
            images.put(name, image);
        } catch (LoadException e1) {
            try {
                BufferedImage defaultImage = internalLoadImage(defaultPath);
                images.put(name, defaultImage);
            } catch (LoadException e2) {
                System.err.println("Không thể tải được ảnh theo theme hoặc mặc định: " + name);
                e2.printStackTrace();
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
            loadImage(name, path); // Tải từng frame
        }
    }

    /**
     * Tải tất cả tài sản toàn cục (global assets) không theo theme.
     */
    public void loadGlobalAssets() {
        // Tải các asset không theo theme
        for (AssetDefinition asset : AssetDefinition.values()) {
            if (!asset.isThemed()) {
                // Tải tất cả asset global (UI, Powerups, Skins, Misc)
                loadImage(asset.getKey(), asset.getFullPath());
            }
        }

        // Tải các animation đặc biệt
        loadAnimation("explosion_render", "explosion_render", 8, ".png");
    }

    /**
     * Tải tất cả tài nguyên theo theme.
     * @param prefix Tiền tố theme (ví dụ: "ice_")
     */
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

    /**
     * Lấy ảnh nền theo tên, với ảnh mặc định nếu tên rỗng.
     * @param bgName Tên ảnh nền (không có đường dẫn)
     * @return BufferedImage
     */
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