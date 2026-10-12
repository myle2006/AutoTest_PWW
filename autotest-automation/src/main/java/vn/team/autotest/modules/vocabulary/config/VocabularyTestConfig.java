package vn.team.autotest.modules.vocabulary.config;

import java.nio.file.Path;
import java.util.Optional;

public record VocabularyTestConfig(
        String adminUsername,
        String adminPassword,
        String userUsername,
        String userPassword,
        Integer approvedMaxWordLength,
        Long approvedImageLimitBytes,
        String approvedDifficulty,
        Path approvedWorkbook,
        String crossModuleTopic,
        boolean allocationConfirmed,
        boolean selectionPersistenceConfirmed) {

    public static VocabularyTestConfig fromEnvironment() {
        return new VocabularyTestConfig(
                env("PWW_ADMIN_USERNAME"), env("PWW_ADMIN_PASSWORD"),
                env("PWW_USER_USERNAME"), env("PWW_USER_PASSWORD"),
                integer("PWW_VOCAB_MAX_LENGTH"), longValue("PWW_VOCAB_IMAGE_LIMIT_BYTES"),
                env("PWW_VOCAB_DIFFICULTY"),
                Optional.ofNullable(env("PWW_VOCAB_EXPECTED_XLSX")).map(Path::of).orElse(null),
                env("PWW_VOCAB_XMOD_TOPIC"),
                Boolean.parseBoolean(envOr("PWW_VOCAB_ALLOCATION_CONFIRMED", "false")),
                Boolean.parseBoolean(envOr("PWW_VOCAB_SELECTION_PERSISTS", "false")));
    }

    private static String env(String name) {
        String value = System.getenv(name);
        return value == null || value.isBlank() ? null : value.trim();
    }
    private static String envOr(String name, String fallback) {
        String value = env(name);
        return value == null ? fallback : value;
    }
    private static Integer integer(String name) {
        String value = env(name);
        return value == null ? null : Integer.valueOf(value);
    }
    private static Long longValue(String name) {
        String value = env(name);
        return value == null ? null : Long.valueOf(value);
    }
}
