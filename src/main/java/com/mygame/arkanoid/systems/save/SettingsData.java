package com.mygame.arkanoid.systems.save;

import java.io.Serializable;

/**
 * Dữ liệu thiết lập (ví dụ âm lượng). Mở rộng thêm nếu cần.
 */
public class SettingsData implements Serializable {
    private static final long serialVersionUID = 1L;

    private float musicVolume = 1.0f; // 0..1
    private float sfxVolume = 1.0f;   // 0..1
    private boolean fullscreen = false;

    public float getMusicVolume() { return musicVolume; }
    public void setMusicVolume(float musicVolume) { this.musicVolume = clamp01(musicVolume); }

    public float getSfxVolume() { return sfxVolume; }
    public void setSfxVolume(float sfxVolume) { this.sfxVolume = clamp01(sfxVolume); }

    public boolean isFullscreen() { return fullscreen; }
    public void setFullscreen(boolean fullscreen) { this.fullscreen = fullscreen; }

    private float clamp01(float v) { return Math.max(0f, Math.min(1f, v)); }
}
