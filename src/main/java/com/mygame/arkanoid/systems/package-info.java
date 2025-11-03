/**
 * Chứa các hệ thống (Systems) quản lý logic nghiệp vụ cụ thể của game.
 * <p>
 * Không giống như 'core' (quản lý vòng lặp) hay 'engine' (công cụ cấp thấp),
 * các lớp trong gói này chịu trách nhiệm cho các cơ chế gameplay cụ thể,
 * hoạt động trên các đối tượng (objects).
 * <ul>
 * <li>{@link com.mygame.arkanoid.systems.EntityManager}: Quản lý vòng đời
 * của các thực thể (thêm, xóa, cập nhật).</li>
 * <li>{@link com.mygame.arkanoid.systems.CollisionSystem}: Xử lý logic va chạm
 * giữa các đối tượng (ví dụ: bóng và gạch, bóng và paddle).</li>
 * <li>{@link com.mygame.arkanoid.systems.ExplosionSystem}: Quản lý logic
 * khi gạch nổ (ExplosiveBrick) được kích hoạt.</li>
 * <li>{@link com.mygame.arkanoid.systems.ScalingManager}: Xử lý việc
 * co giãn (scaling) kích thước game để phù hợp với cửa sổ.</li>
 * <li>{@link com.mygame.arkanoid.systems.PlayerStats}: Theo dõi và quản lý
 * trạng thái của người chơi (điểm, mạng sống, v.v.).</li>
 * </ul>
 * </p>
 */
package com.mygame.arkanoid.systems;