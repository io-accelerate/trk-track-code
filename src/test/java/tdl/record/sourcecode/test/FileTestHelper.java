package tdl.record.sourcecode.test;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.IOFileFilter;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class FileTestHelper {

    public static boolean isDirectoryEqualsWithoutGit(Path dir1, Path dir2) {
        return isDirectoryEquals(dir1, dir2,
                (file) -> !file.getAbsolutePath().contains(".git" + File.separator));
    }

    private static boolean isDirectoryEquals(Path dir1, Path dir2, FileFilter filter) {
        String checksum1 = createDirectoryChecksum(dir1, filter);
        String checksum2 = createDirectoryChecksum(dir2, filter);
        return checksum1.equals(checksum2);
    }

    public static boolean isDirectoryEquals(Path dir1, Path dir2) {
        return isDirectoryEquals(dir1, dir2, null);
    }

    public static String createDirectoryChecksum(Path directory) {
        return createDirectoryChecksum(directory, null);
    }

    private static String createDirectoryChecksum(Path directory, FileFilter filter) {
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

    public static void createDirectory(Path targetFolderPath) throws IOException {
        Files.createDirectory(targetFolderPath);
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
        appendStringToFile(dir.resolve(path),text);
    }

    public static void appendStringToFile(Path targetFile, String text) throws IOException {
        FileUtils.writeStringToFile(targetFile.toFile(), text, Charset.defaultCharset(), true);
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
        URL resourceUrl = Objects.requireNonNull(classLoader.getResource(filePathAsResourceName));
        File file = new File(resourceUrl.getFile());

        try {
            return FileUtils.readFileToString(file, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException(
                    "Unable to read file " + filePathAsResourceName + " from resource due to an error", e
            );
        }
    }

    public static List<String> applyIOFileFilter(IOFileFilter filter, Path dir, List<String> filenames) {
        // Identify
        List<Path> targetPaths = filenames.stream()
                .map(dir::resolve)
                .map(Path::toAbsolutePath)
                .collect(Collectors.toList());

        // Create
        targetPaths.forEach(FileTestHelper::createFile);

        // Filter
        return targetPaths.stream()
                .filter(path -> filter.accept(path.toFile()))
                .map(dir::relativize)
                .map(Path::toString)
                .sorted()
                .collect(Collectors.toList());
    }

    private static void createFile(Path targetPath) {
        try {
            FileTestHelper.appendStringToFile(targetPath, "Hello World!");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
