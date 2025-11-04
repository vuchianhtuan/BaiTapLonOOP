package com.mygame.arkanoid.objects.bricks;

import com.mygame.arkanoid.effects.Shard;
import com.mygame.arkanoid.objects.GameObject;
import com.mygame.arkanoid.systems.ScalingManager;

// KHẮC PHỤC LỖI IMPORT VÀ GENERICS
import java.awt.Color; // Cần import Color
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.List;

/**
 * Lớp trừu tượng đại diện cho một viên gạch trong trò chơi Arkanoid.
 */
public abstract class Brick extends GameObject {

    // Thuộc tính chung cho tất cả các gạch
    protected int hitPoints;
    protected String type;
    protected String imageName;
    private int id = -1; // ID duy nhất cho mỗi gạch

    public Brick(int x, int y, int width, int height, int hitPoints, String type, String imageName) {
        super(x, y, width, height);
        this.hitPoints = hitPoints;
        this.type = type;
        this.imageName = imageName;
    }

    /**
     * Phương thức giảm điểm máu của gạch khi bị đánh trúng.
     */
    public void takeHit() {
        if (this.hitPoints > 0) {
            this.hitPoints--;
        }
    }

    /**
     * Kiểm tra xem gạch đã bị phá hủy hay chưa.
     * @return true nếu gạch đã bị phá hủy, false nếu chưa.
     */
    public boolean isDestroyed() {
        return this.hitPoints <= 0;
    }

    /**
     * Phương thức tạo mảnh vụn khi gạch bị phá hủy.
     * @return Danh sách các mảnh vụn được tạo ra.
     */
    public List<Shard> shatter() {
        List<Shard> shards = new ArrayList<>();
        final int NUM_SHARDS = 20;

        // Vị trí tâm của gạch
        int randomXOffset = (int) (Math.random() * getWidth());
        int centerX = getX() + randomXOffset;

        int centerY = getY() + getHeight() / 2;

        Color shardColor = Color.ORANGE;

        for (int i = 0; i < NUM_SHARDS; i++) {
            // Truyền màu xám cố định vào Shard constructor
            Shard s = new Shard(centerX, centerY, shardColor);
            shards.add(s);
        }
        return shards;
    }

    /**
     * Phương thức tạo mảnh vụn khi gạch bị đánh trúng.
     */
    public void shatterHit(List<Shard> targetList) {
        final int NUM_SHARDS = 4; // Số lượng mảnh vụn ít hơn (3-5 hạt)

        // Vị trí X ngẫu nhiên trên chiều rộng của gạch
        int randomXOffset = (int) (Math.random() * getWidth());
        int startX = getX() + randomXOffset;

        int centerY = getY() + getHeight() / 2;

        Color shardColor = Color.ORANGE;

        for (int i = 0; i < NUM_SHARDS; i++) {
            Shard s = new Shard(startX, centerY, shardColor);
            targetList.add(s);
        }
    }

    /**
     * Lấy điểm máu hiện tại của gạch.
     * @return
     */
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