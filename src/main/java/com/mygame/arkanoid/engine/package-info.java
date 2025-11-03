/**
 * Cung cấp các hệ thống kỹ thuật cấp thấp (engine) hỗ trợ cho game Arkanoid.
 * <p>
 * Gói này chứa các lớp "trụ cột" không liên quan trực tiếp đến logic game
 * (như Bricks, Ball) mà thay vào đó cung cấp các dịch vụ cần thiết
 * để game có thể chạy, bao gồm:
 * <ul>
 * <li>{@link com.mygame.arkanoid.engine.AssetManager}: Tải và quản lý tài nguyên (hình ảnh, font).</li>
 * <li>{@link com.mygame.arkanoid.engine.SoundManager}: Tải và phát âm thanh.</li>
 * <li>{@link com.mygame.arkanoid.engine.InputHandler}: Lắng nghe và xử lý sự kiện chuột và bàn phím.</li>
 * <li>{@link com.mygame.arkanoid.engine.Renderer}: Cung cấp các phương thức tiện ích để vẽ các đối tượng game.</li>
 * </ul>
 * </p>
 */
package com.mygame.arkanoid.engine;