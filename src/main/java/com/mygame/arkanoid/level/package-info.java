/**
 * Quản lý mọi thứ liên quan đến các màn chơi (levels) của Arkanoid.
 * <p>
 * Gói này chứa các lớp định nghĩa cấu trúc của một màn chơi,
 * quản lý việc tải và chuyển đổi giữa các màn chơi.
 * <ul>
 * <li>{@link com.mygame.arkanoid.level.Level}: Định nghĩa cấu trúc của một màn chơi,
 * bao gồm cách sắp xếp gạch và các thuộc tính khác.</li>
 * <li>{@link com.mygame.arkanoid.level.LevelManager}: Chịu trách nhiệm tải
 * và quản lý màn chơi hiện tại.</li>
 * <li>{@link com.mygame.arkanoid.level.LevelTransition}: Xử lý các hiệu ứng
 * hình ảnh và logic khi chuyển từ màn chơi này sang màn chơi khác.</li>
 * <li>{@link com.mygame.arkanoid.level.LevelTextAnimation}: Hiển thị
 * các hiệu ứng văn bản liên quan đến màn chơi (ví dụ: "Level 1",...).</li>
 * </ul>
 * </p>
 */
package com.mygame.arkanoid.level;