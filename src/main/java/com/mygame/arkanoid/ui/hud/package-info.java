/**
 * Chịu trách nhiệm hiển thị Giao diện Người dùng trong lúc chơi (Heads-Up Display - HUD).
 * <p>
 * Các lớp trong gói này vẽ các thông tin quan trọng trực tiếp lên màn hình chơi,
 * giúp người chơi theo dõi trạng thái game.
 * <ul>
 * <li>{@link com.mygame.arkanoid.ui.hud.Sidebar}: Quản lý và vẽ khu vực
 * thanh bên (sidebar) chứa điểm số, mạng sống, v.v.</li>
 * <li>{@link com.mygame.arkanoid.ui.hud.ScoreUI}: Vẽ điểm số.</li>
 * <li>{@link com.mygame.arkanoid.ui.hud.HeartUI}: Vẽ hiển thị mạng sống (hình trái tim).</li>
 * <li>{@link com.mygame.arkanoid.ui.hud.GameSummaryPanel}: Hiển thị bảng
 * tóm tắt khi kết thúc game (Game Over / You Win).</li>
 * </ul>
 * </p>
 */
package com.mygame.arkanoid.ui.hud;