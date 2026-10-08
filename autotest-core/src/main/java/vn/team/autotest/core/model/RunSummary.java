package vn.team.autotest.core.model;

import java.time.Instant;
import java.util.List;

public record RunSummary(String runId, TestStatus status, Instant startedAt, Instant finishedAt,
                         int passed, int failed, int skipped, int score, int maxScore,
                         List<TestResult> results) {}
