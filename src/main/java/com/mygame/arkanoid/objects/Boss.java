package com.mygame.arkanoid.objects;

import com.mygame.arkanoid.objects.bricks.Brick;
import java.awt.Point;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Lớp Boss đại diện cho một con trùm trong game, bao gồm nhiều khối gạch di chuyển cùng nhau.
 */
public class Boss {

    // Vị trí hiện tại của Boss, vận tốc di chuyển và danh sách các khối gạch bên trong nó.
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
     * Cập nhật vị trí của Boss và các khối gạch bên trong nó.
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
     * Xóa các khối gạch đã bị phá hủy khỏi danh sách.
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
     * Kiểm tra xem Boss đã bị đánh bại chưa.
     */
    public boolean isDefeated() {
        return bricks.isEmpty();
    }

    public List<Brick> getBricks() {
        return bricks;
    }
}
