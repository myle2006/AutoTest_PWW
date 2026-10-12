package vn.team.autotest.modules.vocabulary.page;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import java.util.Optional;
import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import vn.team.autotest.automation.base.PwwModulePage;
import vn.team.autotest.core.config.AppConfig;
import vn.team.autotest.modules.vocabulary.model.VocabularyRecord;
import vn.team.autotest.modules.vocabulary.model.VocabularyRow;

public final class VocabularyPage extends PwwModulePage {
    public static final String LIST_ROUTE = "/admin_vocab.php";
    public static final String CREATE_ROUTE = "/admin_vocab_action.php";

    private static final By WORD = By.id("word-input");
    private static final By IPA = By.id("ipa-input");
    private static final By IPA_STATUS = By.id("ipa-status");
    private static final By SPINNER = By.id("spinner");
    private static final By DEFINITION = By.id("definition-input");
    private static final By EXAMPLE = By.name("example");
    private static final By TOPIC = By.id("typeword-select");
    private static final By DIFFICULTY = By.id("difficulty-select");
    private static final By IMAGE = By.id("imageFileInput");
    private static final By FILE_NAME = By.id("fileName");
    private static final By PREVIEW = By.cssSelector("#filePreview img");
    private static final By SUBMIT = By.cssSelector("#vocabForm button[type='submit']");
    private static final By TABLE_ROWS = By.cssSelector("table.data-table tbody tr");
    private static final By HEADER = By.cssSelector("table.data-table thead th");
    private static final By HEADER_CHECKBOX = By.id("selectAll");
    private static final By SELECTED_COUNT = By.id("selectedCount");
    private static final By ROW_CHECKBOXES = By.cssSelector("table.data-table tbody .item-checkbox");
    private static final By ERRORS = By.cssSelector(".error-text, .error-message, .field-error, .alert-danger, .alert-error, .form-error");

    private final AppConfig config;

    public VocabularyPage(WebDriver driver, AppConfig config) {
        super(driver, config, LIST_ROUTE);
        this.config = config;
    }

    public void openList() {
        driver.get(config.baseUrl() + LIST_ROUTE);
        visible(By.cssSelector("table.data-table"));
    }

    public void openCreateForm() {
        driver.get(config.baseUrl() + CREATE_ROUTE);
        visible(WORD);
    }

    public void installAutomaticIpaMock(String word, String ipa, String definition) {
        // Mock chỉ thay kho từ điển trên form; vẫn kiểm tra đúng luồng oninput/debounce/tự điền UI.
        script("quickDict[arguments[0].toLowerCase()]={ipa:arguments[1],vi:arguments[2]};", word, ipa, definition);
    }

    public long enterWordAndWaitForAutomaticIpa(String word) {
        long started = System.nanoTime();
        type(WORD, word);
        wait.until(d -> !value(IPA).isBlank() && !"block".equals(present(SPINNER).getCssValue("display")));
        return Duration.ofNanos(System.nanoTime() - started).toMillis();
    }

    public String ipa() { return value(IPA); }
    public String ipaStatus() { return text(IPA_STATUS); }
    public String enteredWord() { return value(WORD); }
    public String definition() { return value(DEFINITION); }
    public String example() { return value(EXAMPLE); }
    public int wordMaxLength() { return Integer.parseInt(present(WORD).getAttribute("maxlength")); }
    public List<String> topicOptions() {
        return all(By.cssSelector("#typeword-select option")).stream()
                .map(element -> element.getText().trim()).filter(value -> !value.isBlank()).toList();
    }
    public List<String> difficultyOptions() {
        return all(By.cssSelector("#difficulty-select option")).stream()
                .map(element -> element.getText().trim()).filter(value -> !value.isBlank()).toList();
    }

    public void fill(VocabularyRecord record, boolean includeIpa) {
        type(WORD, record.word());
        if (includeIpa) type(IPA, record.ipa());
        selectText(TOPIC, record.topic());
        selectText(DIFFICULTY, record.difficulty());
        type(DEFINITION, record.definition());
        type(EXAMPLE, record.example());
        if (record.image() != null) upload(record.image());
    }

    public void fillRemaining(VocabularyRecord record) {
        selectText(TOPIC, record.topic());
        selectText(DIFFICULTY, record.difficulty());
        type(DEFINITION, record.definition());
        type(EXAMPLE, record.example());
        if (record.image() != null) upload(record.image());
    }

    public void upload(Path file) {
        present(IMAGE).sendKeys(file.toAbsolutePath().toString());
        wait.until(ExpectedConditions.textToBePresentInElementLocated(FILE_NAME, file.getFileName().toString()));
    }

