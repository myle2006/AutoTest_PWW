package vn.team.autotest.core.model;

public record TestDefinition(String moduleId, String number, String name, String route, String role,
                             int testCount, int maxPoints, String packageName, String suiteFile) {}
