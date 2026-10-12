package vn.team.autotest.modules.vocabulary.scenario;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import org.openqa.selenium.WebDriver;
import org.testng.Assert;
import org.testng.SkipException;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;
import vn.team.autotest.automation.auth.LoginPage;
import vn.team.autotest.core.base.BaseScenario;
import vn.team.autotest.core.driver.DriverFactory;
import vn.team.autotest.modules.vocabulary.config.VocabularyTestConfig;
import vn.team.autotest.modules.vocabulary.data.VocabularyExcelDataset;
import vn.team.autotest.modules.vocabulary.model.ImageValidationCase;
import vn.team.autotest.modules.vocabulary.model.ValidationCase;
import vn.team.autotest.modules.vocabulary.model.VocabularyRecord;
import vn.team.autotest.modules.vocabulary.model.VocabularyRow;
import vn.team.autotest.modules.vocabulary.page.LearningVocabularyPage;
import vn.team.autotest.modules.vocabulary.page.VocabularyPage;
import vn.team.autotest.modules.vocabulary.util.ImageFixtureFactory;
import vn.team.autotest.modules.vocabulary.util.TableComparator;

public class VocabularyScenario extends BaseScenario {
    private static final long IPA_SLA_MILLIS = 3_000;
    private static final String DEFAULT_TOPIC = "Science";
    private final Set<String> cleanupWords = new LinkedHashSet<>();

    @Test(description = "KB-11-001 - Mocked integration: tạo từ hợp lệ và tự động điền IPA")
    public void kb11001CreateValidVocabularyWithMockedAutomaticIpa() {
        evidenceContext("KB-11-001", "CREATE_VALID_IPA_MOCK");
        VocabularyPage page = loginAdmin();
        String word = uniqueLetters("qavocab");
        Path image = ImageFixtureFactory.jpeg(Path.of("target", "vocabulary-fixtures"), word + ".jpg", 32_768);
        VocabularyRecord input = validRecord(word, "/test/", DEFAULT_TOPIC, image);
        resultDetails("Tạo vocabulary hợp lệ với IPA tự động (mocked integration)",
                "word=" + word + ", topic=" + input.topic() + ", difficulty=" + input.difficulty()
                        + ", image=" + image.getFileName(),
                "IPA tự điền trong tối đa 3000ms; lưu đúng toàn bộ trường và chỉ tạo một bản ghi.");
        cleanupWords.add(word);

        page.openCreateForm();
        requireFormOptions(page, input);
        page.installAutomaticIpaMock(word, input.ipa(), input.definition());
        long lookupMillis = page.enterWordAndWaitForAutomaticIpa(word);
        Assert.assertEquals(page.ipa(), input.ipa(), "IPA phải do luồng tự động điền, không nhập tay");
        Assert.assertTrue(lookupMillis <= IPA_SLA_MILLIS, "Tra IPA vượt SLA 3 giây: " + lookupMillis + "ms");
        page.fillRemaining(input);
        Assert.assertEquals(page.uploadedFileName(), image.getFileName().toString());
        Assert.assertTrue(page.previewVisible(), "Thiếu preview ảnh hợp lệ");
        page.submit();

        VocabularyRow actual = page.findExact(word).orElseThrow(() -> new AssertionError("Không tìm thấy bản ghi đã lưu"));
        actualResult("IPA=" + input.ipa() + ", lookup=" + lookupMillis + "ms; bản ghi=" + actual
                + "; count=" + page.countExact(word));
        assertPersisted(input, actual);
        Assert.assertEquals(page.countExact(word), 1L, "Phải tạo đúng một bản ghi");
    }

