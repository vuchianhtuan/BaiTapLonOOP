package com.mygame.arkanoid.engine;

/**
 * Ngoại lệ dùng khi không thể tải tài nguyên (ảnh, âm thanh, v.v.)
 */
public class LoadException extends Exception {
    /**
     * Tạo LoadException với thông điệp lỗi.
     */
    public LoadException(String message) {
        super(message);
    }

    /**
     * Tạo LoadException với thông điệp lỗi và nguyên nhân gốc.
     */
    public LoadException(String message, Throwable cause) {
        super(message, cause);
    }
}
