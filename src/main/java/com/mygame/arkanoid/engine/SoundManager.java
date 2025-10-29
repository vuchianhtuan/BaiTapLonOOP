package com.mygame.arkanoid.engine;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.*;
import java.net.URL;

public class SoundManager {

    // --- BỔ SUNG: Hằng số cho tên file âm thanh SFX ---
    // (Bạn hãy thay "ten_file.wav" bằng tên file thực tế của bạn trong thư mục /sounds/)
    public static final String SFX_BRICK_HIT = "gach_vo.wav";     // Ví dụ
    public static final String SFX_PADDLE_HIT = "paddle_cham.wav";  // Ví dụ
    public static final String SFX_BALL_LOSS = "mat_bong.wav";   // Ví dụ
    public static final String SFX_EXPLOSION = "no.wav";       // Ví dụ

    // --- THAY ĐỔI: Chia 1 volume thành 3 volume ---
    private float masterVolume = 1.0f; // Âm lượng tổng
    private float musicVolume = 0.8f;  // Âm lượng nhạc nền
    private float sfxVolume = 0.5f;    // Âm lượng hiệu ứng
    private boolean muted = false;     // Trạng thái tắt tiếng

    private Clip backgroundMusicClip;

    public void playBackgroundMusic(String musicName) {
        stopBackgroundMusic();
        try {
            URL url = this.getClass().getResource("/sounds/" + musicName);
            if (url == null) {
                System.err.println("Không tìm thấy file âm thanh: /sounds/" + musicName);
                return;
            }

            AudioInputStream audioInput = AudioSystem.getAudioInputStream(url);
            backgroundMusicClip = AudioSystem.getClip();
            backgroundMusicClip.open(audioInput);

            // THAY ĐỔI: Áp dụng âm lượng nhạc nền đã tính toán
            updateBackgroundMusicVolume(); // Sử dụng hàm helper mới

            backgroundMusicClip.loop(Clip.LOOP_CONTINUOUSLY);
            backgroundMusicClip.start();

        } catch (Exception e) {
            System.err.println("Lỗi khi phát nhạc nền: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void playSound(String soundName) {
        try {
            URL url = this.getClass().getResource("/sounds/" + soundName);
            if (url == null) {
                System.err.println("Không tìm thấy file âm thanh: /sounds/" + soundName);
                return;
            }
            AudioInputStream audioInput = AudioSystem.getAudioInputStream(url);
            Clip clip = AudioSystem.getClip();
            clip.open(audioInput);

            // THAY ĐỔI: Tính toán âm lượng hiệu ứng (SFX)
            float effectiveSfxVolume = muted ? 0.0f : masterVolume * sfxVolume;
            setClipVolume(clip, effectiveSfxVolume); // Áp dụng âm lượng đã tính

            // BỔ SUNG: Tự động đóng clip sau khi phát xong để giải phóng tài nguyên
            clip.addLineListener(event -> {
                if (event.getType() == LineEvent.Type.STOP) {
                    clip.close();
                }
            });

            clip.start();
        } catch (Exception e) {
            System.err.println("Lỗi khi phát âm thanh: " + e.getMessage());
        }
    }

    // --- CÁC HÀM SETTER VÀ GETTER MỚI ---

    // Hàm private để áp dụng âm lượng (0.0f - 1.0f) cho một Clip CỤ THỂ
    private void setClipVolume(Clip clip, float volume) {
        if (volume < 0f) volume = 0f;
        if (volume > 1f) volume = 1f;

        try {
            if (clip != null && clip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
                FloatControl gainControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
                // Chuyển đổi âm lượng (0-1) sang thang đo Decibel (dB)
                // -80.0f (tắt) đến 6.0206f (tối đa)
                float range = gainControl.getMaximum() - gainControl.getMinimum();
                float gain = (range * volume) + gainControl.getMinimum();
                gainControl.setValue(gain);
            } else if(clip != null) {
                System.err.println("MASTER_GAIN control không được hỗ trợ.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Hàm private để cập nhật nhạc nền đang phát (khi master, music, hoặc mute thay đổi)
    private void updateBackgroundMusicVolume() {
        if (backgroundMusicClip != null) {
            float effectiveMusicVolume = muted ? 0.0f : masterVolume * musicVolume;
            setClipVolume(backgroundMusicClip, effectiveMusicVolume);
        }
    }

    // Hàm tiện ích private để giới hạn giá trị từ 0.0f đến 1.0f
    private float clamp(float value) {
        if (value < 0f) return 0f;
        if (value > 1f) return 1f;
        return value;
    }

    // --- Các hàm public này sẽ được SettingManager gọi ---

    public void setMasterVolume(float volume) {
        this.masterVolume = clamp(volume);
        updateBackgroundMusicVolume(); // Cập nhật nhạc nền ngay lập tức
    }

    public void setMusicVolume(float volume) {
        this.musicVolume = clamp(volume);
        updateBackgroundMusicVolume(); // Cập nhật nhạc nền ngay lập tức
    }

    public void setSfxVolume(float volume) {
        this.sfxVolume = clamp(volume);
        // Không cần cập nhật gì ngay, vì SFX sẽ lấy giá trị này ở lần phát tiếp theo
    }

    public void setMuted(boolean muted) {
        this.muted = muted;
        updateBackgroundMusicVolume(); // Cập nhật nhạc nền ngay lập tức
    }

    // Các hàm Getter để SettingManager lấy giá trị ban đầu cho slider
    public float getMasterVolume() { return masterVolume; }
    public float getMusicVolume() { return musicVolume; }
    public float getSfxVolume() { return sfxVolume; }
    public boolean isMuted() { return muted; }

    // --- CÁC HÀM QUẢN LÝ NHẠC NỀN (Giữ nguyên) ---

    public void pauseBackgroundMusic() {
        if (backgroundMusicClip != null && backgroundMusicClip.isRunning()) {
            backgroundMusicClip.stop();
        }
    }

    public void resumeBackgroundMusic() {
        if (backgroundMusicClip != null && !backgroundMusicClip.isRunning()) {
            backgroundMusicClip.loop(Clip.LOOP_CONTINUOUSLY);
        }
    }

    public void stopBackgroundMusic() {
        if (backgroundMusicClip != null) {
            if (backgroundMusicClip.isRunning()) {
                backgroundMusicClip.stop();
            }
            backgroundMusicClip.close();
        }
    }

    // Xóa hàm setVolume(float vol) và getVolume() cũ
}