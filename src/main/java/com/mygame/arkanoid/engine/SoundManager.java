package com.mygame.arkanoid.engine;

import javax.sound.sampled.*;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * Quản lý âm thanh của trò chơi.
 * Lớp này xử lý hai loại âm thanh riêng biệt:
 * <ol>
 * <li><b>Nhạc nền (Music):</b> Phát trực tuyến (streaming) từ tệp,
 * dùng một {@code Clip} duy nhất và lặp lại (loop).</li>
 * <li><b>Hiệu ứng (SFX):</b> Tải trước (preload) vào bộ nhớ (RAM)
 * để phát ngay lập tức, cho phép nhiều hiệu ứng chồng chéo.</li>
 * </ol>
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

    /** Clip duy nhất dùng để phát nhạc nền. */
    private Clip backgroundMusicClip;

    /** * Cache (bộ đệm) lưu trữ dữ liệu âm thanh (bytes) đã được giải mã của SFX.
     * Key là tên file (ví dụ: "no.wav").
     */
    private final Map<String, byte[]> soundData = new HashMap<>();
    /** * Cache lưu trữ định dạng (format) của dữ liệu âm thanh đã giải mã.
     */
    private final Map<String, AudioFormat> soundFormat = new HashMap<>();

    /**
     * Khởi tạo SoundManager.
     * Tự động tải trước (preload) tất cả các hiệu ứng âm thanh (SFX) quan trọng
     * vào bộ nhớ khi được tạo.
     */
    public SoundManager() {
        // Gọi preload khi tạo SoundManager. Nếu muốn gọi ở chỗ khác, bỏ dòng này
        try {
            preloadAllSfx();
        } catch (LoadException e) {
            System.err.println("Không thể tải trước SFX: " + e.getMessage());
        }
    }

    // =====================
    // Background music APIs
    // =====================

    /**
     * Phát một tệp nhạc nền mới.
     * <p>
     * Dừng bất kỳ nhạc nền nào đang phát, tải tệp mới (từ {@code /sounds/}),
     * giải mã (decode) sang định dạng PCM chuẩn và phát lặp lại (loop).
     *
     * @param musicName Tên tệp nhạc (ví dụ: "Menu.wav").
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

            // Chỉ định định dạng mục tiêu là PCM_SIGNED (chuẩn cho Clip)
            AudioFormat decodedFormat = new AudioFormat(
                    AudioFormat.Encoding.PCM_SIGNED,
                    baseFormat.getSampleRate(),
                    16, // 16-bit
                    baseFormat.getChannels(),
                    baseFormat.getChannels() * 2,
                    baseFormat.getSampleRate(),
                    false // Big-endian: false (little-endian)
            );

            // Tạo AudioInputStream đã giải mã (decode)
            AudioInputStream dais = AudioSystem.getAudioInputStream(decodedFormat, audioInput);

            backgroundMusicClip = AudioSystem.getClip();
            backgroundMusicClip.open(dais); // Mở clip với dữ liệu âm thanh đã giải mã

            updateBackgroundMusicVolume(); // Cập nhật âm lượng theo thiết lập hiện tại
            backgroundMusicClip.loop(Clip.LOOP_CONTINUOUSLY); // Lặp vô hạn
            backgroundMusicClip.start(); // Bắt đầu phát nhạc nền

        } catch (Exception e) {
            System.err.println("Lỗi khi phát nhạc nền: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Tạm dừng nhạc nền đang phát.
     */
    public void pauseBackgroundMusic() {
        if (backgroundMusicClip != null && backgroundMusicClip.isRunning()) {
            backgroundMusicClip.stop();
        }
    }

    /**
     * Tiếp tục phát nhạc nền (và đặt lại vòng lặp).
     */
    public void resumeBackgroundMusic() {
        if (backgroundMusicClip != null && !backgroundMusicClip.isRunning()) {
            backgroundMusicClip.loop(Clip.LOOP_CONTINUOUSLY);
        }
    }

    /**
     * Dừng hoàn toàn nhạc nền và giải phóng tài nguyên của {@code Clip}.
     */
    public void stopBackgroundMusic() {
        if (backgroundMusicClip != null) {
            if (backgroundMusicClip.isRunning()) {
                backgroundMusicClip.stop();
            }
            backgroundMusicClip.close(); // Giải phóng tài nguyên
            backgroundMusicClip = null;
        }
    }

    // =====================
    // Preload and fast play
    // =====================

    /**
     * Tải trước (preload) một tệp SFX vào bộ nhớ (RAM).
     * <p>
     * Đọc tệp âm thanh, giải mã (decode) sang định dạng PCM chuẩn,
     * và lưu trữ dữ liệu {@code byte[]} thô cùng với {@code AudioFormat} của nó
     * vào các (Maps) cache.
     *
     * @param soundName Tên tệp SFX (ví dụ: "gach_vo.wav").
     * @throws LoadException Nếu không tìm thấy tệp hoặc lỗi giải mã.
     */
    public void preloadSound(String soundName) throws LoadException {
        try {
            URL url = this.getClass().getResource("/sounds/" + soundName);
            if (url == null) {
                throw new LoadException("Không tìm thấy file âm thanh: /sounds/" + soundName);
            }

            try (AudioInputStream ais = AudioSystem.getAudioInputStream(url)) {
                // Chuyển đổi định dạng sang PCM_SIGNED (tương tự như nhạc nền)
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

                // Đọc toàn bộ dữ liệu đã giải mã vào một mảng byte[]
                try (AudioInputStream dais = AudioSystem.getAudioInputStream(decodedFormat, ais);
                     ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

                    byte[] buffer = new byte[4096];
                    int read;
                    while ((read = dais.read(buffer)) != -1) {
                        baos.write(buffer, 0, read);
                    }
                    byte[] audioBytes = baos.toByteArray();

                    // Lưu vào cache
                    soundData.put(soundName, audioBytes); // Lưu dữ liệu âm thanh đã giải mã
                    soundFormat.put(soundName, decodedFormat); // Lưu định dạng âm thanh
                }
            }
        } catch (Exception e) {
            throw new LoadException("Lỗi khi preload âm thanh: " + soundName, e);
        }
    }

    /**
     * Hàm tiện ích, gọi {@link #preloadSound} cho tất cả các SFX quan trọng.
     * @throws LoadException
     */
    public void preloadAllSfx() throws LoadException {
        preloadSound(SFX_BRICK_HIT);
        preloadSound(SFX_PADDLE_HIT);
        preloadSound(SFX_BALL_LOSS);
        preloadSound(SFX_EXPLOSION);
    }

    /**
     * Phát nhanh một hiệu ứng âm thanh (SFX) đã được tải trước.
     * <p>
     * Tạo một {@code Clip} mới ngay lập tức từ dữ liệu {@code byte[]} trong bộ nhớ.
     * Cách này cho phép nhiều âm thanh giống hệt nhau phát chồng chéo.
     * Clip sẽ tự động được đóng ({@code close()}) sau khi phát xong.
     * <p>
     * <b>Fallback:</b> Nếu âm thanh chưa được tải trước, hàm sẽ cố gắng
     * gọi {@link #preloadSound} ngay lập tức (có thể gây trễ ở lần đầu tiên).
     *
     * @param soundName Tên tệp SFX (ví dụ: {@code SFX_BRICK_HIT}).
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
                if (audioBytes == null || format == null) return; // Vẫn thất bại
            } catch (LoadException e) {
                System.err.println("Không thể nạp âm thanh khi đang phát: " + e.getMessage());
                return;
            }
        }

        // Tạo AudioInputStream từ mảng byte[] trong bộ nhớ
        AudioInputStream ais = new AudioInputStream(
                new ByteArrayInputStream(audioBytes),
                format,
                audioBytes.length / format.getFrameSize());

        // Phát âm thanh nhanh
        try {
            Clip clip = AudioSystem.getClip();
            clip.open(ais);

            // Tính toán và đặt âm lượng SFX
            float effectiveSfxVolume = muted ? 0.0f : masterVolume * sfxVolume;
            setClipVolume(clip, effectiveSfxVolume);

            // Thêm listener để tự động đóng clip khi phát xong
            // Điều này rất quan trọng để tránh rò rỉ tài nguyên (resource leak)
            clip.addLineListener(event -> {
                if (event.getType() == LineEvent.Type.STOP) {
                    clip.close(); // Đóng clip để giải phóng tài nguyên
                }
            });

            clip.start(); // Phát một lần
        } catch (Exception e) {
            System.err.println("Lỗi khi phát âm thanh nhanh: " + e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                ais.close(); // Đóng AudioInputStream (an toàn)
            } catch (Exception ignored) {
            }
        }
    }

    // =====================
    // Volume helpers
    // =====================

    /**
     * Hàm nội bộ: Cập nhật âm lượng của {@code backgroundMusicClip} (nếu tồn tại)
     * dựa trên giá trị {@code masterVolume}, {@code musicVolume} và {@code muted} hiện tại.
     */
    private void updateBackgroundMusicVolume() {
        if (backgroundMusicClip != null) {
            float effectiveMusicVolume = muted ? 0.0f : masterVolume * musicVolume;
            setClipVolume(backgroundMusicClip, effectiveMusicVolume); // Cập nhật âm lượng nhạc nền
        }
    }

    /**
     * Hàm nội bộ: Đặt âm lượng cho một {@code Clip} cụ thể.
     * Sử dụng {@code FloatControl.Type.MASTER_GAIN}.
     * <p>
     * <b>Logic:</b> Chuyển đổi giá trị âm lượng tuyến tính (linear) (0.0f - 1.0f)
     * thành giá trị decibel (dB) (logarithmic) mà {@code FloatControl} yêu cầu,
     * sử dụng công thức: {@code dB = 20 * log10(volume)}.
     *
     * @param clip Clip cần đặt âm lượng.
     * @param volume Âm lượng tuyến tính (0.0f đến 1.0f).
     */
    private void setClipVolume(Clip clip, float volume) {
        if (volume < 0f) volume = 0f;
        if (volume > 1f) volume = 1f;

        // Chuyển đổi âm lượng tuyến tính (0.0 - 1.0) sang dB
        try {
            // Lấy điều khiển âm lượng MASTER_GAIN
            if (clip != null && clip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
                FloatControl gainControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
                float min = gainControl.getMinimum(); // Giá trị dB nhỏ nhất (thường là âm)
                float max = gainControl.getMaximum(); // Giá trị dB lớn nhất (thường là dương)
                float dB;
                if (volume == 0f) {
                    dB = min; // Tắt tiếng hoàn toàn
                } else {
                    // Ánh xạ tuyến tính 0..1 sang dải dB (logarithmic)
                    dB = (float) (20.0 * Math.log10(volume));
                    // Đảm bảo giá trị dB nằm trong phạm vi hỗ trợ của control
                    if (dB < min) dB = min;
                    if (dB > max) dB = max;
                }
                gainControl.setValue(dB); // Đặt âm lượng
            } else if (clip != null) {
                System.err.println("MASTER_GAIN control không được hỗ trợ.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Hàm tiện ích: Giới hạn một giá trị float trong khoảng [0.0f, 1.0f].
     * @param value Giá trị đầu vào.
     * @return Giá trị đã được giới hạn.
     */
    private float clamp(float value) {
        if (value < 0f) return 0f;
        if (value > 1f) return 1f;
        return value;
    }

    /**
     * Lớp Exception nội bộ cho các lỗi liên quan đến tải âm thanh.
     */
    private static class LoadException extends Exception {
        public LoadException(String message) {
            super(message);
        }
        public LoadException(String message, Throwable cause) {
            super(message, cause);
        }
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