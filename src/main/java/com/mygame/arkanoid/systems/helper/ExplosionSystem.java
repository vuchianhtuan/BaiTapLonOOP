// File: 'src/main/java/com/mygame/arkanoid/systems/ExplosionSystem.java'
package com.mygame.arkanoid.systems.helper;

import com.mygame.arkanoid.core.GameManager;
import com.mygame.arkanoid.objects.bricks.Shard;
import com.mygame.arkanoid.objects.bricks.Brick;
import com.mygame.arkanoid.objects.bricks.ExplosiveBrick;

import java.util.ArrayList;
import java.util.List;

public class ExplosionSystem {

    // Giữ nguyên logic cũ, chỉ di dời khỏi GameManager
    public void explode(Brick sourceBrick, double radius, GameManager gm) {
        int sourceCenterX = sourceBrick.getX() + sourceBrick.getWidth() / 2;
        int sourceCenterY = sourceBrick.getY() + sourceBrick.getHeight() / 2;

        // Tốc độ lan truyền của vụ nổ (số frame tối đa để lan truyền hết bán kính)
        // Bạn có thể điều chỉnh số này, 30 frame là nửa giây (ở 60FPS)
        final float MAX_PROPAGATION_FRAMES = 30.0f;

        List<Brick> allActiveBricks = new ArrayList<>();
        allActiveBricks.addAll(gm.getLaserShooters());
        allActiveBricks.addAll(gm.getBricks());

        for (Brick otherBrick : allActiveBricks) {
            if (otherBrick == sourceBrick) continue;

            int otherCenterX = otherBrick.getX() + otherBrick.getWidth() / 2;
            int otherCenterY = otherBrick.getY() + otherBrick.getHeight() / 2;
            double dx = sourceCenterX - otherCenterX;
            double dy = sourceCenterY - otherCenterY;
            double distance = Math.sqrt(dx * dx + dy * dy);

            // Nếu gạch nằm trong bán kính nổ
            if (distance <= radius) {

                // Tính toán độ trễ dựa trên khoảng cách
                // (distance / radius) là tỉ lệ từ 0.0 đến 1.0
                // Gạch ở gần (distance = 0) -> delay = 0
                // Gạch ở xa (distance = radius) -> delay = MAX_PROPAGATION_FRAMES
                int delay = (int) ((distance / radius) * MAX_PROPAGATION_FRAMES);

                // Kiểm tra xem gạch lân cận có phải là gạch nổ không
                if (otherBrick instanceof ExplosiveBrick) {
                    ExplosiveBrick eb = (ExplosiveBrick) otherBrick;

                    // Chỉ kích hoạt nếu nó còn sống
                    if (eb.isAlive()) {
                        eb.ignite(delay);
                    }
                } else if (!otherBrick.isDestroyed()) {
                    boolean wasAboutToDie = otherBrick.getHitPoints() == 1;

                    otherBrick.takeHit();
                    gm.addScore(10);

                    if (otherBrick.isDestroyed()) {
                        List<Shard> shards = otherBrick.shatter();
                        gm.getActiveShards().addAll(shards);
                    } else if (!wasAboutToDie) {
                        // Chỉ tạo vỡ vụn nhẹ nếu nó chưa vỡ (HP > 0) và không phải là cú đánh chí mạng
                        otherBrick.shatterHit(gm.getActiveShards());
                    }
                }
            }
        }
    }
}
