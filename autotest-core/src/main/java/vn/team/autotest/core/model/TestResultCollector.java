package vn.team.autotest.core.model;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import org.testng.ITestResult;
import org.testng.TestListenerAdapter;

public class TestResultCollector extends TestListenerAdapter {
    private final String runId;
    private final List<TestResult> results = new CopyOnWriteArrayList<>();

    public TestResultCollector(String runId) { this.runId = runId; }

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
        Object evidence = raw.getAttribute("evidencePath");
        results.add(new TestResult(runId, raw.getMethod().getMethodName(), moduleId, status,
                raw.getEndMillis() - raw.getStartMillis(), failure == null ? null : failure.getMessage(),
                evidence == null ? null : evidence.toString()));
    }
}
