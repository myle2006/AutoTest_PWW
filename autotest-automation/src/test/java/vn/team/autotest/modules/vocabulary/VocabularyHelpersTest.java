package vn.team.autotest.modules.vocabulary;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.testng.Assert;
import org.testng.annotations.Test;
import vn.team.autotest.modules.vocabulary.model.VocabularyRow;
import vn.team.autotest.modules.vocabulary.util.ImageFixtureFactory;
import vn.team.autotest.modules.vocabulary.util.TableComparator;

public final class VocabularyHelpersTest {
    @Test
    public void createsDecodableJpegAtExactBoundaryBytes() throws Exception {
        // Kiểm tra hai biên thật, tránh fixture chỉ đổi đuôi hoặc sai một byte.
        Path folder = Files.createTempDirectory("vocabulary-image-fixture-");
        Path exact = ImageFixtureFactory.jpeg(folder, "exact.jpg", 5_242_880);
        Path over = ImageFixtureFactory.jpeg(folder, "over.jpg", 5_242_881);
        Assert.assertEquals(Files.size(exact), 5_242_880L);
        Assert.assertEquals(Files.size(over), 5_242_881L);
    }

    @Test
    public void comparesRowsByVocabularyWordInsteadOfPosition() {
        VocabularyRow first = new VocabularyRow("Alpha", "/a/", "Science", "Low", "A", "Example A", "");
        VocabularyRow second = new VocabularyRow("Beta", "/b/", "Business", "High", "B", "Example B", "");
        Assert.assertEquals(TableComparator.compareByWord(List.of(first, second), List.of(second, first)), List.of());
    }
}
