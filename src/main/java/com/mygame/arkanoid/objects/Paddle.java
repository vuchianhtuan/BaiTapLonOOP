package com.mygame.arkanoid.objects;
import com.mygame.arkanoid.engine.AssetManager;
import com.mygame.arkanoid.objects.powerups.PowerUp;
import com.mygame.arkanoid.engine.InputHandler;
import java.awt.Graphics;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.awt.event.KeyEvent;
import com.mygame.arkanoid.systems.ScalingManager;
import com.mygame.arkanoid.config.GameConstants;

/**
 * Lớp Paddle đại diện cho thanh đỡ (thanh điều khiển) do người chơi điều khiển.
 * <p>
 * Quản lý logic di chuyển ngang (trái/phải), xử lý va chạm với biên
 * (clamping), và các thay đổi trạng thái từ PowerUp
 * (ví dụ: {@link #expand(int)}, {@link #setSticky(boolean)}).
 */
public class Paddle extends MovableObject {
    private int speed = GameConstants.PADDLE_SPEED;
    /** (Dường như không được sử dụng) Tham chiếu đến power-up hiện tại. */
    private PowerUp currentPowerUp;
    /** Key (mã) của skin paddle (ví dụ: "skin_paddle_1"). */
    private String imageName;
    /** Chiều rộng gốc, dùng để reset sau khi hiệu ứng "expand" kết thúc. */
    private final int originalWidth;
    /** Cờ trạng thái: true nếu paddle đang có hiệu ứng "dính" (Sticky). */
    private boolean isSticky = false;

    /**
     * Kiểm tra xem paddle có đang ở trạng thái 'dính' (sticky) hay không.
     * @return true nếu paddle có tính năng dính bóng, false nếu không.
     */
    public boolean isSticky() {
        return isSticky;
    }

    /**
     * Đặt trạng thái 'dính' (sticky) cho paddle.
     * (Thường được gọi bởi {@link StickyPaddlePowerUp}).
     * @param sticky true để bật tính năng dính bóng, false để tắt.
     */
    public void setSticky(boolean sticky) {
        this.isSticky = sticky;
    }

    /**
     * Di chuyển paddle sang trái một khoảng bằng {@code speed}.
     * Đảm bảo paddle không di chuyển ra ngoài biên trái (x = 0).
     */
    public void moveLeft() {
        x -= speed;
        if (x < 0) x = 0; // Kẹp (clamp) vị trí
    }

    /**
     * Di chuyển paddle sang phải một khoảng bằng {@code speed}.
     * Đảm bảo paddle không di chuyển ra ngoài biên phải
     * (giới hạn bởi {@code GAME_AREA_WIDTH}).
     */
    public void moveRight() {
        x += speed;
        int gameAreaWidth = ScalingManager.getInstance().GAME_AREA_WIDTH;
        if (x + width > gameAreaWidth) x = gameAreaWidth - width; // Kẹp (clamp) vị trí
    }

    /**
     * Khởi tạo một Paddle mới với skin cụ thể.
     * Lưu lại chiều rộng ban đầu ({@code originalWidth}) để dùng cho việc
     * {@link #resetWidth()} sau khi hiệu ứng PowerUp kết thúc.
     *
     * @param x Vị trí X (logic) ban đầu.
     * @param y Vị trí Y (logic) ban đầu.
     * @param width Chiều rộng (logic) ban đầu.
     * @param height Chiều cao (logic) ban đầu.
     * @param skinKey Key (mã) của skin (ví dụ: "skin_paddle_1").
     */
    public Paddle(int x, int y, int width, int height, String skinKey) {
        super(x, y, width, height);
        this.imageName = skinKey;
        this.originalWidth = width; // Lưu lại chiều rộng gốc
    }

    /**
     * Mở rộng chiều rộng của paddle.
     * <p>
     * Dịch chuyển vị trí X sang trái một nửa ({@code amount / 2})
     * đồng thời tăng chiều rộng, tạo ra hiệu ứng "mở rộng từ tâm".
     * (Thường được gọi bởi {@link ExpandPaddlePowerUp}).
     *
     * @param amount Lượng pixel (logic) cần mở rộng thêm.
     */
    public void expand(int amount) {
        // Tăng chiều rộng và điều chỉnh lại vị trí x để nó mở rộng đều 2 bên
        this.x -= amount / 2;
        this.width += amount;
    }

    /**
     * Thu hẹp (reset) chiều rộng của paddle về giá trị {@code originalWidth} ban đầu.
     * <p>
     * Dịch chuyển vị trí X trở lại (sang phải) để căn giữa
     * sau khi thu hẹp, đảo ngược logic của {@link #expand(int)}.
     */
    public void resetWidth() {
        // Điều chỉnh lại vị trí x trước khi thu hẹp
        this.x += (this.width - this.originalWidth) / 2;
        this.width = this.originalWidth;
    }

    /**
     * Cập nhật logic di chuyển của paddle dựa trên đầu vào (input) của người chơi.
     * <p>
     * Phương thức này được gọi bởi {@code GameManager} hoặc {@code EntityManager}
     * mỗi frame. Hỗ trợ cả phím mũi tên (Arrow Keys) và phím A/D.
     *
     * @param inputHandler Trình xử lý đầu vào (để kiểm tra phím).
     */
    public void update(InputHandler inputHandler) {
        boolean left = inputHandler.isKeyDown(KeyEvent.VK_LEFT) ||
                inputHandler.isKeyDown(KeyEvent.VK_A);
        boolean right = inputHandler.isKeyDown(KeyEvent.VK_RIGHT) ||
                inputHandler.isKeyDown(KeyEvent.VK_D);
        if (left) {
            moveLeft();
        } else if (right) {
            moveRight();
        }
    }

    /** Ghi đè (override) phương thức rỗng. Logic di chuyển nằm trong {@link #update(InputHandler)}. */
    @Override public void move() {}
    /** Ghi đè (override) phương thức rỗng. Logic cập nhật nằm trong {@link #update(InputHandler)}. */
    @Override public void update() {}

    /**
     * Vẽ (render) paddle (skin) lên màn hình.
     * <p>
     * Vẽ tại vị trí {@code (x, y)} và kích thước {@code (width, height)} hiện tại
     * (lưu ý: {@code width} có thể đã bị thay đổi bởi PowerUp).
     *
     * @param g Đối tượng Graphics để vẽ.
     * @param sm Trình quản lý co giãn (ScalingManager).
     */
    @Override public void render(Graphics g, ScalingManager sm) {

        BufferedImage img = AssetManager.getInstance().getImage(this.imageName);
        if (img != null) {
            // Vẽ ảnh skin
            g.drawImage(img,
                    sm.scaleX(this.x), sm.scaleY(this.y),
                    sm.scaleWidth(this.width), sm.scaleHeight(this.height), null);
        } else {
            // Fallback: Vẽ hình chữ nhật màu xanh nếu ảnh bị lỗi
            g.setColor(Color.BLUE);
            g.fillRect(sm.scaleX(this.x), sm.scaleY(this.y),
                    sm.scaleWidth(this.width), sm.scaleHeight(this.height));
        }
    }

    public String getImageName() {
        return imageName;
    }

    public void setImageName(String imageName) {
        this.imageName = imageName;
    }
}