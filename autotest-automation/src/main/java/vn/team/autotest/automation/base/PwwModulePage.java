package vn.team.autotest.automation.base;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.testng.Assert;
import vn.team.autotest.core.base.BasePage;
import vn.team.autotest.core.config.AppConfig;

public abstract class PwwModulePage extends BasePage {
    private final String route;

    protected PwwModulePage(WebDriver driver, AppConfig config, String route) {
        super(driver, config.timeout());
        this.route = route;
    }

    public void waitUntilReady() { visible(By.tagName("body")); }

    public void verifyPrimaryContent() {
        Assert.assertTrue(currentUrl().contains(route.equals("/") ? configPathRoot() : route),
                "URL không đúng route mong đợi: " + route);
        Assert.assertFalse(text(By.tagName("body")).isBlank(), "Trang không có nội dung hiển thị");
    }

    private String configPathRoot() { return currentUrl().replaceFirst("https?://[^/]+", ""); }
}
