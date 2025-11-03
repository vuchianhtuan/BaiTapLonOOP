/**
 * Chứa các lớp đại diện cho tất cả các loại Gạch (Bricks) trong game Arkanoid.
 * <p>
 * Gói này bao gồm lớp cơ sở {@link com.mygame.arkanoid.objects.bricks.Brick}
 * và các lớp con cụ thể định nghĩa hành vi cho từng loại gạch, chẳng hạn như:
 * <ul>
 * <li>{@link com.mygame.arkanoid.objects.bricks.NormalBrick}: Gạch tiêu chuẩn.</li>
 * <li>{@link com.mygame.arkanoid.objects.bricks.StrongBrick}: Gạch cần nhiều lần va chạm để vỡ.</li>
 * <li>{@link com.mygame.arkanoid.objects.bricks.ExplosiveBrick}: Gạch nổ, phá hủy các gạch lân cận.</li>
 * <li>{@link com.mygame.arkanoid.objects.bricks.MovingBrick}: Gạch di chuyển.</li>
 * </ul>
 * Gói này cũng chứa {@link com.mygame.arkanoid.objects.bricks.BrickFactory}
 * chịu trách nhiệm tạo ra các đối tượng gạch dựa trên dữ liệu từ màn chơi (level).
 * </p>
 */
package com.mygame.arkanoid.objects.bricks;