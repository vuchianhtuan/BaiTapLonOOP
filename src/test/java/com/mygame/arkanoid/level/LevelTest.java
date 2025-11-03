package com.mygame.arkanoid.level;

import com.mygame.arkanoid.objects.bricks.Brick;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import com.mygame.arkanoid.engine.AssetManager;
import org.junit.jupiter.api.BeforeAll;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Lớp kiểm thử (test class) JUnit 5 dành cho {@link Level}.
 * <p>
 * Mục đích là xác minh rằng lớp {@code Level} đọc và phân tích (parse)
 * tệp định nghĩa màn chơi ({@code .txt}) một cách chính xác,
 * bao gồm cả hai phần: phần Header (cấu hình) và phần Layout (bố cục gạch).
 */
@DisplayName("Kiểm tra Trình Tải Level (Level.java)")
class LevelTest {

    private Level level;
    private final String REAL_LEVEL_PATH = "/levels/Level1.txt";

    /**
     * Thiết lập (setup) toàn cục, chạy một lần duy nhất trước tất cả các test.
     * <p>
     * Tải (load) các tài sản (assets) chung, ví dụ như hoạt ảnh (animation) nổ.
     * Cần thiết vì constructor của {@link Level} (được gọi trong {@code setUp})
     * sẽ tạo các đối tượng gạch (như {@code ExplosiveBrick}) mà chúng
     * phụ thuộc vào {@link AssetManager} đã được khởi tạo.
     */
    @BeforeAll
    static void setupGlobal() {
        // Phải tải assets trước, vì hàm new Level()
        // sẽ gián tiếp tạo ExplosiveBrick
        AssetManager.getInstance().loadGlobalAssets();
    }

    /**
     * Thiết lập (setup) chạy trước *mỗi* phương thức {@code @Test}.
     * <p>
     * Tải lại (reload) đối tượng {@code Level} từ tệp ({@code REAL_LEVEL_PATH})
     * để đảm bảo mỗi kiểm thử (test) hoạt động trên một đối tượng 'sạch',
     * không bị ảnh hưởng bởi các test khác.
     */
    @BeforeEach
    void setUp() {
        // --- HÀNH ĐỘNG (Act) ---
        level = new Level(REAL_LEVEL_PATH);
    }

    @Test
    @DisplayName("Đọc file Level1.txt và phân tích Header chính xác")
    void testRealLevelHeaderParsing() {

        assertNotNull(level, "Không thể tải file Level. File có thể bị sai đường dẫn.");
        assertFalse(level.isBossLevel(), "Loại level phải là 'normal' (isBossLevel() nên là false)");


        assertEquals("Forest.wav", level.getThemeMusic(), "Tên file nhạc bị sai");
        assertEquals("forest_", level.getThemeAssetPrefix(), "Tiền tố asset bị sai");
        assertEquals("background_forest.png", level.getThemeBackground(), "Ảnh nền bị sai");
        assertEquals(0.1, level.getLaserShooterSpawnRate(), "Tỷ lệ spawn laser bị sai");
    }

    @Test
    @DisplayName("Đọc file Level1.txt và đếm số lượng gạch chính xác")
    void testRealLevelBrickParsing() {

        List<Brick> bossBricks = level.getBossBricks();
        assertEquals(0, bossBricks.size(), "Level 1 không được có gạch boss");

        List<Brick> normalBricks = level.getBricks();
        assertEquals(101, normalBricks.size(), "Tổng số gạch (1, 2, E, L, M) đếm được bị sai");
    }
}