    @DataProvider(name = "vocabularyValidation", parallel = false)
    public Object[][] vocabularyValidation() {
        VocabularyTestConfig settings = VocabularyTestConfig.fromEnvironment();
        Integer max = settings.approvedMaxWordLength();
        return new Object[][] {
            {new ValidationCase("EMPTY_WORD", "", false, false)},
            {new ValidationCase("ONE_CHARACTER", "${ONE_CHAR}", true, false)},
            {new ValidationCase("MAX_LENGTH", max == null ? "${UNRESOLVED_MAX}" : "q".repeat(max - 1) + "z", true, false)},
            {new ValidationCase("MAX_PLUS_ONE", max == null ? "${UNRESOLVED_MAX_PLUS_ONE}" : "a".repeat(max + 1), false, false)},
            {new ValidationCase("INVALID_CHARACTERS", "word@qa", false, false)},
            {new ValidationCase("DUPLICATE_WORD", "${DUPLICATE}", false, true)}
        };
    }

    @Test(dataProvider = "vocabularyValidation", description = "KB-11-002 - Validation từ vựng theo DataProvider")
    public void kb11002ValidateVocabularyInput(ValidationCase data) {
        evidenceContext("KB-11-002", data.datasetId());
        resultDetails(validationCaseName(data.datasetId()), "word=" + printable(data.word()),
                validationExpected(data));
        VocabularyTestConfig settings = VocabularyTestConfig.fromEnvironment();
        if (data.word().startsWith("${UNRESOLVED")) {
            throw new SkipException("Blocked: chưa chốt PWW_VOCAB_MAX_LENGTH; SRS=100 nhưng DOM maxlength=50");
        }
        VocabularyPage page = loginAdmin();
        String word = switch (data.word()) {
            case "${ONE_CHAR}" -> unusedOneCharacter(page);
            case "${DUPLICATE}" -> uniqueLetters("qaduplicate");
            default -> data.word();
        };

        if (data.duplicateFixture()) {
            createManual(page, validRecord(word, "/test/", DEFAULT_TOPIC, null));
            cleanupWords.add(word);
            Assert.assertEquals(page.countExact(word), 1L, "Fixture trùng phải được tạo cô lập");
        }

        page.openCreateForm();
        int domMaxLength = page.wordMaxLength();
        if (settings.approvedMaxWordLength() != null) {
            Assert.assertEquals(domMaxLength, settings.approvedMaxWordLength().intValue(),
                    "DOM không khớp giới hạn đã phê duyệt");
        }
        VocabularyRecord record = validRecord(word, "/test/", DEFAULT_TOPIC, null);
        requireFormOptions(page, record);
        page.fill(record, true);
        boolean preventedOrTruncated = page.enteredWord().length() < word.length();
        if (!data.expectedValid() && preventedOrTruncated) {
            actualResult("Trình duyệt đã chặn/truncate: độ dài nhập=" + word.length()
                    + ", độ dài giữ lại=" + page.enteredWord().length() + "; record count=0");
            Assert.assertEquals(page.countExact(word), 0L, "Giá trị gốc bị chặn/truncate không được tạo");
            return;
        }

        if (data.expectedValid()) cleanupWords.add(word);
        page.submit();
        List<String> messages = page.validationMessages();
        long count = page.countExact(word);
        actualResult("enteredWord=" + printable(page.enteredWord()) + ", validation=" + messages
                + ", exactRecordCount=" + count);
        if (data.expectedValid()) {
            Assert.assertEquals(count, 1L, "Dữ liệu hợp lệ phải tạo đúng một bản ghi");
        } else {
            Assert.assertEquals(count, data.duplicateFixture() ? 1L : 0L, "Dữ liệu lỗi không được tạo thêm");
            Assert.assertFalse(messages.isEmpty(), "Thiếu phản hồi validation quan sát được");
        }
    }

    @DataProvider(name = "imageValidation", parallel = false)
    public Object[][] imageValidation() {
        Long limit = VocabularyTestConfig.fromEnvironment().approvedImageLimitBytes();
        long boundary = limit == null ? -1 : limit;
        return new Object[][] {
            {new ImageValidationCase("VALID_EXACT_LIMIT", ImageValidationCase.FileKind.JPEG, boundary, true)},
            {new ImageValidationCase("IMAGE_LIMIT_PLUS_ONE", ImageValidationCase.FileKind.JPEG, boundary < 0 ? -1 : boundary + 1, false)},
            {new ImageValidationCase("PDF_UNSUPPORTED", ImageValidationCase.FileKind.PDF, 102_400, false)}
        };
    }

