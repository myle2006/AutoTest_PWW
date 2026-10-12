package vn.team.autotest.core.base;

import java.time.Duration;
import java.util.List;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

public abstract class BasePage {
    protected final WebDriver driver;
    protected final WebDriverWait wait;

    protected BasePage(WebDriver driver, Duration timeout) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, timeout);
    }

    protected WebElement visible(By locator) {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
    }

    protected WebElement present(By locator) { return wait.until(ExpectedConditions.presenceOfElementLocated(locator)); }
    protected List<WebElement> all(By locator) { return driver.findElements(locator); }
    protected void click(By locator) { wait.until(ExpectedConditions.elementToBeClickable(locator)).click(); }
    protected void type(By locator, String value) { WebElement field = visible(locator); field.clear(); field.sendKeys(value); }
    protected void selectText(By locator, String value) { new Select(visible(locator)).selectByVisibleText(value); }
    protected String text(By locator) { return visible(locator).getText().trim(); }
    protected String value(By locator) { return present(locator).getAttribute("value"); }
    protected boolean exists(By locator) { return !driver.findElements(locator).isEmpty(); }
    protected boolean selected(By locator) { return present(locator).isSelected(); }
    protected Object script(String script, Object... args) {
        return ((JavascriptExecutor) driver).executeScript(script, args);
    }
    public String title() { return driver.getTitle(); }
    public String currentUrl() { return driver.getCurrentUrl(); }
}
