package vn.team.autotest.modules.vocabulary.model;

public record ImageValidationCase(String datasetId, FileKind kind, long sizeBytes, boolean expectedValid) {
    public enum FileKind { JPEG, PDF }
    @Override public String toString() { return datasetId; }
}
