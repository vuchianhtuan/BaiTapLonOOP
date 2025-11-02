package com.mygame.arkanoid.engine;

/**
 * Ngoại lệ dùng khi không thể tải tài nguyên (ảnh, âm thanh, v.v.)
 */
public class LoadException extends Exception {
    public LoadException(String message) {
        super(message);
    }

    public LoadException(String message, Throwable cause) {
        super(message, cause);
    }
}
