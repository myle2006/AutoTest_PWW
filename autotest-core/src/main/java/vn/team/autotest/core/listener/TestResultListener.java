package vn.team.autotest.core.listener;

import java.util.List;
import java.util.function.Consumer;
import vn.team.autotest.core.model.TestResult;
import vn.team.autotest.core.model.TestResultCollector;

public final class TestResultListener extends TestResultCollector {
    public TestResultListener(String runId) {
        super(runId);
    }
    public TestResultListener(String runId, Consumer<List<TestResult>> onUpdate) {
        super(runId, onUpdate);
    }
}
