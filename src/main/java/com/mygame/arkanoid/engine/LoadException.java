package com.mygame.arkanoid.engine;

/**
 * Ngoại lệ dùng khi không thể tải tài nguyên (ảnh, âm thanh, v.v.)
 */
public class LoadException extends Exception {
    /**
     * Tạo ngoại lệ với thông điệp cụ thể.
     * @param message
     */
    public LoadException(String message) {
        super(message);
    }

    /**
     * Tạo ngoại lệ với thông điệp cụ thể và nguyên nhân gốc.
     * @param message
     * @param cause
     */
    public LoadException(String message, Throwable cause) {
        super(message, cause);
    }
}
