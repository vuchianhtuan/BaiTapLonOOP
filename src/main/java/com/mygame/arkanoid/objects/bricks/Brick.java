package com.mygame.arkanoid.objects.bricks;

import com.mygame.arkanoid.effects.Shard;
import com.mygame.arkanoid.objects.GameObject;
import com.mygame.arkanoid.systems.ScalingManager;

import java.awt.Color;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.List;

/**
 * Lớp trừu tượng (abstract) cơ sở cho tất cả các loại gạch trong game.
 * <p>
 * Định nghĩa các hành vi và thuộc tính chung như máu (hitPoints),
 * khả năng nhận sát thương (takeHit), và bị phá hủy (isDestroyed).
 * Các lớp con (ví dụ: {@code NormalBrick}, {@code StrongBrick}) sẽ kế thừa từ lớp này.
 */
public abstract class Brick extends GameObject {

    // Thuộc tính chung cho tất cả các gạch
    protected int hitPoints;
    protected String type;
    protected String imageName;
    /** ID duy nhất cho mỗi gạch, dùng để lưu/tải trạng thái level. */
    private int id = -1;

    /**
     * Khởi tạo một đối tượng gạch cơ bản.
     *
     * @param x Vị trí X (tọa độ logic).
     * @param y Vị trí Y (tọa độ logic).
     * @param width Chiều rộng (tọa độ logic).
     * @param height Chiều cao (tọa độ logic).
     * @param hitPoints Lượng máu (số lần va chạm) gạch có thể chịu.
     * @param type Mã định danh loại gạch (ví dụ: "normal").
     * @param imageName Key của hình ảnh (asset key) dùng để vẽ gạch.
     */
    public Brick(int x, int y, int width, int height, int hitPoints, String type, String imageName) {
        super(x, y, width, height);
        this.hitPoints = hitPoints;
        this.type = type;
        this.imageName = imageName;
    }

    /**
     * Giảm điểm máu (hitPoints) của gạch đi 1,
     * chỉ khi gạch còn máu ({@code hitPoints > 0}).
     */
    public void takeHit() {
        if (this.hitPoints > 0) {
            this.hitPoints--;
        }
    }

    /**
     * Kiểm tra xem gạch đã bị phá hủy hay chưa (hết máu).
     * @return true nếu {@code hitPoints <= 0}, false nếu còn.
     */
    public boolean isDestroyed() {
        return this.hitPoints <= 0;
    }

    /**
     * Tạo và trả về một danh sách các mảnh vỡ ({@link Shard}) khi gạch
     * bị **phá hủy hoàn toàn**.
     * <p>
     * Thường tạo ra một vụ nổ lớn (ví dụ: 20 mảnh).
     *
     * @return Một {@code List<Shard>} chứa các mảnh vỡ mới.
     */
    public List<Shard> shatter() {
        List<Shard> shards = new ArrayList<>();
        final int NUM_SHARDS = 20; // Số lượng mảnh vỡ lớn khi bị phá hủy

        // Vị trí tâm của gạch
        // Lấy một điểm X ngẫu nhiên trên chiều rộng gạch để tạo hiệu ứng tự nhiên hơn
        int randomXOffset = (int) (Math.random() * getWidth());
        int centerX = getX() + randomXOffset;
        int centerY = getY() + getHeight() / 2;

        Color shardColor = Color.ORANGE; // Màu mặc định, có thể được override ở lớp con

        for (int i = 0; i < NUM_SHARDS; i++) {
            // Tạo mảnh vỡ tại vị trí đã tính
            Shard s = new Shard(centerX, centerY, shardColor);
            shards.add(s);
        }
        return shards;
    }

    /**
     * Tạo hiệu ứng mảnh vỡ nhỏ khi gạch bị **đánh trúng** (nhưng chưa bị phá hủy).
     * <p>
     * Thường tạo ra ít mảnh vỡ (ví dụ: 3-5 mảnh) để thể hiện va chạm.
     *
     * @param targetList Danh sách (thường là của EntityManager) để thêm các mảnh vỡ mới vào.
     */
    public void shatterHit(List<Shard> targetList) {
        final int NUM_SHARDS = 4; // Số lượng mảnh vỡ ít hơn

        // Vị trí X ngẫu nhiên trên chiều rộng của gạch
        int randomXOffset = (int) (Math.random() * getWidth());
        int startX = getX() + randomXOffset;
        int centerY = getY() + getHeight() / 2;

        Color shardColor = Color.ORANGE; // Màu mặc định

        for (int i = 0; i < NUM_SHARDS; i++) {
            Shard s = new Shard(startX, centerY, shardColor);
            targetList.add(s); // Thêm trực tiếp vào danh sách được cung cấp
        }
    }

    public int getHitPoints() {
        return hitPoints;
    }

    public String getType() {
        return type;
    }

    @Override
    public abstract void update();

    @Override
    public abstract void render(Graphics g, ScalingManager sm);

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
}