package vn.team.autotest.core.model;

public record TestResult(String runId, String testId, String moduleId, TestStatus status,
                         long durationMillis, String error, String evidencePath) {}
