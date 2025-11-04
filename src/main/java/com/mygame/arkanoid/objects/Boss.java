package com.mygame.arkanoid.objects;

import com.mygame.arkanoid.objects.bricks.Brick;
import java.awt.Point;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Lớp Boss đại diện cho một con trùm (Boss) trong game.
 * <p>
 * Đây là một đối tượng "composite" (tổ hợp), hoạt động như một container
 * di chuyển một nhóm các đối tượng {@link Brick} (các gạch của trùm)
 * như một thực thể (entity) thống nhất.
 * <p>
 * <b>Nguyên lý hoạt động:</b>
 * <ol>
 * <li>Khi khởi tạo, nó lưu vị trí tương đối (offset) của mỗi gạch
 * so với một điểm neo (anchor) chung.</li>
 * <li>Trong {@code update()}, nó chỉ di chuyển điểm neo và kiểm tra va chạm tường.</li>
 * <li>Sau đó, nó cập nhật (đồng bộ) vị trí tuyệt đối của TẤT CẢ
 * các gạch con dựa trên vị trí mới của điểm neo.</li>
 * </ol>
 */
public class Boss {

    /** Vị trí "neo" (anchor) của toàn bộ Boss (thường là góc trên bên trái). */
    private float x, y;
    /** Vận tốc di chuyển ngang (horizontal speed) của cả nhóm. */
    private float dx;
    /** Danh sách các gạch con (brick components) còn sống của Boss. */
    private final List<Brick> bricks;
    /** Chiều rộng (logic) của khu vực chơi game, dùng để kiểm tra va chạm tường. */
    private final int screenWidth;
    /**
     * "Hình dạng" (Shape) của Boss.
     * Ánh xạ (Map) mỗi gạch con (Brick) tới vị trí tương đối (Point/offset)
     * của nó so với điểm neo (x, y) của Boss.
     */
    private final Map<Brick, Point> relativePositions;

    /**
     * Khởi tạo một đối tượng Boss mới.
     * <p>
     * Tính toán và "đóng băng" (freeze) hình dạng của Boss bằng cách
     * lưu trữ vị trí tương đối (offset) của mỗi gạch con
     * so với điểm neo (anchor) ban đầu.
     *
     * @param bossBricks Danh sách gạch ban đầu (đã được tạo bởi Level/Factory).
     * @param startX Vị trí X (logic) ban đầu của điểm neo (anchor).
     * @param startY Vị trí Y (logic) ban đầu của điểm neo (anchor).
     * @param originalMinX Tọa độ X (logic) nhỏ nhất của gạch ngoài cùng
     * bên trái TỪ TỆP LEVEL. Dùng để tính toán offset chính xác.
     * @param screenWidth Chiều rộng (logic) của khu vực chơi game.
     */
    public Boss(List<Brick> bossBricks, float startX, float startY, int originalMinX, int screenWidth) {
        this.bricks = new ArrayList<>(bossBricks);
        this.x = startX;
        this.y = startY;
        this.dx = 2.0f; // Tốc độ mặc định
        this.screenWidth = screenWidth;
        this.relativePositions = new HashMap<>();

        // Tính toán và "đóng băng" vị trí tương đối của mỗi gạch
        // so với điểm neo (anchor) của Boss.
        for (Brick brick : bricks) {
            int relX = brick.getX() - originalMinX;
            int relY = brick.getY() - (int) startY;
            relativePositions.put(brick, new Point(relX, relY));
        }
    }

    /**
     * Cập nhật logic di chuyển của Boss (được gọi mỗi frame).
     * <p>
     * 1. Di chuyển điểm neo ({@code this.x}) theo vận tốc ({@code dx}).
     * 2. Tính toán biên (bounding box) thực tế dựa trên các gạch con.
     * 3. Kiểm tra va chạm tường (biên màn hình) và đổi hướng nếu cần.
     * 4. Đồng bộ (sync) vị trí của tất cả gạch con ({@code brick.setX/setY})
     * dựa trên vị trí mới của điểm neo và offset đã lưu.
     */
    public void update() {
        // 1. Di chuyển điểm neo
        x += dx;

        // 2. Tính toán biên (bounding box) của boss
        // Tìm ra offset tương đối trái nhất và phải nhất của khối gạch
        int minRelX = Integer.MAX_VALUE;
        int maxRelX = Integer.MIN_VALUE;
        for (Point relPos : relativePositions.values()) {
            minRelX = Math.min(minRelX, relPos.x);
        }
        for (Map.Entry<Brick, Point> entry : relativePositions.entrySet()) {
            // Cạnh phải tương đối = offset X + chiều rộng gạch
            maxRelX = Math.max(maxRelX, entry.getValue().x + entry.getKey().getWidth());
        }
        int bossWidth = maxRelX - minRelX; // Chiều rộng thực tế của Boss

        // 3. Tính toán cạnh thực tế (đã di chuyển) và kiểm tra va chạm tường
        float leftEdge = this.x + minRelX; // Cạnh trái tuyệt đối
        float rightEdge = this.x + minRelX + bossWidth; // Cạnh phải tuyệt đối

        if (leftEdge <= 0) {
            dx = -dx; // Đảo chiều
            this.x = -minRelX; // Kẹp (clamp) vị trí để không đi lố
        } else if (rightEdge >= screenWidth) {
            dx = -dx; // Đảo chiều
            this.x = screenWidth - bossWidth - minRelX; // Kẹp vị trí
        }

        // 4. Đồng bộ vị trí của tất cả gạch con
        for (Brick brick : bricks) {
            Point relPos = relativePositions.get(brick);
            if (relPos != null) {
                // Vị trí mới = Vị trí neo + Vị trí tương đối đã lưu
                brick.setX((int) this.x + relPos.x);
                brick.setY((int) this.y + relPos.y);
            }
        }
    }

    /**
     * Dọn dẹp (cleanup) các gạch đã bị phá hủy.
     * <p>
     * Xóa gạch khỏi danh sách {@code bricks} và
     * khỏi bản đồ {@code relativePositions} để chúng không còn
     * được cập nhật hoặc tính toán trong bounding box.
     */
    public void removeDestroyedBricks() {
        bricks.removeIf(brick -> {
            if (brick.isDestroyed()) {
                relativePositions.remove(brick); // Xóa gạch khỏi map
                return true; // Xóa gạch khỏi list
            }
            return false;
        });
    }

    /**
     * Kiểm tra xem Boss đã bị đánh bại hay chưa.
     * @return true nếu danh sách {@code bricks} rỗng, false nếu còn.
     */
    public boolean isDefeated() {
        return bricks.isEmpty();
    }

    /**
     * Lấy danh sách các gạch con (còn sống) của Boss.
     * @return Danh sách các {@link Brick}.
     */
    public List<Brick> getBricks() {
        return bricks;
    }
}