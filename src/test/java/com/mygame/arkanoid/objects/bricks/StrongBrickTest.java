package com.mygame.arkanoid.objects.bricks;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Kiểm tra logic của Gạch Cứng (StrongBrick)")
class StrongBrickTest {

    private StrongBrick brick;

    @BeforeEach
    void setUp() {
        // Tạo gạch 3 HP mới cho mỗi lần test
        brick = new StrongBrick(100, 100, 45, 20, 3);
    }

    @Test
    @DisplayName("Gạch mất máu và bị phá hủy chính xác")
    void testTakeHitAndDestroy() {
        // Kịch bản 1: Mới tạo (3 HP)
        assertEquals(3, brick.getHitPoints(), "Máu ban đầu phải là 3");
        assertFalse(brick.isDestroyed(), "Gạch không được vỡ khi mới tạo");

        // Kịch bản 2: Bắn 1 lần (còn 2 HP)
        brick.takeHit();
        assertEquals(2, brick.getHitPoints(), "Máu phải là 2 sau khi bị bắn 1 lần");
        assertFalse(brick.isDestroyed(), "Gạch không được vỡ khi còn 2 máu");

        // Kịch bản 3: Bắn 2 lần (còn 1 HP)
        brick.takeHit();
        assertEquals(1, brick.getHitPoints(), "Máu phải là 1 sau khi bị bắn 2 lần");
        assertFalse(brick.isDestroyed(), "Gạch không được vỡ khi còn 1 máu");

        // Kịch bản 4: Bắn 3 lần (VỠ - còn 0 HP)
        brick.takeHit();
        assertEquals(0, brick.getHitPoints(), "Máu phải là 0 sau khi bị bắn 3 lần");
        assertTrue(brick.isDestroyed(), "Gạch PHẢI vỡ khi 0 máu");

        // Kịch bản 5: Bắn thêm khi đã vỡ
        brick.takeHit();
        assertEquals(0, brick.getHitPoints(), "Máu không được âm khi bị bắn lúc đã vỡ");
        assertTrue(brick.isDestroyed(), "Gạch vẫn phải ở trạng thái vỡ");
    }

    @Test
    @DisplayName("Tên ảnh (imageName) cập nhật đúng theo số máu")
    void testImageNameUpdatesOnHit() {
        // Trạng thái 1: 3 HP (ban đầu)
        assertEquals("strongBrick", brick.imageName, "Ảnh ban đầu (3 HP) phải là 'strongBrick'");

        // Trạng thái 2: 2 HP
        brick.takeHit();
        brick.update();  // Gọi hàm update để cập nhật tên ảnh
        assertEquals("strongBrick1", brick.imageName, "Ảnh (2 HP) phải là 'strongBrick1'");

        // Trạng thái 3: 1 HP
        brick.takeHit();
        brick.update();
        assertEquals("strongBrick2", brick.imageName, "Ảnh (1 HP) phải là 'strongBrick2'");

        // Trạng thái 4: 0 HP (đã vỡ)
        brick.takeHit();
        brick.update();
        // Tên ảnh không đổi khi 0 HP, vì gạch sẽ bị xóa (đây là hành vi đúng)
        assertEquals("strongBrick2", brick.imageName, "Ảnh (0 HP) vẫn giữ nguyên là 'strongBrick2'");
    }
}