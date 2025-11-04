package com.mygame.arkanoid.level;

import com.mygame.arkanoid.objects.bricks.Brick;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import com.mygame.arkanoid.engine.AssetManager;
import org.junit.jupiter.api.BeforeAll;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Kiểm tra Trình Tải Level (Level.java)")
class LevelTest {

    private Level level;
    private final String REAL_LEVEL_PATH = "/levels/Level1.txt";

    @BeforeAll
    static void setupGlobal() {
        // Bắt buộc: Tải assets trước, vì Level() sẽ tạo gạch (vd: ExplosiveBrick)
        // yêu cầu assets phải được tải.
        AssetManager.getInstance().loadGlobalAssets();
    }

    @BeforeEach
    void setUp() {
        // Tải level trước mỗi bài test
        level = new Level(REAL_LEVEL_PATH);
    }

    @Test
    @DisplayName("Đọc file Level1.txt và phân tích Header chính xác")
    void testRealLevelHeaderParsing() {
        // --- XÁC MINH (Assert) ---
        assertNotNull(level, "Không thể tải file Level. File có thể bị sai đường dẫn.");

        // CHỈ CẦN KIỂM TRA BẰNG HÀM isBossLevel()
        assertFalse(level.isBossLevel(), "Loại level phải là 'normal' (isBossLevel() nên là false)");

        assertEquals("Forest.wav", level.getThemeMusic(), "Tên file nhạc bị sai");
        assertEquals("forest_", level.getThemeAssetPrefix(), "Tiền tố asset bị sai");
        assertEquals("background_forest.png", level.getThemeBackground(), "Ảnh nền bị sai");
        assertEquals(0.1, level.getLaserShooterSpawnRate(), "Tỷ lệ spawn laser bị sai");
    }

    @Test
    @DisplayName("Đọc file Level1.txt và đếm số lượng gạch chính xác")
    void testRealLevelBrickParsing() {
        // --- XÁC MINH (Assert) ---

        // Level 1 không được có gạch boss
        List<Brick> bossBricks = level.getBossBricks();
        assertEquals(0, bossBricks.size(), "Level 1 không được có gạch boss");

        // Đếm tổng số gạch thường
        List<Brick> normalBricks = level.getBricks();
        assertEquals(101, normalBricks.size(), "Tổng số gạch (1, 2, E, L, M) đếm được bị sai");
    }
}