package vn.team.autotest.automation.registry;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import vn.team.autotest.core.model.TestDefinition;

public final class TestCatalog {
    private TestCatalog() {}

    private static final List<TestDefinition> MODULES = List.of(
        module("home", "HOME", "Trang chủ", "/", "Guest", 6, 8, "home", "home.xml"),
        module("dashboard", "03", "Bảng điều khiển", "/dashboard", "User", 8, 8, "dashboard", "dashboard.xml"),
        module("learning", "04", "Học tập", "/learning", "User", 7, 7, "learning", "learning.xml"),
        module("flashcard", "05", "Thẻ ghi nhớ", "/flashcards", "User", 9, 7, "flashcard", "flashcard.xml"),
        module("quiz", "06", "Trắc nghiệm chọn đáp án", "/quiz", "User", 10, 7, "quiz", "quiz.xml"),
        module("matching", "07", "Nối từ", "/matching", "User", 8, 7, "matching", "matching.xml"),
        module("game", "08", "Trò chơi", "/games", "User", 7, 7, "game", "game.xml"),
        module("notification", "09", "Thông báo", "/notifications", "User", 6, 7, "notification", "notification.xml"),
        module("wordtype", "10", "Quản lý loại từ", "/admin/word-types", "Admin", 11, 7, "wordtype", "word-type.xml"),
        module("vocabulary", "11", "Quản lý từ vựng", "/admin_vocab.php", "Admin", 5, 7, "vocabulary", "vocabulary.xml"),
        module("folder", "12", "Thư mục", "/folders", "User", 8, 7, "folder", "folder.xml"),
        module("library", "13", "Thư viện học tập", "/library", "User", 8, 7, "library", "library.xml"),
        module("customflashcard", "14", "Thẻ ghi nhớ cá nhân", "/my-flashcards", "User", 12, 7, "customflashcard", "custom-flashcard.xml"),
        module("users", "15", "Quản lý người dùng", "/admin/users", "Admin", 15, 7, "users", "users.xml")
    );
    private static final Map<String, TestDefinition> BY_ID = MODULES.stream().collect(Collectors.toUnmodifiableMap(TestDefinition::moduleId, Function.identity()));

    public static List<TestDefinition> all() { return MODULES; }
    public static TestDefinition byId(String id) {
        TestDefinition definition = BY_ID.get(id);
        if (definition == null) throw new IllegalArgumentException("Không tồn tại module: " + id);
        return definition;
    }
    private static TestDefinition module(String id, String number, String name, String route, String role, int tests, int points, String pkg, String suite) {
        return new TestDefinition(id, number, name, route, role, tests, points, pkg, suite);
    }
}
