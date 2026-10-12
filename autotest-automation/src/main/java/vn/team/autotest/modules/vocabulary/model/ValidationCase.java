package vn.team.autotest.modules.vocabulary.model;

public record ValidationCase(String datasetId, String word, boolean expectedValid, boolean duplicateFixture) {
    @Override public String toString() { return datasetId; }
}
