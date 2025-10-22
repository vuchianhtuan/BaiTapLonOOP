package com.mygame.arkanoid.engine;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.*;
import java.net.URL;

public class SoundManager {
 /*   public void playSound(String soundName) {}
    public void playBackgroundMusic(String musicName) {}
    public void stopBackgroundMusic() {}

  */

    private Clip backgroundMusicClip;
    public static float volume = 0.8f;

    public void playBackgroundMusic(String musicName) {
        stopBackgroundMusic();
        try {
            URL url = this.getClass().getResource("/sounds/" + musicName);

            if (url == null) {
                System.err.println("Không tìm thấy file âm thanh: /sound/" + musicName);
                return;
            }

            AudioInputStream audioInput = AudioSystem.getAudioInputStream(url);
            backgroundMusicClip = AudioSystem.getClip();
            backgroundMusicClip.open(audioInput);

            FloatControl gainControl = (FloatControl) backgroundMusicClip.getControl(FloatControl.Type.MASTER_GAIN);
            gainControl.setValue(-10.0f);

            backgroundMusicClip.loop(Clip.LOOP_CONTINUOUSLY);
            backgroundMusicClip.start();
            setVolume(backgroundMusicClip, volume);
        } catch (Exception e) {
            System.err.println("Lỗi khi phát nhạc nền: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void setVolume(Clip clip, float volume) {
        // Volume phải nằm trong khoảng [0.0f, 1.0f]
        if (volume < 0f || volume > 1f)
            throw new IllegalArgumentException("Volume outside valid range: " + volume);

        try {
            // 1. Kiểm tra xem Clip có hỗ trợ FloatControl.Type.MASTER_GAIN không
            if (clip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {

                // 2. Lấy đối tượng FloatControl
                FloatControl gainControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);

                // 3. Lấy giá trị Min và Max của Control này (thường là tính theo Decibel - dB)
                float minGain = gainControl.getMinimum();
                float maxGain = gainControl.getMaximum();

                // 4. Ánh xạ giá trị volume [0.0, 1.0] sang dải [minGain, maxGain] (logarithmic scale)

                // Công thức chuyển đổi tuyến tính từ volume [0.0, 1.0] sang dB (độ lợi)
                // Lưu ý: Mặc dù dB là logarit, nhưng thường người ta sử dụng công thức
                // này để mô phỏng cảm nhận âm lượng tuyến tính hơn.
                float range = maxGain - minGain;
                float gain = (range * volume) + minGain;

                // Hoặc, công thức dựa trên decibel thường thấy (để 1.0f tương ứng với 0dB)
                // float dB = (float)(Math.log(volume) / Math.log(10.0) * 20.0);

                // 5. Thiết lập giá trị mới cho Master Gain
                gainControl.setValue(gain);

                System.out.println("Volume set to: " + (int)(volume * 100) + "%");
            } else {
                System.err.println("MASTER_GAIN control is not supported for this Clip.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void setVolume(float vol) {
        volume = vol;
    }

    public void stopBackgroundMusic() {
        if (backgroundMusicClip != null && backgroundMusicClip.isRunning()) {
            backgroundMusicClip.stop();
            backgroundMusicClip.close();
        }
    }

    public void playSound(String soundName) {
        try {
            URL url = this.getClass().getResource("/sound/" + soundName);

            if (url == null) {
                System.err.println("Không tìm thấy file âm thanh: /sound/" + soundName);
                return;
            }
            AudioInputStream audioInput = AudioSystem.getAudioInputStream(url);
            Clip clip = AudioSystem.getClip();
            clip.open(audioInput);
            clip.start();
        } catch (Exception e) {
            System.err.println("Lỗi khi phát âm thanh: " + e.getMessage());
        }
    }
}
