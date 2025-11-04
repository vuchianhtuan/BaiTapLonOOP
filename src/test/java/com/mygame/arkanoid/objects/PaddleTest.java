package com.mygame.arkanoid.objects;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import com.mygame.arkanoid.systems.ScalingManager;

@DisplayName("Kiểm tra logic của Paddle")
class PaddleTest {

    private Paddle paddle;
    private static int GAME_AREA_WIDTH;
    private static final int PADDLE_WIDTH = 120;
    private static final int PADDLE_SPEED = 15;

    // GIẢI PHÁP CHO SINGLETON
    @BeforeAll
    static void setupGlobal() {
        ScalingManager sm = ScalingManager.getInstance();
        sm.update(1120, 720);
        GAME_AREA_WIDTH = sm.GAME_AREA_WIDTH;
    }

    @BeforeEach
    void setUp() {
        int startX = (GAME_AREA_WIDTH - PADDLE_WIDTH) / 2;
        paddle = new Paddle(startX, 600, PADDLE_WIDTH, 20, "skin_test");
    }

    @Test
    @DisplayName("Di chuyển sang phải (không chạm tường)")
    void testMoveRight() {
        int initialX = paddle.getX();
        paddle.moveRight();
        assertEquals(initialX + PADDLE_SPEED, paddle.getX(), "Paddle di chuyển phải thất bại");
    }

    @Test
    @DisplayName("Di chuyển sang trái (không chạm tường)")
    void testMoveLeft() {
        int initialX = paddle.getX();
        paddle.moveLeft();
        assertEquals(initialX - PADDLE_SPEED, paddle.getX(), "Paddle di chuyển trái thất bại");
    }

    @Test
    @DisplayName("Không thể di chuyển qua mép phải")
    void testMoveRightAtEdge() {
        int max_x = GAME_AREA_WIDTH - PADDLE_WIDTH;
        paddle.setX(max_x - 5);
        paddle.moveRight();
        assertEquals(max_x, paddle.getX(), "Paddle đã di chuyển VƯỢT QUÁ mép phải");
        paddle.moveRight();
        assertEquals(max_x, paddle.getX(), "Paddle vẫn di chuyển dù đã ở mép phải");
    }

    @Test
    @DisplayName("Không thể di chuyển qua mép trái")
    void testMoveLeftAtEdge() {
        paddle.setX(5);
        paddle.moveLeft();
        assertEquals(0, paddle.getX(), "Paddle đã di chuyển VƯỢT QUÁ mép trái");
        paddle.moveLeft();
        assertEquals(0, paddle.getX(), "Paddle vẫn di chuyển dù đã ở mép trái");
    }
}