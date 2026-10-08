package vn.team.autotest.core.base;

import org.openqa.selenium.WebDriver;
import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import vn.team.autotest.core.config.AppConfig;
import vn.team.autotest.core.driver.DriverFactory;
import vn.team.autotest.core.evidence.ScreenshotService;

public abstract class BaseScenario {
    private static final ThreadLocal<WebDriver> DRIVERS = new ThreadLocal<>();
    private AppConfig config;

    @BeforeMethod(alwaysRun = true)
    public void openBrowser() {
        config = AppConfig.fromEnvironment();
        DRIVERS.set(DriverFactory.create(config));
    }

    @AfterMethod(alwaysRun = true)
    public void closeBrowser(ITestResult result) {
        WebDriver driver = DRIVERS.get();
        if (driver != null) {
            if (!result.isSuccess()) result.setAttribute("evidencePath", ScreenshotService.capture(driver, result.getMethod().getMethodName()));
            driver.quit();
            DRIVERS.remove();
        }
    }

    protected WebDriver driver() { return DRIVERS.get(); }
    protected AppConfig config() { return config; }
    protected void open(String route) { driver().get(config.baseUrl() + (route.startsWith("/") ? route : "/" + route)); }
}
