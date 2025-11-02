package com.mygame.arkanoid.objects;

import com.mygame.arkanoid.objects.bricks.Brick;
import java.awt.Point;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// QUẢN LÝ BOSS VỚI NHIỀU KHỐI GẠCH
public class Boss {

    // VỊ TRÍ HIỆN TẠI CỦA BOSS, VẬN TỐC THEO TRỤC X, DANH SÁCH GẠCH VÀ BẢN ĐỒ VỊ TRÍ TƯƠNG ĐỐI
    private float x, y;
    private float dx;
    private final List<Brick> bricks;
    private final int screenWidth;
    private final Map<Brick, Point> relativePositions;

    public Boss(List<Brick> bossBricks, float startX, float startY, int originalMinX, int screenWidth) {
        this.bricks = new ArrayList<>(bossBricks);
        this.x = startX;
        this.y = startY;
        this.dx = 2.0f;
        this.screenWidth = screenWidth;
        this.relativePositions = new HashMap<>();

        // Tính toán vị trí tương đối dựa trên vị trí GỐC (originalMinX)
        for (Brick brick : bricks) {
            int relX = brick.getX() - originalMinX;
            int relY = brick.getY() - (int) startY;
            relativePositions.put(brick, new Point(relX, relY));
        }
    }

    /**
     * CẬP NHẬT VỊ TRÍ CỦA BOSS VÀ CÁC KHỐI GẠCH BÊN TRONG NÓ.
     */
    public void update() {
        x += dx;

        // ... (Logic tính toán va chạm tường không đổi, nó đã đúng) ...
        int minRelX = Integer.MAX_VALUE;
        int maxRelX = Integer.MIN_VALUE;
        // Tìm ra offset tương đối trái nhất và phải nhất của khối gạch
        for (Point relPos : relativePositions.values()) {
            minRelX = Math.min(minRelX, relPos.x);
        }
        for (Map.Entry<Brick, Point> entry : relativePositions.entrySet()) {
            maxRelX = Math.max(maxRelX, entry.getValue().x + entry.getKey().getWidth());
        }
        int bossWidth = maxRelX - minRelX;

        float leftEdge = this.x + minRelX;
        float rightEdge = this.x + minRelX + bossWidth;

        if (leftEdge <= 0) {
            dx = -dx;
            this.x = -minRelX;
        } else if (rightEdge >= screenWidth) {
            dx = -dx;
            this.x = screenWidth - bossWidth - minRelX;
        }

        for (Brick brick : bricks) {
            Point relPos = relativePositions.get(brick);
            if (relPos != null) {
                brick.setX((int) this.x + relPos.x);
                brick.setY((int) this.y + relPos.y);
            }
        }
    }

    /**
     * XÓA CÁC KHỐI GẠCH ĐÃ BỊ PHÁ HỦY KHỎI DANH SÁCH VÀ BẢN ĐỒ VỊ TRÍ TƯƠNG ĐỐI.
     */
    public void removeDestroyedBricks() {
        bricks.removeIf(brick -> {
            if (brick.isDestroyed()) {
                relativePositions.remove(brick); // Xóa gạch khỏi map
                return true;
            }
            return false;
        });
    }

    /**
     * KIỂM TRA NẾU BOSS ĐÃ BỊ ĐÁNH BẠI (KHÔNG CÒN GẠCH NÀO).
     * @return true NẾU BOSS ĐÃ BỊ ĐÁNH BẠI, NGƯỢC LẠI LÀ false.
     */
    public boolean isDefeated() {
        return bricks.isEmpty();
    }

    public List<Brick> getBricks() {
        return bricks;
    }
}