    public boolean previewVisible() {
        try {
            WebElement preview = wait.until(ExpectedConditions.visibilityOfElementLocated(PREVIEW));
            // Chờ trình duyệt giải mã ảnh xong, không chỉ chờ thẻ img vừa được gắn vào DOM.
            return wait.until(d -> ((Number) script("return arguments[0].naturalWidth;", preview)).intValue() > 0);
        } catch (TimeoutException timeout) {
            return false;
        }
    }
    public String uploadedFileName() { return exists(FILE_NAME) ? text(FILE_NAME) : ""; }

    public void submit() {
        click(SUBMIT);
        wait.until(d -> d.getCurrentUrl().contains("admin_vocab.php") || !d.findElements(ERRORS).isEmpty()
                || !d.findElements(By.cssSelector("#vocabForm .is-invalid")).isEmpty());
    }

    public List<String> validationMessages() {
        return all(ERRORS).stream().filter(WebElement::isDisplayed).map(e -> e.getText().trim())
                .filter(value -> !value.isBlank()).toList();
    }

    public long countExact(String word) {
        openSearch(word);
        return all(TABLE_ROWS).stream().map(this::toRow).filter(row -> row.word().equals(word)).count();
    }

    public Optional<VocabularyRow> findExact(String word) {
        openSearch(word);
        return all(TABLE_ROWS).stream().map(this::toRow).filter(row -> row.word().equals(word)).findFirst();
    }

    public List<VocabularyRow> rowsForWords(List<String> words) {
        return words.stream().map(this::findExact).flatMap(Optional::stream).toList();
    }

    public void deleteExact(String word) {
        openSearch(word);
        Optional<WebElement> row = all(TABLE_ROWS).stream().filter(element -> toRow(element).word().equals(word)).findFirst();
        if (row.isEmpty()) return;
        row.get().findElement(By.cssSelector("a[href*='action=delete']")).click();
        wait.until(ExpectedConditions.alertIsPresent()).accept();
        wait.until(ExpectedConditions.urlContains("admin_vocab.php"));
    }

    public List<String> menuLabels() {
        return all(By.cssSelector("#sec-admin a, #sec-main a, #sec-methods a")).stream()
                .map(e -> e.getText().trim()).filter(value -> !value.isBlank()).toList();
    }

    public List<String> columnHeaders() {
        return all(HEADER).stream().map(e -> e.getText().trim()).filter(value -> !value.isBlank()).toList();
    }

    public void selectRow(String word, boolean checked) {
        openSearch(word);
        WebElement row = all(TABLE_ROWS).stream().filter(element -> toRow(element).word().equals(word)).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy row: " + word));
        WebElement checkbox = row.findElement(By.cssSelector(".item-checkbox"));
        if (checkbox.isSelected() != checked) checkbox.click();
        wait.until(d -> checkbox.isSelected() == checked);
    }

    public void selectAll(boolean checked) {
        WebElement checkbox = present(HEADER_CHECKBOX);
        if (checkbox.isSelected() != checked) checkbox.click();
        wait.until(d -> checkbox.isSelected() == checked);
    }

    public boolean headerChecked() { return selected(HEADER_CHECKBOX); }
    public boolean headerIndeterminate() { return Boolean.TRUE.equals(script("return arguments[0].indeterminate;", present(HEADER_CHECKBOX))); }
    public int selectedCount() { return exists(SELECTED_COUNT) ? Integer.parseInt(text(SELECTED_COUNT)) : 0; }
    public int visibleRowCount() { return all(ROW_CHECKBOXES).size(); }

    private void openSearch(String word) {
        String encoded = URLEncoder.encode(word, StandardCharsets.UTF_8);
        driver.get(config.baseUrl() + LIST_ROUTE + "?search=" + encoded);
        visible(By.cssSelector("table.data-table"));
    }

    private VocabularyRow toRow(WebElement row) {
        List<WebElement> cells = row.findElements(By.tagName("td"));
        if (cells.size() < 8) return new VocabularyRow("", "", "", "", "", "", "");
        List<WebElement> wordParts = cells.get(3).findElements(By.cssSelector("div,span"));
        String word = wordParts.isEmpty() ? cells.get(3).getText().trim() : wordParts.get(0).getText().trim();
        String ipa = wordParts.size() < 2 ? cells.get(3).getText().replaceFirst(java.util.regex.Pattern.quote(word), "").trim()
                : wordParts.get(wordParts.size() - 1).getText().trim();
        List<WebElement> definitionParts = cells.get(6).findElements(By.cssSelector("div,span"));
        String definition = definitionParts.isEmpty() ? cells.get(6).getText().trim() : definitionParts.get(0).getText().trim();
        String example = definitionParts.size() < 2 ? "" : definitionParts.get(definitionParts.size() - 1).getText().trim().replaceAll("^\"|\"$", "");
        String image = cells.get(2).findElements(By.tagName("img")).stream().findFirst()
                .map(element -> element.getAttribute("src")).orElse("");
        return new VocabularyRow(word, ipa, cells.get(4).getText().trim(), cells.get(5).getText().trim(),
                definition, example, image);
    }
}
