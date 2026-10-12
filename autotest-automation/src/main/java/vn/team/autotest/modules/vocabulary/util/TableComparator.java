package vn.team.autotest.modules.vocabulary.util;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import vn.team.autotest.modules.vocabulary.model.VocabularyRow;

public final class TableComparator {
    private TableComparator() {}

    public static List<String> compareByWord(List<VocabularyRow> expected, List<VocabularyRow> actual) {
        Map<String, VocabularyRow> expectedByWord = unique(expected, "expected");
        Map<String, VocabularyRow> actualByWord = unique(actual, "actual");
        java.util.ArrayList<String> differences = new java.util.ArrayList<>();
        expectedByWord.forEach((word, expectedRow) -> {
            VocabularyRow actualRow = actualByWord.get(word);
            if (actualRow == null) differences.add(word + ": thiếu bản ghi");
            else if (!sameData(expectedRow, actualRow)) differences.add(word + ": sai dữ liệu, actual=" + actualRow);
        });
        actualByWord.keySet().stream().filter(word -> !expectedByWord.containsKey(word))
                .forEach(word -> differences.add(word + ": bản ghi ngoài tập expected"));
        return List.copyOf(differences);
    }

    private static Map<String, VocabularyRow> unique(List<VocabularyRow> rows, String source) {
        try {
            return rows.stream().collect(Collectors.toMap(VocabularyRow::word, Function.identity()));
        } catch (IllegalStateException duplicate) {
            throw new IllegalArgumentException("Trùng khóa word trong " + source, duplicate);
        }
    }

    private static boolean sameData(VocabularyRow left, VocabularyRow right) {
        return left.word().equals(right.word()) && left.ipa().equals(right.ipa())
                && left.topic().equals(right.topic()) && left.difficulty().equals(right.difficulty())
                && left.definition().equals(right.definition()) && left.example().equals(right.example());
    }
}
