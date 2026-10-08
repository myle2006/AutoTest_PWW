package vn.team.autotest.core.data;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.file.Path;

public final class JsonTestDataReader implements TestDataReader {
    private final ObjectMapper objectMapper;

    public JsonTestDataReader() {
        this(new ObjectMapper());
    }

    public JsonTestDataReader(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public <T> T read(Path path, Class<T> targetType) {
        try {
            return objectMapper.readValue(path.toFile(), targetType);
        } catch (IOException exception) {
            throw new IllegalArgumentException("Không đọc được dữ liệu kiểm thử: " + path, exception);
        }
    }
}
