package vn.team.autotest.automation.auth;

import java.time.Duration;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import vn.team.autotest.core.base.BasePage;

public final class LoginPage extends BasePage {
    private static final By USERNAME = By.id("username");
    private static final By PASSWORD = By.id("password");
    private static final By SUBMIT = By.cssSelector("#loginForm button[type='submit']");
    private static final By SERVER_ERROR = By.cssSelector(".server-alert.danger");

    public LoginPage(WebDriver driver, Duration timeout) {
        super(driver, timeout);
    }

    public void login(String baseUrl, String username, String password) {
        driver.get(baseUrl + "/login.php");
        type(USERNAME, username);
        type(PASSWORD, password);
        click(SUBMIT);
        wait.until(ExpectedConditions.or(
                ExpectedConditions.not(ExpectedConditions.urlContains("login.php")),
                ExpectedConditions.visibilityOfElementLocated(SERVER_ERROR)));
        if (driver.getCurrentUrl().contains("login.php") && exists(SERVER_ERROR)) {
            throw new IllegalStateException("Website từ chối thông tin đăng nhập: " + text(SERVER_ERROR));
        }
    }
}
