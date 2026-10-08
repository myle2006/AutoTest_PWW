package vn.team.autotest.core.config;

import java.time.Duration;

public record AppConfig(String baseUrl, String browser, boolean headless, Duration timeout) {
    public static AppConfig fromEnvironment() {
        String baseUrl = value("PWW_BASE_URL", "http://localhost:8000");
        String browser = value("PWW_BROWSER", "chrome");
        boolean headless = Boolean.parseBoolean(value("PWW_HEADLESS", "true"));
        long timeoutSeconds = Long.parseLong(value("PWW_TIMEOUT_SECONDS", "15"));
        return new AppConfig(baseUrl.replaceAll("/$", ""), browser, headless, Duration.ofSeconds(timeoutSeconds));
    }

    private static String value(String name, String fallback) {
        String value = System.getenv(name);
        return value == null || value.isBlank() ? fallback : value.trim();
    }
}
