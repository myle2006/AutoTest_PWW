package vn.team.autotest.modules.vocabulary.data;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import vn.team.autotest.modules.vocabulary.model.VocabularyRow;

public final class VocabularyExcelDataset {
    public static final String SHEET_NAME = "Test_Data";
    private final Map<String, String> valuesById;

    private VocabularyExcelDataset(Map<String, String> valuesById) {
        this.valuesById = Map.copyOf(valuesById);
    }

    public static VocabularyExcelDataset read(Path path) {
        if (path == null || !Files.isRegularFile(path)) {
            throw new IllegalArgumentException("Thiếu file Excel expected: PWW_VOCAB_EXPECTED_XLSX");
        }
        try (InputStream input = Files.newInputStream(path); Workbook workbook = WorkbookFactory.create(input)) {
            Sheet sheet = workbook.getSheet(SHEET_NAME);
            if (sheet == null) throw new IllegalArgumentException("File expected thiếu sheet " + SHEET_NAME);
            DataFormatter formatter = new DataFormatter();
            Map<String, String> result = new LinkedHashMap<>();
            for (Row row : sheet) {
                String id = formatter.formatCellValue(row.getCell(0));
                if (!id.isBlank()) result.put(id, formatter.formatCellValue(row.getCell(5)));
            }
            return new VocabularyExcelDataset(result);
        } catch (IOException exception) {
            throw new IllegalArgumentException("Không đọc được Excel expected: " + path, exception);
        }
    }

    public List<String> labels(String datasetId) {
        return Arrays.stream(required(datasetId).split("\\s*\\|\\s*", -1)).toList();
    }

    public List<VocabularyRow> vocabularyRows(String datasetId) {
        return required(datasetId).lines().filter(line -> !line.isBlank()).map(line -> {
            String[] fields = line.split("\\|", -1);
            if (fields.length != 6) throw new IllegalArgumentException(datasetId + " phải có 6 cột, actual=" + fields.length);
            return new VocabularyRow(fields[0], fields[1], fields[2], fields[3], fields[4], fields[5], "");
        }).toList();
    }

    private String required(String datasetId) {
        String value = valuesById.get(datasetId);
        if (value == null || value.isBlank()) throw new IllegalArgumentException("Thiếu dataset approved: " + datasetId);
        return value;
    }
}