    @Test(dataProvider = "imageValidation", description = "KB-11-003 - Validation định dạng và biên dung lượng ảnh")
    public void kb11003ValidateVocabularyImage(ImageValidationCase data) {
        evidenceContext("KB-11-003", data.datasetId());
        resultDetails(imageCaseName(data.datasetId()),
                "kind=" + data.kind() + ", sizeBytes=" + data.sizeBytes(), imageExpected(data));
        if (data.sizeBytes() < 0) {
            throw new SkipException("Blocked: chưa chốt 5MB là MB thập phân hay MiB; cần PWW_VOCAB_IMAGE_LIMIT_BYTES");
        }
        VocabularyPage page = loginAdmin();
        String word = uniqueLetters("qaimage");
        Path folder = Path.of("target", "vocabulary-fixtures");
        Path file = data.kind() == ImageValidationCase.FileKind.JPEG
                ? ImageFixtureFactory.jpeg(folder, data.datasetId().toLowerCase() + ".jpg", data.sizeBytes())
                : ImageFixtureFactory.pdf(folder, "qa_not_image.pdf", data.sizeBytes());
        VocabularyRecord record = validRecord(word, "/test/", DEFAULT_TOPIC, file);
        if (data.expectedValid()) cleanupWords.add(word);

        page.openCreateForm();
        requireFormOptions(page, record);
        page.fill(record, true);
        String observedFileName = page.uploadedFileName();
        boolean observedPreview = page.previewVisible();
        Assert.assertEquals(observedFileName, file.getFileName().toString(), "Sai tên tệp đã chọn");
        if (data.kind() == ImageValidationCase.FileKind.JPEG) {
            Assert.assertTrue(observedPreview, "Ảnh hợp lệ phải có preview");
        }
        page.submit();
        if (data.expectedValid()) {
            long count = page.countExact(word);
            actualResult("fileName=" + observedFileName + ", preview=" + observedPreview
                    + ", exactRecordCount=" + count);
            Assert.assertEquals(count, 1L, "Ảnh đúng biên phải được lưu");
        } else {
            List<String> messages = page.validationMessages();
            actualResult("fileName=" + page.uploadedFileName() + ", validation=" + messages
                    + ", fieldsPreserved=" + (page.enteredWord().equals(word)
                    && page.definition().equals(record.definition()) && page.example().equals(record.example()))
                    + ", exactRecordCount=" + page.countExact(word));
            Assert.assertEquals(page.enteredWord(), word, "Form làm mất trường word sau lỗi upload");
            Assert.assertEquals(page.definition(), record.definition(), "Form làm mất definition sau lỗi upload");
            Assert.assertEquals(page.example(), record.example(), "Form làm mất example sau lỗi upload");
            long count = page.countExact(word);
            Assert.assertEquals(count, 0L, "Upload không hợp lệ không được tạo vocabulary");
            Assert.assertFalse(messages.isEmpty(), "Thiếu thông báo upload không hợp lệ");
        }
    }

