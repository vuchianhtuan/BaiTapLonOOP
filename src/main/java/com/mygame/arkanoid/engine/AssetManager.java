package com.mygame.arkanoid.engine;

import com.mygame.arkanoid.config.AssetDefinition;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.io.InputStream;

/**
 * Trình quản lý tài sản (Singleton) của game.
 * Chịu trách nhiệm tải, lưu trữ (cache) và cung cấp các tài sản hình ảnh ({@link BufferedImage}).
 * Hỗ trợ cả tài sản toàn cục (global) và tài sản theo chủ đề (themed) với cơ chế fallback.
 */
public class AssetManager {
    private static final AssetManager instance = new AssetManager();
    /** Cache lưu trữ các ảnh đã được tải, ánh xạ từ key (String) sang ảnh (BufferedImage). */
    private final Map<String, BufferedImage> images = new HashMap<>();
    private static final String IMAGE_PATH_PREFIX = "/images/";

    /** Lưu trữ theme hiện tại để tránh tải lại không cần thiết. */
    private String currentTheme = "";

    // Constructor private cho Singleton
    private AssetManager() {}

    public static AssetManager getInstance() {
        return instance;
    }

    /**
     * Hàm tải ảnh cốt lõi. Tải tài nguyên từ classpath (bên trong JAR).
     * @param path Đường dẫn đầy đủ, bắt đầu từ root (ví dụ: "/images/heart.png").
     * @return BufferedImage đã tải.
     * @throws LoadException Nếu tài nguyên không tìm thấy hoặc có lỗi I/O.
     */
    private BufferedImage internalLoadImage(String path) throws LoadException {
        // Sử dụng try-with-resources để đảm bảo InputStream luôn được đóng
        try (InputStream is = getClass().getResourceAsStream(path)) {
            if (is != null) {
                return ImageIO.read(is);
            } else {
                // Ném lỗi cụ thể nếu không tìm thấy tài nguyên
                throw new LoadException("Không tìm thấy file ảnh: " + path);
            }
        } catch (IOException e) {
            // Ném lỗi cụ thể nếu ImageIO thất bại
            throw new LoadException("Lỗi khi tải ảnh: " + path, e);
        }
    }

