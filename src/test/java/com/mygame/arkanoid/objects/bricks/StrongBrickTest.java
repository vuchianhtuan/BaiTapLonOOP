package com.mygame.arkanoid.objects.bricks;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Kiểm tra logic của Gạch Cứng (StrongBrick)")
class StrongBrickTest {

    private StrongBrick brick;

    // Hàm này chạy trước mỗi @Test
    @BeforeEach
    void setUp() {
        // Tạo một viên gạch mới với 3 HP cho mỗi lần test
        brick = new StrongBrick(100, 100, 45, 20, 3);
    }

    @Test
    @DisplayName("Gạch mất máu và bị phá hủy chính xác")
    void testTakeHitAndDestroy() {
        // --- Kịch bản 1: Mới tạo ---
        assertEquals(3, brick.getHitPoints(), "Máu ban đầu phải là 3");
        assertFalse(brick.isDestroyed(), "Gạch không được vỡ khi mới tạo");

        // --- Kịch bản 2: Bị bắn 1 lần ---
        brick.takeHit(); // Máu còn 2
        assertEquals(2, brick.getHitPoints(), "Máu phải là 2 sau khi bị bắn 1 lần");
        assertFalse(brick.isDestroyed(), "Gạch không được vỡ khi còn 2 máu");

        // --- Kịch bản 3: Bị bắn 2 lần ---
        brick.takeHit(); // Máu còn 1
        assertEquals(1, brick.getHitPoints(), "Máu phải là 1 sau khi bị bắn 2 lần");
        assertFalse(brick.isDestroyed(), "Gạch không được vỡ khi còn 1 máu");

        // --- Kịch bản 4: Bị bắn 3 lần (VỠ) ---
        brick.takeHit(); // Máu còn 0
        assertEquals(0, brick.getHitPoints(), "Máu phải là 0 sau khi bị bắn 3 lần");
        assertTrue(brick.isDestroyed(), "Gạch PHẢI vỡ khi 0 máu");

        // --- Kịch bản 5: Bị bắn thêm khi đã vỡ ---
        brick.takeHit(); // Máu vẫn là 0 (không bị âm)
        assertEquals(0, brick.getHitPoints(), "Máu không được âm khi bị bắn lúc đã vỡ");
        assertTrue(brick.isDestroyed(), "Gạch vẫn phải ở trạng thái vỡ");
    }

    @Test
    @DisplayName("Tên ảnh (imageName) cập nhật đúng theo số máu")
    void testImageNameUpdatesOnHit() {
        // --- Trạng thái 1: 3 HP (ban đầu) ---
        // (Constructor đã gọi getInitialImageName("strongBrick"))
        assertEquals("strongBrick", brick.imageName, "Ảnh ban đầu (3 HP) phải là 'strongBrick'");

        // --- Trạng thái 2: 2 HP ---
        brick.takeHit(); // Máu còn 2
        brick.update();  // Gọi hàm update để cập nhật tên ảnh
        assertEquals("strongBrick1", brick.imageName, "Ảnh (2 HP) phải là 'strongBrick1'");

        // --- Trạng thái 3: 1 HP ---
        brick.takeHit(); // Máu còn 1
        brick.update();  // Gọi hàm update
        assertEquals("strongBrick2", brick.imageName, "Ảnh (1 HP) phải là 'strongBrick2'");

        // --- Trạng thái 4: 0 HP ---
        brick.takeHit(); // Máu còn 0
        brick.update();  // Gọi hàm update
        // Tên ảnh vẫn là "strongBrick2" vì hàm update không có logic cho 0 HP
        // (Điều này là BÌNH THƯỜNG, vì gạch vỡ sẽ bị xóa, không ai thấy ảnh nữa)
        assertEquals("strongBrick2", brick.imageName, "Ảnh (0 HP) vẫn giữ nguyên là 'strongBrick2'");
    }
}