    @Test(description = "KB-11-005 - Đối chiếu menu, bảng và checkbox với Excel approved")
    public void kb11005CompareMenusTablesAndCheckboxes() {
        evidenceContext("KB-11-005", "TD-11-COMPARISON");
        resultDetails("Đối chiếu menu, bảng và hành vi checkbox",
                "Excel datasets: TD-MENU-ADMIN-APPROVED, TD-11-VOCAB-COLUMNS-APPROVED, TD-FIXTURE-VOCAB-01",
                "Nhãn và thứ tự đúng dataset; record khớp theo word; checkbox row/header/counter đúng trạng thái.");
        VocabularyTestConfig settings = VocabularyTestConfig.fromEnvironment();
        List<String> expectedMenus;
        List<String> expectedHeaders;
        List<VocabularyRow> fixtureRows;
        try {
            VocabularyExcelDataset dataset = VocabularyExcelDataset.read(settings.approvedWorkbook());
            expectedMenus = dataset.labels("TD-MENU-ADMIN-APPROVED");
            expectedHeaders = dataset.labels("TD-11-VOCAB-COLUMNS-APPROVED");
            fixtureRows = dataset.vocabularyRows("TD-FIXTURE-VOCAB-01");
            if (fixtureRows.size() < 2) {
                throw new IllegalArgumentException("TD-FIXTURE-VOCAB-01 cần ít nhất hai record để kiểm tra checkbox");
            }
        } catch (IllegalArgumentException missing) {
            throw new SkipException("Blocked: " + missing.getMessage());
        }
        VocabularyPage page = loginAdmin();
        String runToken = uniqueLetters("run");
        List<VocabularyRow> expectedRows = fixtureRows.stream()
                .map(row -> new VocabularyRow(row.word().replaceAll("[^A-Za-z'-]", "") + runToken,
                        row.ipa(), row.topic(), row.difficulty(), row.definition(), row.example(), row.imageSource()))
                .toList();
        for (VocabularyRow row : expectedRows) {
            createManual(page, new VocabularyRecord(row.word(), row.ipa(), row.topic(), row.difficulty(),
                    row.definition(), row.example(), null));
            cleanupWords.add(row.word());
        }

        page.openList();
        SoftAssert softly = new SoftAssert();
        softly.assertEquals(page.menuLabels(), expectedMenus, "Sai nhãn/thứ tự menu");
        softly.assertEquals(page.columnHeaders(), expectedHeaders, "Sai nhóm cột/thứ tự");
        List<VocabularyRow> actualRows = page.rowsForWords(expectedRows.stream().map(VocabularyRow::word).toList());
        softly.assertEquals(TableComparator.compareByWord(expectedRows, actualRows), List.of(), "Sai dữ liệu theo khóa word");

        String first = expectedRows.get(0).word();
        String second = expectedRows.get(1).word();
        page.selectRow(first, true);
        softly.assertTrue(page.headerIndeterminate(), "Header phải indeterminate khi chọn một phần");
        softly.assertEquals(page.selectedCount(), 1, "Sai bộ đếm sau khi chọn một dòng");
        page.openList();
        page.selectAll(true);
        softly.assertTrue(page.headerChecked(), "Header phải checked sau chọn tất cả");
        softly.assertTrue(page.selectedCount() >= page.visibleRowCount(), "Bộ đếm chọn tất cả không hợp lệ");
        page.selectRow(second, false);
        softly.assertTrue(page.headerIndeterminate(), "Header phải indeterminate sau bỏ một dòng");
        actualResult("menus=" + page.menuLabels() + ", headers=" + page.columnHeaders()
                + ", matchedRows=" + actualRows.size() + ", selectedCount=" + page.selectedCount()
                + ", headerIndeterminate=" + page.headerIndeterminate());
        softly.assertAll();
    }

