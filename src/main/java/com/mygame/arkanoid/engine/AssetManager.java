package com.mygame.arkanoid.engine;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.io.InputStream;

public class AssetManager {
    private static final AssetManager instance = new AssetManager();
    private final Map<String, BufferedImage> images = new HashMap<>();

    private AssetManager() {}

    public static AssetManager getInstance() {
        return instance;
    }

    public void loadImage(String name, String path) {
        try {
            // 1. Thử tải ảnh chủ đề (ví dụ: /images/ice_ball.png)
            InputStream is = getClass().getResourceAsStream(path);

            if (is != null) {
                // 1a. Tìm thấy! Tải và lưu trữ nó.
                BufferedImage image = ImageIO.read(is);
                images.put(name, image);
                is.close();
            } else {
                // 1b. Không tìm thấy ảnh chủ đề. Bắt đầu thử "fallback".

                // Tìm vị trí của tiền tố (prefix), ví dụ "ice_"
                int lastSlash = path.lastIndexOf('/');
                int prefixStart = path.indexOf('_', lastSlash);

                // Kiểm tra xem có phải là một đường dẫn có tiền tố hay không
                if (prefixStart != -1) {
                    // Tạo đường dẫn mặc định
                    // Ví dụ: /images/ice_ball.png -> /images/ball.png
                    String baseName = path.substring(prefixStart + 1);
                    String defaultPath = "/images/" + baseName;

                    // 2. Thử tải ảnh mặc định
                    InputStream defaultIs = getClass().getResourceAsStream(defaultPath);

                    if (defaultIs != null) {
                        // 2a. Tìm thấy ảnh mặc định! Tải và lưu nó.
                        BufferedImage image = ImageIO.read(defaultIs);
                        images.put(name, image);
                        defaultIs.close();
                    } else {
                        // 2b. Lỗi: Không tìm thấy cả ảnh chủ đề và ảnh mặc định
                        System.err.println("Lỗi nghiêm trọng: Không thể tải " + path + " hoặc " + defaultPath);
                    }
                } else {
                    // Lỗi: Ngay cả ảnh mặc định (không có tiền tố) cũng không tìm thấy
                    System.err.println("Lỗi: Không thể tải ảnh: " + path);
                }
            }
        } catch (IOException e) {
            System.err.println("Lỗi IO khi tải ảnh: " + path);
            e.printStackTrace();
        }
    }

    public BufferedImage getImage(String name) {
        return images.get(name);
    }
}
