package net.petrabarus.java.record_dir_and_upload.test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.io.FileUtils;

public class FileTestHelper {

    public static boolean isDirectoryEquals(Path dir1, Path dir2) {
        try {
            String checksum1 = createDirectoryChecksum(dir1);
            String checksum2 = createDirectoryChecksum(dir2);
            return checksum1.equals(checksum2);
        } catch (IOException ex) {
            return false;
        }
    }

    public static String createDirectoryChecksum(Path directory) throws IOException {
        try {
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            String aggregateChecksum = Files.walk(directory)
                    .filter(file -> file.toFile().isFile())
                    .sorted((file1, file2)
                            -> file1.toAbsolutePath()
                            .compareTo(file2.toAbsolutePath())
                    )
                    .map(file -> createFileDigest(file))
                    .collect(Collectors.joining());
            byte[] checksum = md5.digest(aggregateChecksum.getBytes());
            return Hex.encodeHexString(checksum);
        } catch (NoSuchAlgorithmException | IOException ex) {
            return "";
        }
    }

    private static String createFileDigest(Path file) {
        try {
            String content = FileUtils.readFileToString(
                    file.toFile(),
                    StandardCharsets.UTF_8
            );
            //Intentionally trim whitespace
            String hexdigest = Hex.encodeHexString(content.trim().getBytes());
            return hexdigest;
        } catch (IOException ex) {
            return "";
        }
    }
}
