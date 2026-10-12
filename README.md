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
| `PWW_BASE_URL` | `https://learnenglish.plt.pro.vn` | Website PWW cần kiểm thử |
| `PWW_BROWSER` | `chrome` | `chrome`, `firefox` hoặc `edge` |
| `PWW_HEADLESS` | `true` | Chạy ẩn trình duyệt |
| `PWW_TIMEOUT_SECONDS` | `15` | Timeout Selenium |
| `AUTOTEST_PORT` | `8081` | Cổng giao diện Spring Boot |

Không đưa tài khoản/mật khẩu vào Git. Hãy truyền qua biến môi trường hoặc cấu hình cục bộ không commit.

## Chạy Vocabulary Management (KB-11-001..005)

Tài khoản luôn truyền qua `PWW_ADMIN_USERNAME`, `PWW_ADMIN_PASSWORD`; KB-11-004 cần thêm
`PWW_USER_USERNAME`, `PWW_USER_PASSWORD`. Các xung đột yêu cầu không có giá trị mặc định:

- `PWW_VOCAB_MAX_LENGTH`: PO chốt `50` theo DOM hay `100` theo SRS.
- `PWW_VOCAB_IMAGE_LIMIT_BYTES`: PO chốt `5000000` (MB) hay `5242880` (MiB).
- `PWW_VOCAB_DIFFICULTY`: PO chốt cách ánh xạ SRS `Dễ/Trung bình/Khó` sang UI `Low/Medium/High`.
- `PWW_VOCAB_EXPECTED_XLSX`: file approved có sheet `Test_Data` và các ID
  `TD-MENU-ADMIN-APPROVED`, `TD-11-VOCAB-COLUMNS-APPROVED`, `TD-FIXTURE-VOCAB-01`.
- KB-11-004 chỉ chạy khi có `PWW_VOCAB_ALLOCATION_CONFIRMED=true` và
  `PWW_VOCAB_XMOD_TOPIC` là chủ đề fixture riêng, bảo đảm chỉ chứa từ của lượt chạy.
- Mẫu dữ liệu để PO/QA chốt cho KB-11-004 nằm tại
  `autotest-automation/src/test/resources/testdata/KB-11-004_Test_Data.xlsx`; cột `Value`
  ghi rõ biến môi trường tương ứng và các prerequisite chưa chốt được đánh dấu `BLOCKED`.

Chạy cả năm kịch bản:

```bash
mvn -pl autotest-automation -am install -DskipTests
mvn -pl autotest-automation test -Dsurefire.suiteXmlFiles=src/main/resources/suites/vocabulary.xml
```

Chạy riêng từng scenario (DataProvider vẫn tách từng invocation):

```bash
mvn -pl autotest-automation -am test -Dsurefire.failIfNoSpecifiedTests=false -Dtest=VocabularyScenarioTest#kb11001CreateValidVocabularyWithMockedAutomaticIpa
mvn -pl autotest-automation -am test -Dsurefire.failIfNoSpecifiedTests=false -Dtest=VocabularyScenarioTest#kb11002ValidateVocabularyInput
mvn -pl autotest-automation -am test -Dsurefire.failIfNoSpecifiedTests=false -Dtest=VocabularyScenarioTest#kb11003ValidateVocabularyImage
mvn -pl autotest-automation -am test -Dsurefire.failIfNoSpecifiedTests=false -Dtest=VocabularyScenarioTest#kb11004VerifyVocabularyAcrossLearningMethods
mvn -pl autotest-automation -am test -Dsurefire.failIfNoSpecifiedTests=false -Dtest=VocabularyScenarioTest#kb11005CompareMenusTablesAndCheckboxes
```

Khi fail/skip, evidence nằm trong thư mục `evidence` của working directory chạy TestNG; tên và log có `scenarioId`,
`datasetId`, URL và nguyên nhân. KB-11-001 hiện là mocked integration cho IPA; không được dùng
kết quả này để tuyên bố live Dictionary API đã Pass.

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
