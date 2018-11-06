package tdl.record.sourcecode.test;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.diff.RawText;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Comparator;
import java.util.stream.Collectors;

public class FileTestHelper {

    public static boolean isDirectoryEqualsWithoutGit(Path dir1, Path dir2) {
        return isDirectoryEquals(dir1, dir2,
                (file) -> !file.getAbsolutePath().contains(".git" + File.separator));
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

    private static String createDirectoryChecksum(Path directory, FileFilter filter) throws IOException {
        try {
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            String aggregateChecksum = Files.walk(directory)
                    .filter(file -> file.toFile().isFile())
                    .filter(file -> filter == null || filter.accept(file.toFile()))
                    .sorted(Comparator.comparing(Path::toAbsolutePath))
                    .map(FileTestHelper::createFileDigest)
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
            return Hex.encodeHexString(content.trim().getBytes());
        } catch (IOException ex) {
            return "";
        }
    }

    public static void appendStringToFile(Path dir, String path, String text) throws IOException {
        FileUtils.writeStringToFile(dir.resolve(path).toFile(), text, Charset.defaultCharset(), true);
    }

    public static void changeContentOfFile(Path dir, String path, String text) throws IOException {
        FileUtils.writeStringToFile(dir.resolve(path).toFile(), text, Charset.defaultCharset(), false);
    }

    public static void deleteFile(Path dir, String path) {
        FileUtils.deleteQuietly(dir.resolve(path).toFile());
    }

    public static boolean doesFileExist(Path dir, String path) {
        return dir.resolve(path).toFile().exists();
    }

    public static String readFileFromResource(String filePathAsResourceName) {
        ClassLoader classLoader = FileTestHelper.class.getClassLoader();
        File file = new File(classLoader.getResource(filePathAsResourceName).getFile());

        RawText rawText;
        try {
            rawText = new RawText(file);
            return rawText.getString(0, rawText.size(), false);
        } catch (IOException e) {
            throw new RuntimeException(
                    "Unable to read file " + filePathAsResourceName + " from resource due to an error", e
            );
        }
    }
}
