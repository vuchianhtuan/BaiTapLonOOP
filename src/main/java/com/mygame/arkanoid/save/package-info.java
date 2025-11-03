/**
 * Chịu trách nhiệm xử lý việc lưu (save) và tải (load) tiến trình của trò chơi.
 * <p>
 * Gói này chứa các lớp cần thiết để tuần tự hóa (serialize) trạng thái game
 * và ghi ra file, cũng như đọc file lưu để khôi phục trạng thái.
 * <ul>
 * <li>{@link com.mygame.arkanoid.save.SaveData}: Lớp cấu trúc dữ liệu,
 * chứa thông tin cần lưu (ví dụ: điểm cao, màn chơi đã vượt qua).</li>
 * <li>{@link com.mygame.arkanoid.save.SaveSystem}: Cung cấp các phương thức
 * để thực hiện hành động lưu và tải dữ liệu vào/từ hệ thống tệp.</li>
 * </ul>
 * </p>
 */
package com.mygame.arkanoid.save;