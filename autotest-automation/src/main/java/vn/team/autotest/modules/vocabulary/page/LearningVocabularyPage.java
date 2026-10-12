package vn.team.autotest.modules.vocabulary.page;

import java.time.Duration;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import vn.team.autotest.core.base.BasePage;

public final class LearningVocabularyPage extends BasePage {
    public enum Method { FLASHCARD("flashcard"), QUIZ("quiz"), MATCH("match");
        private final String routeValue;
        Method(String routeValue) { this.routeValue = routeValue; }
    }

    public LearningVocabularyPage(WebDriver driver, Duration timeout) { super(driver, timeout); }

    public String openTopic(String baseUrl, Method method, String topic) {
        driver.get(baseUrl + "/select_topic.php?type=" + method.routeValue);
        By topicButton = By.xpath("//button[.//*[normalize-space()=" + literal(topic) + "]]");
        click(topicButton);
        wait.until(ExpectedConditions.not(ExpectedConditions.urlContains("select_topic.php")));
        return visible(By.tagName("body")).getText();
    }

    private static String literal(String value) {
        if (!value.contains("'")) return "'" + value + "'";
        if (!value.contains("\"")) return "\"" + value + "\"";
        throw new IllegalArgumentException("Topic chứa cả hai loại dấu nháy");
    }
}
