package com.mygame.arkanoid.engine;

import javax.sound.sampled.*;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * Quản lý âm thanh trong game: nhạc nền và hiệu ứng âm thanh (SFX).
 */
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
        try {
            preloadAllSfx();
        } catch (LoadException e) {
            System.err.println("Không thể tải trước SFX: " + e.getMessage());
        }
    }

    /**
     * Phát nhạc nền từ file đã cho.
     * @param musicName
     */
    public void playBackgroundMusic(String musicName) {
        // Dừng nhạc nền hiện tại nếu có
        stopBackgroundMusic();
        // Bắt đầu phát nhạc nền mới
        try {
            URL url = this.getClass().getResource("/sounds/" + musicName);
            if (url == null) {
                System.err.println("Không tìm thấy file âm thanh: /sounds/" + musicName);
                return;
            }

            AudioInputStream audioInput = AudioSystem.getAudioInputStream(url); // Mở AudioInputStream gốc
            AudioFormat baseFormat = audioInput.getFormat(); // Lấy định dạng gốc
            // Chuyển đổi định dạng sang PCM_SIGNED nếu cần
            AudioFormat decodedFormat = new AudioFormat(
                    AudioFormat.Encoding.PCM_SIGNED,
                    baseFormat.getSampleRate(),
                    16,
                    baseFormat.getChannels(),
                    baseFormat.getChannels() * 2,
                    baseFormat.getSampleRate(),
                    false
            );
            // Tạo AudioInputStream đã giải mã
            AudioInputStream dais = AudioSystem.getAudioInputStream(decodedFormat, audioInput);

            backgroundMusicClip = AudioSystem.getClip();
            backgroundMusicClip.open(dais); // Mở clip với dữ liệu âm thanh đã giải mã

            updateBackgroundMusicVolume(); // Cập nhật âm lượng theo thiết lập hiện tại
            backgroundMusicClip.loop(Clip.LOOP_CONTINUOUSLY);
            backgroundMusicClip.start(); // Bắt đầu phát nhạc nền

        } catch (Exception e) {
            System.err.println("Lỗi khi phát nhạc nền: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Tạm dừng nhạc nền.
     */
    public void pauseBackgroundMusic() {
        if (backgroundMusicClip != null && backgroundMusicClip.isRunning()) {
            backgroundMusicClip.stop();
        }
    }

    /**
     * Tiếp tục phát nhạc nền.
     */
    public void resumeBackgroundMusic() {
        if (backgroundMusicClip != null && !backgroundMusicClip.isRunning()) {
            backgroundMusicClip.loop(Clip.LOOP_CONTINUOUSLY);
        }
    }

    /**
     * Dừng nhạc nền và giải phóng tài nguyên.
     */
    public void stopBackgroundMusic() {
        if (backgroundMusicClip != null) {
            if (backgroundMusicClip.isRunning()) {
                backgroundMusicClip.stop();
            }
            backgroundMusicClip.close();
            backgroundMusicClip = null;
        }
    }

    /**
     * Preload một file âm thanh vào bộ nhớ để phát nhanh sau này.
     */
    public void preloadSound(String soundName) throws LoadException {
        try {
            URL url = this.getClass().getResource("/sounds/" + soundName);
            if (url == null) {
                throw new LoadException("Không tìm thấy file âm thanh: /sounds/" + soundName);
            }

            try (AudioInputStream ais = AudioSystem.getAudioInputStream(url)) {
                // Chuyển đổi định dạng sang PCM_SIGNED
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
                    soundData.put(soundName, audioBytes); // Lưu dữ liệu âm thanh đã giải mã
                    soundFormat.put(soundName, decodedFormat); // Lưu định dạng âm thanh
                }
            }
        } catch (Exception e) {
            throw new LoadException("Lỗi khi preload âm thanh: " + soundName, e);
        }
    }

    /**
     * Preload tất cả SFX.
     */
    public void preloadAllSfx() throws LoadException {
        preloadSound(SFX_BRICK_HIT);
        preloadSound(SFX_PADDLE_HIT);
        preloadSound(SFX_BALL_LOSS);
        preloadSound(SFX_EXPLOSION);
    }

    /**
     * Phát một hiệu ứng âm thanh nhanh từ bộ nhớ đã preload.
     * Nếu chưa preload, sẽ thử preload ngay lúc này (chậm lần đầu).
     */
    public void playSound(String soundName) {
        if (muted) return;

        byte[] audioBytes = soundData.get(soundName);
        AudioFormat format = soundFormat.get(soundName);

        // Nếu chưa preload, thử preload ngay lúc này
        if (audioBytes == null || format == null) {
            // Fallback: preload on demand (chậm lần đầu)
            try {
                preloadSound(soundName);
                audioBytes = soundData.get(soundName);
                format = soundFormat.get(soundName);
                if (audioBytes == null || format == null) return;
            } catch (LoadException e) {
                System.err.println("Không thể nạp âm thanh khi đang phát: " + e.getMessage());
                return;
            }
        }
        // Tạo AudioInputStream từ byte array
        AudioInputStream ais = new AudioInputStream(
                new ByteArrayInputStream(audioBytes),
                format,
                audioBytes.length / format.getFrameSize());

        // Phát âm thanh nhanh
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

    /**
     * Cập nhật âm lượng nhạc nền dựa trên thiết lập hiện tại.
     */
    private void updateBackgroundMusicVolume() {
        if (backgroundMusicClip != null) {
            float effectiveMusicVolume = muted ? 0.0f : masterVolume * musicVolume;
            setClipVolume(backgroundMusicClip, effectiveMusicVolume); // Cập nhật âm lượng nhạc nền
        }
    }

    /**
     * Đặt âm lượng cho một Clip cụ thể.
     */
    private void setClipVolume(Clip clip, float volume) {
        if (volume < 0f) volume = 0f;
        if (volume > 1f) volume = 1f;

        // Chuyển đổi âm lượng tuyến tính (0.0 - 1.0) sang dB
        try {
            // Lấy điều khiển âm lượng MASTER_GAIN
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

    /**
     * Giới hạn giá trị từ 0.0 đến 1.0
     */
    private float clamp(float value) {
        if (value < 0f) return 0f;
        if (value > 1f) return 1f;
        return value;
    }

    /**
     * Đặt âm lượng tổng (master volume).
     */
    public void setMasterVolume(float volume) {
        this.masterVolume = clamp(volume);
        updateBackgroundMusicVolume();
    }

    /**
     * Đặt âm lượng nhạc nền.
     */
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