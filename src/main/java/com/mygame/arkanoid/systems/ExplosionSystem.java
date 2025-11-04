package com.mygame.arkanoid.systems;

import com.mygame.arkanoid.core.GameManager;
import com.mygame.arkanoid.effects.Shard;
import com.mygame.arkanoid.objects.bricks.Brick;
import com.mygame.arkanoid.objects.bricks.ExplosiveBrick;

import java.util.ArrayList;
import java.util.List;

/**
 * Hệ thống xử lý logic vụ nổ (Explosion System).
 * <p>
 * Lớp này chịu trách nhiệm tính toán sát thương và hiệu ứng
 * (ví dụ: nổ dây chuyền - chain reaction) khi một
 * {@link ExplosiveBrick} phát nổ.
 * <p>
 * Hệ thống này không có trạng thái (stateless), nó chỉ chứa logic
 * được gọi bởi {@link CollisionSystem} hoặc {@link GameManager}.
 */
public class ExplosionSystem {

    /**
     * Kích hoạt một vụ nổ tại vị trí của {@code sourceBrick}.
     * <p>
     * Vụ nổ này sẽ:
     * 1. Tìm tất cả các gạch khác trong một {@code radius} (bán kính) nhất định.
     * 2. Gây sát thương ({@code takeHit()}) cho gạch thường/cứng trong bán kính.
     * 3. Kích hoạt ({@code ignite()}) các gạch {@code ExplosiveBrick} khác
     * trong bán kính, với một độ trễ (delay) dựa trên khoảng cách,
     * tạo ra hiệu ứng nổ dây chuyền (chain reaction).
     *
     * @param sourceBrick Viên gạch (thường là {@code ExplosiveBrick})
     * phát ra vụ nổ.
     * @param radius Bán kính ảnh hưởng (logic) của vụ nổ.
     * @param gm {@link GameManager} để truy cập danh sách gạch và thêm điểm/mảnh vỡ.
     */
    public void explode(Brick sourceBrick, double radius, GameManager gm) {
        // Tính toán tâm của vụ nổ (nguồn)
        int sourceCenterX = sourceBrick.getX() + sourceBrick.getWidth() / 2;
        int sourceCenterY = sourceBrick.getY() + sourceBrick.getHeight() / 2;

        /**
         * Thời gian (frame) tối đa để sóng xung kích (shockwave)
         * lan truyền đến mép bán kính.
         * Dùng để tính toán độ trễ (delay) của vụ nổ dây chuyền.
         */
        final float MAX_PROPAGATION_FRAMES = 5.0f;

        // Tập hợp tất cả các gạch "còn sống" để kiểm tra
        List<Brick> allActiveBricks = new ArrayList<>();
        allActiveBricks.addAll(gm.getLaserShooters());
        allActiveBricks.addAll(gm.getBricks());

        // Duyệt qua tất cả các gạch khác
        for (Brick otherBrick : allActiveBricks) {
            if (otherBrick == sourceBrick) continue; // Bỏ qua chính nó

            // Tính khoảng cách từ tâm vụ nổ đến tâm gạch lân cận
            int otherCenterX = otherBrick.getX() + otherBrick.getWidth() / 2;
            int otherCenterY = otherBrick.getY() + otherBrick.getHeight() / 2;
            double dx = sourceCenterX - otherCenterX;
            double dy = sourceCenterY - otherCenterY;
            double distance = Math.sqrt(dx * dx + dy * dy);

            // Nếu gạch lân cận nằm trong bán kính nổ
            if (distance <= radius) {

                // Tính toán độ trễ (delay) dựa trên khoảng cách
                // (distance / radius) là tỉ lệ từ 0.0 (gần) đến 1.0 (xa)
                // Gạch ở gần (distance = 0) -> delay = 0
                // Gạch ở xa (distance = radius) -> delay = MAX_PROPAGATION_FRAMES
                int delay = (int) ((distance / radius) * MAX_PROPAGATION_FRAMES);

                // --- Xử lý logic dựa trên loại gạch lân cận ---

                // 1. Nếu là GẠCH NỔ (ExplosiveBrick) khác
                if (otherBrick instanceof ExplosiveBrick) {
                    ExplosiveBrick eb = (ExplosiveBrick) otherBrick;

                    // Chỉ kích hoạt (ignite) nếu nó còn sống (chưa bị kích hoạt)
                    if (eb.isAlive()) {
                        eb.ignite(delay); // Bắt đầu đếm ngược nổ dây chuyền
                    }
                }
                // 2. Nếu là GẠCH THƯỜNG/CỨNG (và chưa bị phá hủy)
                else if (!otherBrick.isDestroyed()) {
                    // Kiểm tra xem cú đánh này có "kết liễu" gạch hay không
                    boolean wasAboutToDie = otherBrick.getHitPoints() == 1;

                    otherBrick.takeHit(); // Gây sát thương
                    gm.addScore(10); // Cộng điểm

                    if (otherBrick.isDestroyed()) {
                        // Nếu gạch bị phá hủy, tạo mảnh vỡ lớn
                        List<Shard> shards = otherBrick.shatter();
                        gm.getActiveShards().addAll(shards);
                    } else if (!wasAboutToDie) {
                        // Nếu gạch bị trúng nhưng chưa chết (HP > 1)
                        // Chỉ tạo mảnh vỡ nhỏ (hiệu ứng va chạm)
                        otherBrick.shatterHit(gm.getActiveShards());
                    }
                }
            }
        }
    }
}