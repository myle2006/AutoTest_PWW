package vn.team.autotest.core.model;

public record TestResult(String runId, String invocationId, String scenarioId, String testId,
                         String moduleId, String datasetId, String testCase, String testData,
                         String expectedResult, String actualResult, TestStatus status,
                         long durationMillis, String reason, String logs, String evidencePath) {}
