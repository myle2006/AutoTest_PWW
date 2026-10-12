package vn.team.autotest.core.model;

import java.util.ArrayList;
import java.util.List;
import org.testng.Assert;
import org.testng.ITestResult;
import org.testng.Reporter;
import org.testng.SkipException;
import org.testng.TestNG;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class TestResultCollectorTest {
    @Test
    public void publishesOneReadableRowPerDataProviderInvocation() {
        List<Integer> updateSizes = new ArrayList<>();
        TestResultCollector collector = new TestResultCollector("run-live", rows -> updateSizes.add(rows.size()));
        TestNG runner = new TestNG(false);
        runner.setUseDefaultListeners(false);
        runner.setTestClasses(new Class<?>[] {SampleInvocations.class});
        runner.addListener(collector);

        runner.run();

        Assert.assertEquals(collector.results().size(), 2);
        Assert.assertEquals(updateSizes, List.of(1, 2));
        Assert.assertEquals(collector.results().get(0).scenarioId(), "KB-11-099");
        Assert.assertEquals(collector.results().get(0).datasetId(), "DATA-A");
        Assert.assertEquals(collector.results().get(1).status(), TestStatus.BLOCKED);
        Assert.assertTrue(collector.results().get(1).logs().contains("stackTrace:"));
    }

    public static class SampleInvocations {
        @DataProvider(name = "rows")
        public Object[][] rows() { return new Object[][] {{"DATA-A"}, {"DATA-B"}}; }

        @Test(dataProvider = "rows", description = "KB-11-099 - Collector contract")
        public void sample(String dataset) {
            ITestResult result = Reporter.getCurrentTestResult();
            result.setAttribute("scenarioId", "KB-11-099");
            result.setAttribute("datasetId", dataset);
            result.setAttribute("caseName", "Readable " + dataset);
            result.setAttribute("testData", "value=" + dataset);
            result.setAttribute("expectedResult", "Approved expectation");
            if ("DATA-B".equals(dataset)) throw new SkipException("Blocked: prerequisite unavailable");
            result.setAttribute("actualResult", "Observed assertion passed");
        }
    }
}
