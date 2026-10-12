package vn.team.autotest.modules.vocabulary.model;

import java.nio.file.Path;

public record VocabularyRecord(
        String word,
        String ipa,
        String topic,
        String difficulty,
        String definition,
        String example,
        Path image) {
    public VocabularyRecord withWord(String newWord) {
        return new VocabularyRecord(newWord, ipa, topic, difficulty, definition, example, image);
    }
}
