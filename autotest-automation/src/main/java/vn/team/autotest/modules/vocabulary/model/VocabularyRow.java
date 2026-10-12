package vn.team.autotest.modules.vocabulary.model;

public record VocabularyRow(
        String word,
        String ipa,
        String topic,
        String difficulty,
        String definition,
        String example,
        String imageSource) {}
