# PWW AutoTest

Ứng dụng Spring Boot điều phối Selenium + TestNG cho **Trang chủ và chức năng 03–15** của PLT WordWise. Project dùng Java 17, OOP, Page Object Model và Maven multi-module.

## Cấu trúc

- `autotest-core`: cấu hình, WebDriver, base classes, model kết quả và screenshot.
- `autotest-automation`: Page Object, Scenario, registry, test data và TestNG suite của 14 module.
- `autotest-app`: Spring Boot MVC, REST API, runner nền, tính điểm, giao diện Thymeleaf và báo cáo.

## Import vào Eclipse

1. Chọn **File → Import → Maven → Existing Maven Projects**.
2. Chọn thư mục `pww-autotest` chứa file `pom.xml` gốc.
3. Đợi Eclipse tải dependency và nhận đủ ba module.
4. Chạy `vn.team.autotest.app.AutoTestApplication` trong module `autotest-app` bằng **Run As → Java Application** hoặc **Spring Boot App**.
5. Mở `http://localhost:8081`.

## Cấu hình chạy Selenium

| Biến môi trường | Mặc định | Ý nghĩa |
|---|---|---|
| `PWW_BASE_URL` | `http://localhost:8000` | Website PWW cần kiểm thử |
| `PWW_BROWSER` | `chrome` | `chrome`, `firefox` hoặc `edge` |
| `PWW_HEADLESS` | `true` | Chạy ẩn trình duyệt |
| `PWW_TIMEOUT_SECONDS` | `15` | Timeout Selenium |
| `AUTOTEST_PORT` | `8081` | Cổng giao diện Spring Boot |

Không đưa tài khoản/mật khẩu vào Git. Hãy truyền qua biến môi trường hoặc cấu hình cục bộ không commit.

## API

```http
GET  /api/modules
POST /api/runs          {"moduleId":"dashboard"}
POST /api/runs          {"moduleId":"all"}
GET  /api/runs/{runId}
GET  /api/runs
```

## Quy tắc mở rộng chức năng

1. Tạo package riêng `vn.team.autotest.modules.<module>`.
2. Thêm `<Module>Page` vào package `page`, chỉ chứa locator và thao tác.
3. Thêm `<Module>Scenario` vào package `scenario`, chứa TestNG assertion.
4. Đăng ký Scenario trong `ScenarioRegistry` và metadata/điểm trong `TestCatalog`.
5. Đặt dữ liệu tại `src/main/resources/testdata/<module>` và suite tại `src/main/resources/suites`.

Mỗi test phải tự chuẩn bị/dọn dữ liệu, không phụ thuộc thứ tự chạy và không gọi test của module khác để tạo điều kiện.