    @Test(description = "KB-11-004 - Nhất quán vocabulary giữa Flashcard, Quiz và Match")
    public void kb11004VerifyVocabularyAcrossLearningMethods() {
        evidenceContext("KB-11-004", "CROSS_MODULE_DETERMINISTIC_TOPIC");
        resultDetails("Nhất quán vocabulary giữa Flashcard, Quiz và Match",
                "topic=PWW_VOCAB_XMOD_TOPIC; deterministic fixture; separate regular User session",
                "Fixture xuất hiện theo allocation rule; IPA, nghĩa và ví dụ khớp record quản trị khi được hiển thị.");
        VocabularyTestConfig settings = VocabularyTestConfig.fromEnvironment();
        if (!settings.allocationConfirmed() || settings.crossModuleTopic() == null) {
            throw new SkipException("Blocked: thiếu quy tắc phân bổ/cache đã duyệt và PWW_VOCAB_XMOD_TOPIC");
        }
        require(settings.userUsername(), "PWW_USER_USERNAME");
        require(settings.userPassword(), "PWW_USER_PASSWORD");
        if (Objects.equals(settings.adminUsername(), settings.userUsername())
                && Objects.equals(settings.adminPassword(), settings.userPassword())) {
            throw new SkipException("Blocked: KB-11-004 yêu cầu session User thường nhưng cấu hình User trùng tài khoản Admin");
        }
        VocabularyPage adminPage = loginAdmin();
        String word = uniqueLetters("qaxmod");
        VocabularyRecord record = new VocabularyRecord(word, "/qa/", settings.crossModuleTopic(), approvedDifficulty(),
                "Nghĩa kiểm thử liên mô-đun", "QA cross-module example.", null);
        cleanupWords.add(word);
        createManual(adminPage, record);
        Assert.assertEquals(adminPage.countExact(word), 1L, "Fixture quản trị phải tồn tại độc lập");

        WebDriver userDriver = DriverFactory.create(config());
        try {
            try {
                new LoginPage(userDriver, config().timeout()).login(config().baseUrl(), settings.userUsername(), settings.userPassword());
            } catch (IllegalStateException rejected) {
                throw new SkipException("Blocked: tài khoản User không đăng nhập được - " + rejected.getMessage());
            }
            LearningVocabularyPage learning = new LearningVocabularyPage(userDriver, config().timeout());
            SoftAssert softly = new SoftAssert();
            for (LearningVocabularyPage.Method method : LearningVocabularyPage.Method.values()) {
                String body = learning.openTopic(config().baseUrl(), method, settings.crossModuleTopic());
                softly.assertTrue(body.contains(word), method + " thiếu fixture " + word);
                assertIfFieldDisplayed(softly, body, "IPA", record.ipa(), method);
                assertIfFieldDisplayed(softly, body, "Nghĩa", record.definition(), method);
                assertIfFieldDisplayed(softly, body, "Ví dụ", record.example(), method);
            }
            actualResult("Đã quan sát fixture " + word + " trong "
                    + java.util.Arrays.toString(LearningVocabularyPage.Method.values())
                    + "; các trường được hiển thị khớp record quản trị.");
            softly.assertAll();
        } finally {
            userDriver.quit();
        }
    }

    @AfterMethod(alwaysRun = true)
    public void cleanupVocabularyFixtures() {
        if (cleanupWords.isEmpty() || driver() == null) return;
        try {
            VocabularyTestConfig settings = VocabularyTestConfig.fromEnvironment();
            if (driver().getCurrentUrl().contains("login.php")) {
                if (settings.adminUsername() == null || settings.adminPassword() == null) return;
                new LoginPage(driver(), config().timeout()).login(config().baseUrl(), settings.adminUsername(), settings.adminPassword());
            }
            VocabularyPage page = new VocabularyPage(driver(), config());
            page.openList();
            page.selectAll(false); // Xóa trạng thái chọn session, không xóa dữ liệu nền.
            for (String word : new ArrayList<>(cleanupWords)) page.deleteExact(word);
        } finally {
            cleanupWords.clear();
        }
    }

    private VocabularyPage loginAdmin() {
        VocabularyTestConfig settings = VocabularyTestConfig.fromEnvironment();
        require(settings.adminUsername(), "PWW_ADMIN_USERNAME");
        require(settings.adminPassword(), "PWW_ADMIN_PASSWORD");
        try {
            new LoginPage(driver(), config().timeout()).login(config().baseUrl(), settings.adminUsername(), settings.adminPassword());
        } catch (IllegalStateException rejected) {
            throw new SkipException("Blocked: tài khoản Admin không đăng nhập được - " + rejected.getMessage());
        }
        return new VocabularyPage(driver(), config());
    }

    private void createManual(VocabularyPage page, VocabularyRecord record) {
        page.openCreateForm();
        requireFormOptions(page, record);
        page.fill(record, true);
        page.submit();
    }

    private static void requireFormOptions(VocabularyPage page, VocabularyRecord record) {
        if (!page.topicOptions().contains(record.topic())) {
            throw new SkipException("Blocked: topic fixture không tồn tại trên form: " + record.topic());
        }
        if (!page.difficultyOptions().contains(record.difficulty())) {
            throw new SkipException("Blocked: difficulty đã cấu hình không tồn tại trên form: " + record.difficulty());
        }
    }

