/**
 * Cung cấp các lớp cốt lõi (core) điều khiển vòng lặp và trạng thái chính của game Arkanoid.
 * <p>
 * Đây là trung tâm của trò chơi, chứa các thành phần thiết yếu:
 * <ul>
 * <li>{@link com.mygame.arkanoid.core.GameManager}: Bộ não chính, quản lý trạng thái game (menu, playing, game over),
 * logic cập nhật, và điều phối các hệ thống khác.</li>
 * <li>{@link com.mygame.arkanoid.core.GameLoop}: Một luồng (Thread) riêng biệt chạy vòng lặp game,
 * đảm bảo việc cập nhật (update) và vẽ (render) diễn ra ổn định ở một FPS mục tiêu.</li>
 * <li>{@link com.mygame.arkanoid.core.GamePanel}: Component Swing (JPanel) chính,
 * chịu trách nhiệm vẽ tất cả đồ họa của game lên màn hình và nhận input.</li>
 * </ul>
 * </p>
 */
package com.mygame.arkanoid.core;