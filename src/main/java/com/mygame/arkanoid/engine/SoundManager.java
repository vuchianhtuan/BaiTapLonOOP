package com.mygame.arkanoid.engine;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;
import java.net.URL;

public class SoundManager {
 /*   public void playSound(String soundName) {}
    public void playBackgroundMusic(String musicName) {}
    public void stopBackgroundMusic() {}

  */

    private Clip backgroundMusicClip;

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
        } catch (Exception e) {
            System.err.println("Lỗi khi phát nhạc nền: " + e.getMessage());
            e.printStackTrace();
        }
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