    private static VocabularyRecord validRecord(String word, String ipa, String topic, Path image) {
        return new VocabularyRecord(word, ipa, topic, approvedDifficulty(),
                "Nghĩa kiểm thử QA", "This QA word is used for testing.", image);
    }

    private static String approvedDifficulty() {
        String value = VocabularyTestConfig.fromEnvironment().approvedDifficulty();
        if (value == null) {
            throw new SkipException("Blocked: chưa chốt PWW_VOCAB_DIFFICULTY; "
                    + "SRS dùng Dễ/Trung bình/Khó nhưng UI dùng Low/Medium/High");
        }
        return value;
    }

    private static void assertPersisted(VocabularyRecord expected, VocabularyRow actual) {
        SoftAssert softly = new SoftAssert();
        softly.assertEquals(actual.word(), expected.word());
        softly.assertEquals(actual.ipa(), expected.ipa());
        softly.assertEquals(actual.topic(), expected.topic());
        softly.assertEquals(actual.difficulty(), expected.difficulty());
        softly.assertEquals(actual.definition(), expected.definition());
        softly.assertEquals(actual.example(), expected.example());
        softly.assertFalse(actual.imageSource().isBlank(), "Thiếu ảnh đã lưu");
        softly.assertAll();
    }

    private static void assertIfFieldDisplayed(SoftAssert softly, String body, String label, String value,
            LearningVocabularyPage.Method method) {
        if (body.contains(label)) softly.assertTrue(body.contains(value), method + " hiển thị " + label + " nhưng sai giá trị");
    }

    private static String unusedOneCharacter(VocabularyPage page) {
        for (char value = 'a'; value <= 'z'; value++) {
            String candidate = String.valueOf(value);
            if (page.countExact(candidate) == 0) return candidate;
        }
        throw new SkipException("Blocked: không còn từ một ký tự trống trong môi trường test");
    }

    private static String uniqueLetters(String prefix) {
        long value = Math.abs(System.nanoTime());
        StringBuilder suffix = new StringBuilder();
        do {
            suffix.append((char) ('a' + value % 26));
            value /= 26;
        } while (value > 0);
        return prefix + suffix;
    }

    private static void require(String value, String variable) {
        if (value == null || value.isBlank()) throw new SkipException("Blocked: thiếu biến môi trường " + variable);
    }

    private static String validationCaseName(String datasetId) {
        return switch (datasetId) {
            case "EMPTY_WORD" -> "Từ để trống";
            case "ONE_CHARACTER" -> "Từ hợp lệ một ký tự";
            case "MAX_LENGTH" -> "Từ đúng giới hạn tối đa";
            case "MAX_PLUS_ONE" -> "Từ vượt giới hạn một ký tự";
            case "INVALID_CHARACTERS" -> "Từ chứa ký tự không hợp lệ";
            case "DUPLICATE_WORD" -> "Từ trùng fixture cô lập";
            default -> datasetId;
        };
    }

    private static String validationExpected(ValidationCase data) {
        if (data.expectedValid()) return "Tạo đúng một record vocabulary.";
        if ("MAX_PLUS_ONE".equals(data.datasetId())) {
            return "Input chặn/truncate theo maxlength hoặc hiển thị validation; không tạo record từ giá trị vượt biên.";
        }
        return "Hiển thị validation phù hợp và không tạo thêm record vocabulary.";
    }

    private static String imageCaseName(String datasetId) {
        return switch (datasetId) {
            case "VALID_EXACT_LIMIT" -> "Ảnh JPEG đúng giới hạn dung lượng";
            case "IMAGE_LIMIT_PLUS_ONE" -> "Ảnh JPEG vượt giới hạn một byte";
            case "PDF_UNSUPPORTED" -> "Tệp PDF nhỏ hơn giới hạn";
            default -> datasetId;
        };
    }

    private static String imageExpected(ImageValidationCase data) {
        return data.expectedValid()
                ? "Hiển thị đúng tên file và preview; lưu đúng một vocabulary."
                : "Hiển thị validation upload; không lưu vocabulary; giữ nguyên các trường text đã nhập.";
    }

    private static String printable(String value) {
        return value == null || value.isEmpty() ? "<empty>" : value;
    }
}
