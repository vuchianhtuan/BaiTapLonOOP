/**
 * Chứa các lớp quản lý các màn hình (screens) giao diện người dùng chính của game.
 * <p>
 * Gói này chịu trách nhiệm cho các trạng thái giao diện toàn màn hình,
 * khác biệt với HUD (hiển thị trong lúc chơi). Các lớp này quản lý
 * logic và việc vẽ (render) cho các menu và màn hình chức năng:
 * <ul>
 * <li>{@link com.mygame.arkanoid.ui.screens.MenuManager}: Quản lý menu chính.</li>
 * <li>{@link com.mygame.arkanoid.ui.screens.ScoreManager}: Hiển thị màn hình điểm cao (high scores).</li>
 * <li>{@link com.mygame.arkanoid.ui.screens.SelectLevel}: Hiển thị màn hình chọn màn chơi.</li>
 * <li>{@link com.mygame.arkanoid.ui.screens.SettingManager}: Quản lý màn hình cài đặt (options/settings).</li>
 * </ul>
 * </p>
 */
package com.mygame.arkanoid.ui.screens;