/**
 * Chứa các lớp định nghĩa tất cả các vật phẩm tăng sức mạnh (Power-Ups) trong game.
 * <p>
 * Gói này bao gồm lớp cơ sở trừu tượng {@link com.mygame.arkanoid.objects.powerups.PowerUp}
 * và các lớp con cụ thể cho từng loại vật phẩm. Mỗi lớp con sẽ định nghĩa
 * hành vi và hiệu ứng riêng khi được kích hoạt, ví dụ:
 * <ul>
 * <li>{@link com.mygame.arkanoid.objects.powerups.ExpandPaddlePowerUp}: Làm thanh đỡ dài ra.</li>
 * <li>{@link com.mygame.arkanoid.objects.powerups.MultiBallPowerUp}: Tách bóng ra làm nhiều quả.</li>
 * <li>{@link com.mygame.arkanoid.objects.powerups.FastBallPowerUp}: Tăng tốc độ bóng.</li>
 * <li>{@link com.mygame.arkanoid.objects.powerups.SlowBallPowerUp}: Giảm tốc độ bóng.</li>
 * <li>{@link com.mygame.arkanoid.objects.powerups.StickyPaddlePowerUp}: Giữ bóng dính vào thanh đỡ.</li>
 * </ul>
 * </p>
 */
package com.mygame.arkanoid.objects.powerups;