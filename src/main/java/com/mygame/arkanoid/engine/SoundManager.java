package com.mygame.arkanoid.engine;

import javax.sound.sampled.*;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class SoundManager {

    // --- Hằng số tên file SFX (đặt trong /sounds/) ---
    public static final String SFX_BRICK_HIT = "gach_vo.wav";
    public static final String SFX_PADDLE_HIT = "paddle_cham.wav";
    public static final String SFX_BALL_LOSS = "falling.wav";
    public static final String SFX_EXPLOSION = "no.wav";
    public static final String SFX_POWERUP = "powerUpSound.wav";


    // --- Volume settings ---
    private float masterVolume = 1.0f;
    private float musicVolume = 0.8f;
    private float sfxVolume = 0.5f;
    private boolean muted = false;

    // --- Background music clip ---
    private Clip backgroundMusicClip;

    // --- Preloaded sound data (PCM bytes and formats) ---
    private final Map<String, byte[]> soundData = new HashMap<>();
    private final Map<String, AudioFormat> soundFormat = new HashMap<>();

    public SoundManager() {
        // Gọi preload khi tạo SoundManager. Nếu muốn gọi ở chỗ khác, bỏ dòng này
        preloadAllSfx();
    }

    // =====================
    // Background music APIs
    // =====================

    public void playBackgroundMusic(String musicName) {
        stopBackgroundMusic();
        try {
            URL url = this.getClass().getResource("/sounds/" + musicName);
            if (url == null) {
                System.err.println("Không tìm thấy file âm thanh: /sounds/" + musicName);
                return;
            }

            AudioInputStream audioInput = AudioSystem.getAudioInputStream(url);
            // chuyển về PCM để đảm bảo tương thích
            AudioFormat baseFormat = audioInput.getFormat();
            AudioFormat decodedFormat = new AudioFormat(
                    AudioFormat.Encoding.PCM_SIGNED,
                    baseFormat.getSampleRate(),
                    16,
                    baseFormat.getChannels(),
                    baseFormat.getChannels() * 2,
                    baseFormat.getSampleRate(),
                    false
            );
            AudioInputStream dais = AudioSystem.getAudioInputStream(decodedFormat, audioInput);

            backgroundMusicClip = AudioSystem.getClip();
            backgroundMusicClip.open(dais);

            updateBackgroundMusicVolume();
            backgroundMusicClip.loop(Clip.LOOP_CONTINUOUSLY);
            backgroundMusicClip.start();

        } catch (Exception e) {
            System.err.println("Lỗi khi phát nhạc nền: " + e.getMessage());
            e.printStackTrace();
        }
    }

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
            backgroundMusicClip = null;
        }
    }

    // =====================
    // Preload and fast play
    // =====================

    // Preload single sound: đọc file, chuyển về PCM_SIGNED, lưu bytes và format
    public void preloadSound(String soundName) {
        try {
            URL url = this.getClass().getResource("/sounds/" + soundName);
            if (url == null) {
                System.err.println("Không tìm thấy file âm thanh: /sounds/" + soundName);
                return;
            }

            try (AudioInputStream ais = AudioSystem.getAudioInputStream(url)) {
                AudioFormat baseFormat = ais.getFormat();
                AudioFormat decodedFormat = new AudioFormat(
                        AudioFormat.Encoding.PCM_SIGNED,
                        baseFormat.getSampleRate(),
                        16,
                        baseFormat.getChannels(),
                        baseFormat.getChannels() * 2,
                        baseFormat.getSampleRate(),
                        false
                );
                try (AudioInputStream dais = AudioSystem.getAudioInputStream(decodedFormat, ais);
                     ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

                    byte[] buffer = new byte[4096];
                    int read;
                    while ((read = dais.read(buffer)) != -1) {
                        baos.write(buffer, 0, read);
                    }
                    byte[] audioBytes = baos.toByteArray();
                    soundData.put(soundName, audioBytes);
                    soundFormat.put(soundName, decodedFormat);
                }
            }
        } catch (Exception e) {
            System.err.println("Lỗi preload âm thanh: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Preload các SFX chính; gọi ở constructor hoặc màn loading
    public void preloadAllSfx() {
        preloadSound(SFX_BRICK_HIT);
        preloadSound(SFX_PADDLE_HIT);
        preloadSound(SFX_BALL_LOSS);
        preloadSound(SFX_EXPLOSION);
    }

    // Phát SFX nhanh từ bộ nhớ đã preload. Nếu chưa preload, sẽ cố gắng preload rồi phát.
    public void playSound(String soundName) {
        if (muted) return;

        byte[] audioBytes = soundData.get(soundName);
        AudioFormat format = soundFormat.get(soundName);

        if (audioBytes == null || format == null) {
            // Fallback: preload on demand (chậm lần đầu)
            preloadSound(soundName);
            audioBytes = soundData.get(soundName);
            format = soundFormat.get(soundName);
            if (audioBytes == null || format == null) return;
        }

        AudioInputStream ais = new AudioInputStream(
                new ByteArrayInputStream(audioBytes),
                format,
                audioBytes.length / format.getFrameSize());

        try {
            Clip clip = AudioSystem.getClip();
            clip.open(ais);

            float effectiveSfxVolume = muted ? 0.0f : masterVolume * sfxVolume;
            setClipVolume(clip, effectiveSfxVolume);

            clip.addLineListener(event -> {
                if (event.getType() == LineEvent.Type.STOP) {
                    clip.close();
                }
            });

            clip.start();
        } catch (Exception e) {
            System.err.println("Lỗi khi phát âm thanh nhanh: " + e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                ais.close();
            } catch (Exception ignored) {
            }
        }
    }

    // =====================
    // Volume helpers
    // =====================

    private void updateBackgroundMusicVolume() {
        if (backgroundMusicClip != null) {
            float effectiveMusicVolume = muted ? 0.0f : masterVolume * musicVolume;
            setClipVolume(backgroundMusicClip, effectiveMusicVolume);
        }
    }

    private void setClipVolume(Clip clip, float volume) {
        if (volume < 0f) volume = 0f;
        if (volume > 1f) volume = 1f;

        try {
            if (clip != null && clip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
                FloatControl gainControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
                float min = gainControl.getMinimum();
                float max = gainControl.getMaximum();
                float dB;
                if (volume == 0f) {
                    dB = min;
                } else {
                    // mapping linear 0..1 to dB range (smooth)
                    dB = (float) (20.0 * Math.log10(volume));
                    if (dB < min) dB = min;
                    if (dB > max) dB = max;
                }
                gainControl.setValue(dB);
            } else if (clip != null) {
                System.err.println("MASTER_GAIN control không được hỗ trợ.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private float clamp(float value) {
        if (value < 0f) return 0f;
        if (value > 1f) return 1f;
        return value;
    }

    // =====================
    // Public setters / getters
    // =====================

    public void setMasterVolume(float volume) {
        this.masterVolume = clamp(volume);
        updateBackgroundMusicVolume();
    }

    public void setMusicVolume(float volume) {
        this.musicVolume = clamp(volume);
        updateBackgroundMusicVolume();
    }

    public void setSfxVolume(float volume) {
        this.sfxVolume = clamp(volume);
    }

    public void setMuted(boolean muted) {
        this.muted = muted;
        updateBackgroundMusicVolume();
    }

    public float getMasterVolume() {
        return masterVolume;
    }

    public float getMusicVolume() {
        return musicVolume;
    }

    public float getSfxVolume() {
        return sfxVolume;
    }

    public boolean isMuted() {
        return muted;
    }
}