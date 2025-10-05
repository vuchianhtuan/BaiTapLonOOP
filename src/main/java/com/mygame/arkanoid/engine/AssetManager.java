package com.mygame.arkanoid.engine;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class AssetManager {
    /*public void loadImage(String path) {}
    public Object getImage(String name) { return null; }
*/
    private static final AssetManager instance = new AssetManager();
    private final Map<String, BufferedImage> images = new HashMap<>();

    // Constructor để private để không ai khác tạo được instance mới
    private AssetManager() {}

    // Phương thức công khai để mọi nơi khác có thể truy cập vào instance duy nhất
    public static AssetManager getInstance() {
        return instance;
    }

    // Các phương thức loadImage và getImage giữ nguyên
    public void loadImage(String name, String path) {
        try {
            BufferedImage image = ImageIO.read(getClass().getResourceAsStream(path));
            if (image != null) {
                images.put(name, image);
            }
        } catch (IOException e) {
            System.err.println("Lỗi khi tải ảnh: " + path);
            e.printStackTrace();
        }
    }
    public BufferedImage getImage(String name) {
        return images.get(name);
    }
}
