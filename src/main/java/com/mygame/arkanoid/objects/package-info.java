/**
 * Chứa các lớp đại diện cho các đối tượng (entities) chính trong game Arkanoid.
 * <p>
 * Gói này định nghĩa các lớp cơ sở trừu tượng như
 * {@link com.mygame.arkanoid.objects.GameObject} (đối tượng game cơ bản) và
 * {@link com.mygame.arkanoid.objects.MovableObject} (đối tượng có thể di chuyển).
 * <p>
 * Nó cũng chứa các thực thể cốt lõi của trò chơi:
 * <ul>
 * <li>{@link com.mygame.arkanoid.objects.Paddle}: Thanh đỡ (vợt) do người chơi điều khiển.</li>
 * <li>{@link com.mygame.arkanoid.objects.Ball}: Quả bóng chính của game.</li>
 * <li>{@link com.mygame.arkanoid.objects.Laser}: Đạn laser được bắn từ Paddle.</li>
 * <li>{@link com.mygame.arkanoid.objects.Boss}: Thực thể trùm (boss) trong các màn chơi đặc biệt.</li>
 * </ul>
 * Các loại đối tượng cụ thể hơn như Gạch (Bricks) và Vật phẩm (Powerups) được
 * tổ chức trong các gói con (sub-packages).
 */
package com.mygame.arkanoid.objects;