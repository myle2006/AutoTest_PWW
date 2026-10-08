package vn.team.autotest.core.data;

import java.nio.file.Path;

public interface TestDataReader {
    <T> T read(Path path, Class<T> targetType);
}
