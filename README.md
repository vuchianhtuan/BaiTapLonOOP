Mô tả ngắn
---------
Dự án là một game kiểu Arkanoid viết bằng Java theo hướng lập trình hướng đối tượng (OOP). Mã nguồn chính nằm dưới `src/main/java/com/mygame/arkanoid/`. Project được cấu trúc thành nhiều package (core, engine, objects, systems, ui, effects, level, save, util) theo kiến trúc mô-đun/manager-based.

Tổng quan kỹ thuật
------------------
- Ngôn ngữ: Java (maven config chỉ định Java 21).
- Build: Maven (pom.xml), có cấu hình để tạo runnable "fat" JAR bằng Maven Shade Plugin (Manifest mainClass được cấu hình).
- Thư viện test: JUnit 5, Mockito (scope=test).
- UI / đồ họa: Swing (import javax.swing.* trong `Main.java`). Các phần vẽ bên trong `GamePanel`.
- Kiến trúc game:
    - GameLoop riêng để cập nhật và render.
    - GameManager (singleton) quản lý trạng thái trò chơi.
    - ScalingManager quản lý tỉ lệ khi cửa sổ thay đổi kích thước.
    - SaveSystem + SaveData xử lý lưu/khôi phục trạng thái (tự động lưu khi đóng cửa sổ nếu có thể tiếp tục).
    - Packages rõ ràng tách các concern: core, engine, objects, systems, ui, effects, level, save, util.
- Packaging / chạy: pom.xml có Maven Shade plugin để đóng gói thành JAR thực thi (main class: com.mygame.arkanoid.Main).

Tính năng chính
----------------------------------------------------
- Game loop tách biệt (update/render) để đảm bảo logic game độc lập với UI.
- Giao diện chạy trong JFrame (Swing) với một GamePanel chứa phần render.
- Responsive scaling: sự kiện resize của cửa sổ kích hoạt ScalingManager.update(...) để thích ứng tỉ lệ hiển thị.
- Lưu / tiếp tục (Save & Continue): SaveSystem.load() và SaveSystem.save(...) + restoreFromSave(...) trong GameManager — hỗ trợ tải trạng thái khi khởi động và tự động lưu khi đóng cửa sổ.
- Cấu trúc multi-package: support cho levels, effects (particle/visual effects), systems (collision, input, physics, ...), objects (ball, paddle, bricks, ...), save.
- Có bộ test (JUnit) cấu hình trong pom.xml (nếu có tests trong thư mục test).
- Tạo runnable JAR bằng Maven (maven-shade-plugin), giúp xuất bản và chạy dễ dàng.

Cấu trúc thư mục (tóm tắt)
--------------------------
- src/main/java/com/mygame/arkanoid/Main.java — entry point (khởi tạo JFrame, GamePanel, GameLoop, listeners).
- src/main/java/com/mygame/arkanoid/core/ — core game classes (GameManager,...).
- src/main/java/com/mygame/arkanoid/engine/ — engine-related classes (GameLoop, update systems,...).
- src/main/java/com/mygame/arkanoid/objects/ — các đối tượng game (ball, paddle, brick,...).
- src/main/java/com/mygame/arkanoid/systems/ — các hệ thống xử lý (collision, input, scoring,...).
- src/main/java/com/mygame/arkanoid/ui/ — giao diện, menu, HUD, ...
- src/main/java/com/mygame/arkanoid/effects/ — hiệu ứng (particle, explosion,...).
- src/main/java/com/mygame/arkanoid/level/ — quản lý level, tải level,...
- src/main/java/com/mygame/arkanoid/save/ — SaveSystem, SaveData,...
- pom.xml — Maven config (Java 21, junit, mockito, maven-shade-plugin).

Hướng dẫn build & chạy
-----------------------
Yêu cầu:
- JDK 21 (pom.xml cấu hình source/target = 21)
- Maven

Build:
1. Từ thư mục gốc:
   mvn clean package

2. Sau khi build thành công, Maven Shade tạo một runnable JAR trong `target/`. Ví dụ:
   java -jar target/arkanoid-1.0-SNAPSHOT.jar

(Manifest đã thiết lập main class là `com.mygame.arkanoid.Main` thông qua pom.xml.)

Ghi chú: Nếu bạn muốn chạy từ IDE (IntelliJ/Eclipse), mở project bằng Maven và chạy class `com.mygame.arkanoid.Main`.

---------------------------------------
- Màn hình menu:
  <img width="1392" height="898" alt="image" src="https://github.com/user-attachments/assets/cc28a85c-4146-4fbc-8682-8ccea42728de" />

- Gameplay:
  <img width="1393" height="898" alt="Screenshot 2025-11-04 230052" src="https://github.com/user-attachments/assets/01582e99-09b4-4a59-bd61-21fc5dedaf22" />

Lưu ý:
Gợi ý mô tả tính năng
---------------------------------------------------
- Save/continue: khi đóng cửa sổ, game tự lưu nếu có thể tiếp tục; khi khởi động, sẽ load save nếu tồn tại.
- Responsive UI: ScalingManager cho phép thay đổi kích thước cửa sổ mà vẫn giữ tỉ lệ hiển thị.
- Engine modular: dễ thêm hệ thống mới (effects, power-ups, AI).
- Levels & progression: load nhiều level từ package level.
- Particles/effects: visual feedback khi phá gạch / mất bóng.
- Highscore / save slots: có thể mở rộng SaveSystem để lưu điểm/tiến trình.

Tóm lược các thư viện & API thực tế
-------------------------------------------
- Java SE: Swing (javax.swing.*) + Java2D (Graphics/Graphics2D).
- Maven: quản lý build/dependencies; Maven Shade plugin tạo runnable JAR.
- Test: JUnit 5, Mockito (scope=test).

Liên hệ
-------
Tác giả repo: @vuchianhtuan