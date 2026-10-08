package vn.team.autotest.automation.registry;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import vn.team.autotest.modules.customflashcard.scenario.CustomFlashcardScenario;
import vn.team.autotest.modules.dashboard.scenario.DashboardScenario;
import vn.team.autotest.modules.flashcard.scenario.FlashcardScenario;
import vn.team.autotest.modules.folder.scenario.FolderScenario;
import vn.team.autotest.modules.game.scenario.GameScenario;
import vn.team.autotest.modules.home.scenario.HomeScenario;
import vn.team.autotest.modules.learning.scenario.LearningScenario;
import vn.team.autotest.modules.library.scenario.LibraryScenario;
import vn.team.autotest.modules.matching.scenario.MatchingScenario;
import vn.team.autotest.modules.notification.scenario.NotificationScenario;
import vn.team.autotest.modules.quiz.scenario.QuizScenario;
import vn.team.autotest.modules.users.scenario.UsersScenario;
import vn.team.autotest.modules.vocabulary.scenario.VocabularyScenario;
import vn.team.autotest.modules.wordtype.scenario.WordTypeScenario;

public final class ScenarioRegistry {
    private ScenarioRegistry() {}
    private static final Map<String, Class<?>> SCENARIOS = new LinkedHashMap<>();
    static {
        SCENARIOS.put("home", HomeScenario.class);
        SCENARIOS.put("dashboard", DashboardScenario.class);
        SCENARIOS.put("learning", LearningScenario.class);
        SCENARIOS.put("flashcard", FlashcardScenario.class);
        SCENARIOS.put("quiz", QuizScenario.class);
        SCENARIOS.put("matching", MatchingScenario.class);
        SCENARIOS.put("game", GameScenario.class);
        SCENARIOS.put("notification", NotificationScenario.class);
        SCENARIOS.put("wordtype", WordTypeScenario.class);
        SCENARIOS.put("vocabulary", VocabularyScenario.class);
        SCENARIOS.put("folder", FolderScenario.class);
        SCENARIOS.put("library", LibraryScenario.class);
        SCENARIOS.put("customflashcard", CustomFlashcardScenario.class);
        SCENARIOS.put("users", UsersScenario.class);
    }
    public static Class<?> byModuleId(String moduleId) {
        Class<?> scenario = SCENARIOS.get(moduleId);
        if (scenario == null) throw new IllegalArgumentException("Không tồn tại scenario: " + moduleId);
        return scenario;
    }
    public static List<Class<?>> all() { return List.copyOf(SCENARIOS.values()); }
}
