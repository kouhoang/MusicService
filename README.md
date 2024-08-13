# MusicService App

## Giới thiệu

**MusicService App** là một ứng dụng phát nhạc cơ bản sử dụng **Android** và **Kotlin**. Ứng dụng được xây dựng theo kiến trúc **MVVM** và hỗ trợ phát nhạc nền với một dịch vụ foreground, bao gồm các tính năng như phát/tạm dừng, chuyển bài hát trước/sau và thông báo tùy chỉnh với các nút điều khiển.

## Tính năng

- Phát nhạc nền với dịch vụ Foreground.
- Tùy chỉnh thông báo với các nút điều khiển: Phát/Tạm dừng, Bài trước, Bài sau.
- Điều khiển nhạc thông qua notification.
- Hỗ trợ các hành động Play, Pause, Next, và Previous từ thông báo.
- Quản lý danh sách bài hát và hiển thị tên bài hát trong thông báo.

## Công nghệ sử dụng

- **Ngôn ngữ:** Kotlin
- **Framework:** Android SDK
- **MediaPlayer:** Phát nhạc và quản lý các hành động liên quan.
- **NotificationCompat:** Tạo thông báo với các nút điều khiển tùy chỉnh.
- **MVVM Architecture:** Mô hình MVVM để tổ chức code rõ ràng và dễ bảo trì.

## Cài đặt

1. **Clone repo:**

    ```sh
    git clone https://github.com/kouhoang/MusicService.git
    ```

2. **Mở project trong Android Studio:**

    - Chọn **File** -> **Open**.
    - Dẫn đến thư mục vừa clone và chọn **Open**.

3. **Build project:**
    - Đảm bảo rằng các dependencies cần thiết được cài đặt.
    - Build project bằng cách chọn **Build** -> **Make Project**.

4. **Run project:**
    - Chọn một thiết bị ảo hoặc kết nối thiết bị thật để chạy ứng dụng.
    - Nhấn nút **Run** để cài đặt và chạy ứng dụng trên thiết bị.

## Sử dụng

- Khi ứng dụng được khởi chạy, người dùng có thể:
  - Chọn một bài hát từ danh sách.
  - Sử dụng các nút điều khiển để phát/tạm dừng nhạc hoặc chuyển bài.
  - Kiểm tra thông báo để điều khiển nhạc mà không cần mở ứng dụng.

## Hướng dẫn thêm

- **Thêm bài hát mới:**
  - Thêm các file nhạc vào thư mục `res/raw/`.
  - Cập nhật danh sách bài hát trong TracksFragments để thêm bài mới vào ứng dụng.
