package vn.team.autotest.core.driver;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import vn.team.autotest.core.config.AppConfig;

public final class DriverFactory {
    private DriverFactory() {}

    public static WebDriver create(AppConfig config) {
        WebDriver driver = switch (config.browser().toLowerCase()) {
            case "firefox" -> new FirefoxDriver(firefoxOptions(config));
            case "edge" -> new EdgeDriver(edgeOptions(config));
            default -> new ChromeDriver(chromeOptions(config));
        };
        driver.manage().timeouts().implicitlyWait(config.timeout());
        driver.manage().timeouts().pageLoadTimeout(config.timeout().multipliedBy(2));
        if (!config.headless()) driver.manage().window().maximize();
        return driver;
    }

    private static ChromeOptions chromeOptions(AppConfig config) {
        ChromeOptions options = new ChromeOptions();
        if (config.headless()) options.addArguments("--headless=new", "--window-size=1440,900");
        options.addArguments("--disable-notifications", "--disable-search-engine-choice-screen");
        return options;
    }

    private static FirefoxOptions firefoxOptions(AppConfig config) {
        FirefoxOptions options = new FirefoxOptions();
        if (config.headless()) options.addArguments("-headless", "--width=1440", "--height=900");
        return options;
    }

    private static EdgeOptions edgeOptions(AppConfig config) {
        EdgeOptions options = new EdgeOptions();
        if (config.headless()) options.addArguments("--headless=new", "--window-size=1440,900");
        return options;
    }
}
