package com.mygame.arkanoid.engine;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.*;
import java.net.URL;

public class SoundManager {
    private Clip backgroundMusicClip;
    // THAY ĐỔI: Chuyển 'volume' thành non-static
    private static float volume = 1.0f; // Mặc định âm lượng tối đa

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

            // THAY ĐỔI: Gọi hàm setVolume (non-static)
            setVolume(backgroundMusicClip, this.volume);

            backgroundMusicClip.loop(Clip.LOOP_CONTINUOUSLY);
            backgroundMusicClip.start();

        } catch (Exception e) {
            System.err.println("Lỗi khi phát nhạc nền: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // THAY ĐỔI: Chuyển hàm này thành 'private' và 'non-static'
    // Đây là hàm nội bộ để áp dụng âm lượng cho 1 clip cụ thể
    private void setVolume(Clip clip, float volume) {
        if (volume < 0f || volume > 1f)
            throw new IllegalArgumentException("Volume outside valid range: " + volume);

        try {
            if (clip != null && clip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
                FloatControl gainControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
                float range = gainControl.getMaximum() - gainControl.getMinimum();
                float gain = (range * volume) + gainControl.getMinimum();
                gainControl.setValue(gain);
            } else if(clip != null) {
                System.err.println("MASTER_GAIN control is not supported for this Clip.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // THAY ĐỔI: Đây là hàm 'public' (non-static) mà SetupVolume sẽ gọi
    // Nó cập nhật âm lượng chung VÀ âm lượng của nhạc nền đang phát
    public void setVolume(float vol) {
        if (vol < 0f) vol = 0f;
        if (vol > 1f) vol = 1f;

        this.volume = vol; // Cập nhật âm lượng chung

        // Áp dụng ngay cho nhạc nền đang phát (nếu có)
        if (backgroundMusicClip != null) {
            setVolume(backgroundMusicClip, this.volume);
        }
    }

    // HÀM MỚI: Dành cho GameManager khi PAUSED
    public void pauseBackgroundMusic() {
        if (backgroundMusicClip != null && backgroundMusicClip.isRunning()) {
            backgroundMusicClip.stop();
        }
    }

    // HÀM MỚI: Dành cho GameManager khi RESUME
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

    public void playSound(String soundName) {
        try {
            // THAY ĐỔI: Sửa lỗi đường dẫn "/sound/" -> "/sounds/"
            URL url = this.getClass().getResource("/sounds/" + soundName);

            if (url == null) {
                // THAY ĐỔI: Cập nhật thông báo lỗi
                System.err.println("Không tìm thấy file âm thanh: /sounds/" + soundName);
                return;
            }
            AudioInputStream audioInput = AudioSystem.getAudioInputStream(url);
            Clip clip = AudioSystem.getClip();
            clip.open(audioInput);

            // THÊM MỚI: Áp dụng âm lượng cho hiệu ứng âm thanh
            setVolume(clip, this.volume);

            clip.start();
        } catch (Exception e) {
            System.err.println("Lỗi khi phát âm thanh: " + e.getMessage());
        }
    }
}
