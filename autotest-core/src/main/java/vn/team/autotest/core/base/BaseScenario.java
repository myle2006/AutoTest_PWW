package vn.team.autotest.core.base;

import org.openqa.selenium.WebDriver;
import org.testng.ITestResult;
import org.testng.Reporter;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import vn.team.autotest.core.config.AppConfig;
import vn.team.autotest.core.driver.DriverFactory;
import vn.team.autotest.core.evidence.FailureEvidenceService;

public abstract class BaseScenario {
    private static final ThreadLocal<WebDriver> DRIVERS = new ThreadLocal<>();
    private static final ThreadLocal<String> SCENARIO_IDS = new ThreadLocal<>();
    private static final ThreadLocal<String> DATASET_IDS = new ThreadLocal<>();
    private static final ThreadLocal<String> CASE_NAMES = new ThreadLocal<>();
    private static final ThreadLocal<String> TEST_DATA = new ThreadLocal<>();
    private static final ThreadLocal<String> EXPECTED_RESULTS = new ThreadLocal<>();
    private static final ThreadLocal<String> ACTUAL_RESULTS = new ThreadLocal<>();
    private AppConfig config;

    @BeforeMethod(alwaysRun = true)
    public void openBrowser() {
        config = AppConfig.fromEnvironment();
        DRIVERS.set(DriverFactory.create(config));
    }

    @AfterMethod(alwaysRun = true)
    public void closeBrowser(ITestResult result) {
        WebDriver driver = DRIVERS.get();
        String scenarioId = SCENARIO_IDS.get() == null ? result.getMethod().getMethodName() : SCENARIO_IDS.get();
        String datasetId = DATASET_IDS.get() == null ? "default" : DATASET_IDS.get();
        result.setAttribute("scenarioId", scenarioId);
        result.setAttribute("datasetId", datasetId);
        setIfPresent(result, "caseName", CASE_NAMES.get());
        setIfPresent(result, "testData", TEST_DATA.get());
        setIfPresent(result, "expectedResult", EXPECTED_RESULTS.get());
        setIfPresent(result, "actualResult", ACTUAL_RESULTS.get());
        try {
            if (driver == null) return;
            if (!result.isSuccess() && result.getAttribute("evidencePath") == null) {
                result.setAttribute("evidencePath", FailureEvidenceService.capture(
                        driver, scenarioId, datasetId, result.getThrowable()));
            }
            driver.quit();
        } finally {
            DRIVERS.remove();
            SCENARIO_IDS.remove();
            DATASET_IDS.remove();
            CASE_NAMES.remove();
            TEST_DATA.remove();
            EXPECTED_RESULTS.remove();
            ACTUAL_RESULTS.remove();
        }
    }

    protected WebDriver driver() { return DRIVERS.get(); }
    protected AppConfig config() { return config; }
    protected void open(String route) { driver().get(config.baseUrl() + (route.startsWith("/") ? route : "/" + route)); }
    protected void evidenceContext(String scenarioId, String datasetId) {
        SCENARIO_IDS.set(scenarioId);
        DATASET_IDS.set(datasetId);
        ITestResult current = Reporter.getCurrentTestResult();
        if (current != null) {
            current.setAttribute("scenarioId", scenarioId);
            current.setAttribute("datasetId", datasetId);
        }
    }
    protected void resultDetails(String caseName, String testData, String expectedResult) {
        CASE_NAMES.set(caseName);
        TEST_DATA.set(testData);
        EXPECTED_RESULTS.set(expectedResult);
        ITestResult current = Reporter.getCurrentTestResult();
        if (current != null) {
            setIfPresent(current, "caseName", caseName);
            setIfPresent(current, "testData", testData);
            setIfPresent(current, "expectedResult", expectedResult);
        }
    }
    protected void actualResult(String actualResult) {
        ACTUAL_RESULTS.set(actualResult);
        ITestResult current = Reporter.getCurrentTestResult();
        if (current != null) setIfPresent(current, "actualResult", actualResult);
    }
    public static WebDriver currentDriver() { return DRIVERS.get(); }
    private static void setIfPresent(ITestResult result, String name, String value) {
        if (value != null && !value.isBlank()) result.setAttribute(name, value);
    }
}
