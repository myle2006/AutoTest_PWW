package vn.team.autotest.core.base;

import java.time.Duration;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public abstract class BasePage {
    protected final WebDriver driver;
    private final WebDriverWait wait;

    protected BasePage(WebDriver driver, Duration timeout) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, timeout);
    }

    protected WebElement visible(By locator) {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
    }

    protected void click(By locator) { visible(locator).click(); }
    protected void type(By locator, String value) { WebElement field = visible(locator); field.clear(); field.sendKeys(value); }
    protected String text(By locator) { return visible(locator).getText().trim(); }
    public String title() { return driver.getTitle(); }
    public String currentUrl() { return driver.getCurrentUrl(); }
}