    /**
     * Tải một ảnh toàn cục (global asset) và lưu vào cache.
     * Chỉ tải nếu key (tên) chưa tồn tại trong cache.
     * @param name Key để truy cập ảnh (ví dụ: "heart").
     * @param path Đường dẫn đầy đủ (ví dụ: "/images/heart.png").
     */
    public void loadImage(String name, String path) {
        if (images.containsKey(name)) {
            return; // Đã tải rồi, bỏ qua
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
     * Tải một ảnh theo chủ đề (themed asset) với cơ chế **fallback**.
     * <p>
     * Hàm sẽ thử tải phiên bản theo chủ đề trước (ví dụ: "/images/ice_normalBrick.png").
     * Nếu file đó không tồn tại (ném ra {@link LoadException}),
     * hàm sẽ tự động thử tải phiên bản mặc định (ví dụ: "/images/normalBrick.png").
     *
     * @param name Key để truy cập ảnh (ví dụ: "normalBrick").
     * @param themePrefix Tiền tố của chủ đề (ví dụ: "ice_").
     * @param baseFileName Tên tệp gốc (ví dụ: "normalBrick.png").
     */
    public void loadThemedImage(String name, String themePrefix, String baseFileName) {
        // Các đường dẫn để thử tải
        String themedPath = IMAGE_PATH_PREFIX + themePrefix + baseFileName;
        String defaultPath = IMAGE_PATH_PREFIX + baseFileName;

        // Thử tải ảnh theo theme
        try {
            BufferedImage image = internalLoadImage(themedPath);
            images.put(name, image);
        } catch (LoadException e1) {
            // Nếu thất bại (e1), thử tải ảnh mặc định
            try {
                BufferedImage defaultImage = internalLoadImage(defaultPath);
                images.put(name, defaultImage);
            } catch (LoadException e2) {
                // Nếu cả hai đều thất bại, báo lỗi nghiêm trọng
                System.err.println("Không thể tải được ảnh theo theme hoặc mặc định: " + name);
                e2.printStackTrace();
            }
        }
    }

    /**
     * Hàm tiện ích để tải một chuỗi các khung hình (frames) cho animation.
     * Tải các file theo định dạng: "fileNamePrefix" + "số" + "fileExtension".
     * Key sẽ là: "namePrefix" + "số".
     *
     * @param namePrefix Tiền tố key (ví dụ: "explosion_render").
     * @param fileNamePrefix Tiền tố file (ví dụ: "explosion_render").
     * @param frameCount Tổng số khung hình cần tải (ví dụ: 8).
     * @param fileExtension Đuôi file (ví dụ: ".png").
     */
    public void loadAnimation(String namePrefix, String fileNamePrefix, int frameCount, String fileExtension) {
        for (int i = 1; i <= frameCount; i++) {
            String name = namePrefix + i;
            String path = IMAGE_PATH_PREFIX + fileNamePrefix + i + fileExtension;
            loadImage(name, path); // Tải từng frame như một asset toàn cục
        }
    }

    /**
     * Tải tất cả tài sản toàn cục (global assets).
     * Duyệt qua {@link AssetDefinition} và tải mọi tài sản *không* được đánh dấu
     * là {@code isThemed = true}.
     * Thường được gọi một lần khi game khởi động.
     */
    public void loadGlobalAssets() {
        // Tải các asset không theo theme
        for (AssetDefinition asset : AssetDefinition.values()) {
            if (!asset.isThemed()) {
                // Tải tất cả asset global (UI, Powerups, Skins, Misc)
                loadImage(asset.getKey(), asset.getFullPath());
            }
        }

        // Tải các animation đặc biệt (ví dụ: hiệu ứng nổ)
        loadAnimation("explosion_render", "explosion_render", 8, ".png");
    }

    /**
     * Tải tất cả tài sản cho một chủ đề (theme) cụ thể.
     * Sẽ bỏ qua nếu theme được yêu cầu giống hệt theme hiện tại.
     * <p>
     * Duyệt qua {@link AssetDefinition} và tải mọi tài sản được đánh dấu
     * là {@code isThemed = true}, sử dụng cơ chế fallback của
     * {@link #loadThemedImage(String, String, String)}.
     *
     * @param prefix Tiền tố theme (ví dụ: "ice_").
     */
    public void loadTheme(String prefix) {
        if (prefix == null || prefix.equals(currentTheme)) {
            return; // Không làm gì nếu theme đã được tải hoặc prefix rỗng
        }
        currentTheme = prefix;

        // Tải tất cả asset được đánh dấu là "isThemed"
        for (AssetDefinition asset : AssetDefinition.values()) {
            if (asset.isThemed()) {
                // Sử dụng hàm loadThemedImage để có fallback
                loadThemedImage(asset.getKey(), prefix, asset.getFileName());
            }
        }
    }

    /**
     * Lấy ảnh nền (background) theo tên, với ảnh mặc định nếu tên rỗng.
     * <p>
     * Phương thức này thực hiện **tải theo yêu cầu (lazy-loading)**.
     * Nếu ảnh nền (với key đã tạo) chưa có trong cache,
     * nó sẽ được tải vào thời điểm này.
     *
     * @param bgName Tên file ảnh nền (ví dụ: "background_forest.png").
     * @return BufferedImage của ảnh nền.
     */
    public BufferedImage getBackgroundImage(String bgName) {
        if (bgName == null || bgName.isEmpty()) {
            // Fallback về ảnh nền mặc định nếu không có tên
            return this.getImage("defaultBackground");
        }

        // Tạo key duy nhất cho ảnh nền
        String assetKey = "bg_" + bgName;
        String path = IMAGE_PATH_PREFIX + bgName;

        // Tải nếu chưa có (loadImage có kiểm tra cache nội bộ)
        this.loadImage(assetKey, path);

        return this.getImage(assetKey);
    }

    public BufferedImage getImage(String name) {
        return images.get(name);
    }

    /**
     * Lớp Exception nội bộ cho các lỗi liên quan đến tải tài sản.
     */
    private static class LoadException extends IOException {
        public LoadException(String message) {
            super(message);
        }

        public LoadException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}