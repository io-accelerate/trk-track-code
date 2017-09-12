package tdl.record.sourcecode.test;

import java.io.FileFilter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.stream.Collectors;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.io.FileUtils;

public class FileTestHelper {

    public static boolean isDirectoryEqualsWithoutGit(Path dir1, Path dir2) {
        FileFilter filter = (file) -> {
            return !file.getAbsolutePath().contains(".git/");
        };
        return isDirectoryEquals(dir1, dir2, filter);
    }

    public static boolean isDirectoryEquals(Path dir1, Path dir2, FileFilter filter) {
        try {
//            System.out.println(dir1);
//            System.out.println("===========================");
            String checksum1 = createDirectoryChecksum(dir1, filter);
//            System.out.println(dir2);
//            System.out.println("===========================");
            String checksum2 = createDirectoryChecksum(dir2, filter);
            return checksum1.equals(checksum2);
        } catch (IOException ex) {
            return false;
        }
    }

    public static boolean isDirectoryEquals(Path dir1, Path dir2) {
        return isDirectoryEquals(dir1, dir2, null);
    }

    public static String createDirectoryChecksum(Path directory) throws IOException {
        return createDirectoryChecksum(directory, null);
    }

    public static String createDirectoryChecksum(Path directory, FileFilter filter) throws IOException {
        try {
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            String aggregateChecksum = Files.walk(directory)
                    .filter(file -> file.toFile().isFile())
                    .filter(file -> filter == null ? true : filter.accept(file.toFile()))
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
        //System.out.println(file);
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

    public static void appendStringToFile(Path dir, String path, String text) throws IOException {
        FileUtils.writeStringToFile(dir.resolve(path).toFile(), text, Charset.defaultCharset(), true);
    }
}
