package vn.team.autotest.modules.vocabulary.scenario;

/**
 * Adapter để Maven Surefire có thể chọn từng method bằng -Dtest.
 * Toàn bộ test logic vẫn nằm trong VocabularyScenario dùng chung với runner của ứng dụng.
 */
public final class VocabularyScenarioTest extends VocabularyScenario {}
