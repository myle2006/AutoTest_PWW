package vn.team.autotest.core.model;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.testng.ITestResult;
import org.testng.TestListenerAdapter;
import org.openqa.selenium.WebDriver;
import vn.team.autotest.core.base.BaseScenario;
import vn.team.autotest.core.evidence.FailureEvidenceService;

public class TestResultCollector extends TestListenerAdapter {
    private final String runId;
    private final List<TestResult> results = new CopyOnWriteArrayList<>();
    private final Consumer<List<TestResult>> onUpdate;
    private static final Pattern SCENARIO_PATTERN = Pattern.compile("([A-Z]{2}-\\d{2}-\\d{3})");

    public TestResultCollector(String runId) { this(runId, ignored -> {}); }
    public TestResultCollector(String runId, Consumer<List<TestResult>> onUpdate) {
        this.runId = runId;
        this.onUpdate = onUpdate == null ? ignored -> {} : onUpdate;
    }

    @Override public void onTestSuccess(ITestResult result) { add(result, TestStatus.PASS); }
    @Override public void onTestFailure(ITestResult result) { add(result, TestStatus.FAIL); }
    @Override public void onTestSkipped(ITestResult result) { add(result, TestStatus.SKIP); }

    public List<TestResult> results() { return List.copyOf(results); }

    private void add(ITestResult raw, TestStatus status) {
        String packageName = raw.getTestClass().getRealClass().getPackageName();
        String marker = ".modules.";
        int start = packageName.indexOf(marker);
        String moduleId = start < 0 ? "unknown" : packageName.substring(start + marker.length()).split("\\.")[0];
        Throwable failure = raw.getThrowable();
        String reason = failure == null ? null : concise(failure.getMessage());
        if (status == TestStatus.SKIP && reason != null && reason.startsWith("Blocked:")) status = TestStatus.BLOCKED;
        String description = Objects.toString(raw.getMethod().getDescription(), raw.getMethod().getMethodName());
        String scenarioId = attribute(raw, "scenarioId", scenarioFrom(description));
        String datasetId = attribute(raw, "datasetId", datasetFrom(raw));
        Object evidence = raw.getAttribute("evidencePath");
        if (status != TestStatus.PASS && evidence == null && raw.getInstance() instanceof BaseScenario) {
            WebDriver driver = BaseScenario.currentDriver();
            if (driver != null) {
                evidence = FailureEvidenceService.capture(driver, scenarioId, datasetId, failure);
                raw.setAttribute("evidencePath", evidence);
            }
        }
        String testCase = attribute(raw, "caseName", readableCase(description));
        String testData = attribute(raw, "testData", parameters(raw));
        String expected = attribute(raw, "expectedResult", "Hoàn thành các assertion của test definition đã duyệt.");
        String actual = attribute(raw, "actualResult", defaultActual(status, reason));
        String invocationId = scenarioId + "::" + datasetId;
        TestResult item = new TestResult(runId, invocationId, scenarioId, raw.getMethod().getMethodName(),
                moduleId, datasetId, testCase, testData, expected, actual, status,
                Math.max(0, raw.getEndMillis() - raw.getStartMillis()), reason,
                fullLog(raw, scenarioId, datasetId, status, actual), evidence == null ? null : evidence.toString());
        results.removeIf(existing -> existing.invocationId().equals(invocationId));
        results.add(item);
        onUpdate.accept(results());
    }

    private static String attribute(ITestResult raw, String name, String fallback) {
        Object value = raw.getAttribute(name);
        return value == null || value.toString().isBlank() ? fallback : value.toString();
    }
    private static String scenarioFrom(String description) {
        Matcher matcher = SCENARIO_PATTERN.matcher(description);
        return matcher.find() ? matcher.group(1) : "UNMAPPED";
    }
    private static String datasetFrom(ITestResult raw) {
        return raw.getParameters().length == 0 ? "default" : String.valueOf(raw.getParameters()[0]);
    }
    private static String parameters(ITestResult raw) {
        return raw.getParameters().length == 0 ? "Không có dataset tham số" : Arrays.toString(raw.getParameters());
    }
    private static String readableCase(String description) {
        return description.replaceFirst("^[A-Z]{2}-\\d{2}-\\d{3}\\s*-\\s*", "");
    }
    private static String defaultActual(TestStatus status, String reason) {
        if (reason != null) return reason;
        return status == TestStatus.PASS ? "Tất cả assertion đã pass theo quan sát thực tế."
                : "Không có mô tả kết quả thực tế.";
    }
    private static String concise(String value) {
        if (value == null || value.isBlank()) return "Không có thông báo chi tiết";
        String firstLine = value.lines().findFirst().orElse(value).trim();
        return firstLine.length() <= 240 ? firstLine : firstLine.substring(0, 237) + "...";
    }
    private static String fullLog(ITestResult raw, String scenarioId, String datasetId,
            TestStatus status, String actual) {
        StringBuilder log = new StringBuilder()
                .append("scenarioId=").append(scenarioId).append('\n')
                .append("datasetId=").append(datasetId).append('\n')
                .append("method=").append(raw.getMethod().getQualifiedName()).append('\n')
                .append("status=").append(status).append('\n')
                .append("durationMillis=").append(Math.max(0, raw.getEndMillis() - raw.getStartMillis())).append('\n')
                .append("parameters=").append(parameters(raw)).append('\n')
                .append("actual=").append(actual).append('\n');
        if (raw.getThrowable() != null) {
            StringWriter trace = new StringWriter();
            raw.getThrowable().printStackTrace(new PrintWriter(trace));
            log.append("\nstackTrace:\n").append(trace);
        }
        return log.toString();
    }
}
