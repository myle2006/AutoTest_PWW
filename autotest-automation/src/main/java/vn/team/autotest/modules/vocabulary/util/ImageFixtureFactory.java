package vn.team.autotest.modules.vocabulary.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;

public final class ImageFixtureFactory {
    private static final byte[] ONE_PIXEL_JPEG = Base64.getDecoder().decode(
            "/9j/4AAQSkZJRgABAQAASABIAAD/4QBMRXhpZgAATU0AKgAAAAgAAYdpAAQAAAABAAAAGgAA"
            + "AAAAA6ABAAMAAAABAAEAAKACAAQAAAABAAAAAaADAAQAAAABAAAAAQAAAAD/7QA4UGhvdG9z"
            + "aG9wIDMuMAA4QklNBAQAAAAAAAA4QklNBCUAAAAAABDUHYzZjwCyBOmACZjs+EJ+/8AAEQgA"
            + "AQABAwEiAAIRAQMRAf/EAB8AAAEFAQEBAQEBAAAAAAAAAAABAgMEBQYHCAkKC//EALUQAAIB"
            + "AwMCBAMFBQQEAAABfQECAwAEEQUSITFBBhNRYQcicRQygZGhCCNCscEVUtHwJDNicoIJChYX"
            + "GBkaJSYnKCkqNDU2Nzg5OkNERUZHSElKU1RVVldYWVpjZGVmZ2hpanN0dXZ3eHl6g4SFhoeI"
            + "iYqSk5SVlpeYmZqio6Slpqeoqaqys7S1tre4ubrCw8TFxsfIycrS09TV1tfY2drh4uPk5ebn"
            + "6Onq8fLz9PX29/j5+v/EAB8BAAMBAQEBAQEBAQEAAAAAAAABAgMEBQYHCAkKC//EALURAAIB"
            + "AgQEAwQHBQQEAAECdwABAgMRBAUhMQYSQVEHYXETIjKBCBRCkaGxwQkjM1LwFWJy0QoWJDTh"
            + "JfEXGBkaJicoKSo1Njc4OTpDREVGR0hJSlNUVVZXWFlaY2RlZmdoaWpzdHV2d3h5eoKDhIWG"
            + "h4iJipKTlJWWl5iZmqKjpKWmp6ipqrKztLW2t7i5usLDxMXGx8jJytLT1NXW19jZ2uLj5OXm"
            + "5+jp6vLz9PX29/j5+v/bAEMAAgICAgICAwICAwUDAwMFBgUFBQUGCAYGBgYGCAoICAgICAgKC"
            + "goKCgoKCgwMDAwMDA4ODg4ODw8PDw8PDw8PD//bAEMBAgICBAQEBwQEBxALCQsQEBAQEBAQ"
            + "EBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEP/dAAQAAf/aAAwDAQAC"
            + "EQMRAD8A/fyiiigD/9k=");
    private ImageFixtureFactory() {}

    public static Path jpeg(Path folder, String fileName, long exactBytes) {
        try {
            Files.createDirectories(folder);
            byte[] base = ONE_PIXEL_JPEG;
            if (exactBytes < base.length) throw new IllegalArgumentException("Kích thước JPEG yêu cầu quá nhỏ");

            ByteArrayOutputStream output = new ByteArrayOutputStream(Math.toIntExact(exactBytes));
            output.write(base, 0, base.length - 2); // Giữ marker EOI ở cuối tệp.
            long remaining = exactBytes - base.length;
            while (remaining > 0) {
                if (remaining < 4) throw new IllegalArgumentException("Không thể tạo JPEG đúng biên này");
                int payload = (int) Math.min(65_533, remaining - 4);
                if (remaining - (payload + 4) > 0 && remaining - (payload + 4) < 4) payload -= 4;
                output.write(0xFF);
                output.write(0xFE); // COM là segment hợp lệ của JPEG, không phải đổi đuôi tệp.
                int segmentLength = payload + 2;
                output.write((segmentLength >>> 8) & 0xFF);
                output.write(segmentLength & 0xFF);
                output.write(new byte[payload]);
                remaining -= payload + 4L;
            }
            output.write(0xFF);
            output.write(0xD9);
            Path target = folder.resolve(fileName);
            Files.write(target, output.toByteArray());
            byte[] written = Files.readAllBytes(target);
            if (Files.size(target) != exactBytes || written[0] != (byte) 0xFF || written[1] != (byte) 0xD8
                    || written[written.length - 2] != (byte) 0xFF || written[written.length - 1] != (byte) 0xD9) {
                throw new IllegalStateException("Fixture JPEG không hợp lệ hoặc sai kích thước");
            }
            return target.toAbsolutePath();
        } catch (IOException exception) {
            throw new IllegalStateException("Không tạo được fixture JPEG", exception);
        }
    }

    public static Path pdf(Path folder, String fileName, long exactBytes) {
        try {
            Files.createDirectories(folder);
            byte[] prefix = ("%PDF-1.4\n1 0 obj<</Type/Catalog/Pages 2 0 R>>endobj\n"
                    + "2 0 obj<</Type/Pages/Count 0/Kids[]>>endobj\n").getBytes(StandardCharsets.US_ASCII);
            byte[] suffix = "\ntrailer<</Root 1 0 R>>\n%%EOF\n".getBytes(StandardCharsets.US_ASCII);
            if (exactBytes < prefix.length + suffix.length) throw new IllegalArgumentException("Kích thước PDF quá nhỏ");
            ByteArrayOutputStream output = new ByteArrayOutputStream(Math.toIntExact(exactBytes));
            output.write(prefix);
            output.write(new byte[Math.toIntExact(exactBytes - prefix.length - suffix.length)]);
            output.write(suffix);
            Path target = folder.resolve(fileName);
            Files.write(target, output.toByteArray());
            return target.toAbsolutePath();
        } catch (IOException exception) {
            throw new IllegalStateException("Không tạo được fixture PDF", exception);
        }
    }
}
