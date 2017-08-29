package net.petrabarus.java.record_dir_and_upload.diff;

import difflib.DiffUtils;
import difflib.Patch;
import difflib.PatchFailedException;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;

public class DirectoryDiffUtils {

    public static List<String> getRelativeFilePathList(Path directory) {
        File dir = directory.toFile();
        if (!dir.isDirectory()) {
            throw new RuntimeException("Path " + dir.getName() + " is not directory");
        }
        Collection<File> files = FileUtils.listFiles(dir, TrueFileFilter.INSTANCE, TrueFileFilter.INSTANCE);

        List<String> result = files
                .stream()
                .map(file -> {
                    return createRelativeUrl(dir, file);
                })
                .collect(Collectors.toList());

        Collections.sort(result);
        return result;
    }

    private static String createRelativeUrl(File base, File file) {
        return base.toURI().relativize(file.toURI()).getPath();
    }

    public static List<String> getUnionRelativeFilePathList(Path directory1, Path directory2) {
        List<String> list1 = getRelativeFilePathList(directory1);
        List<String> list2 = getRelativeFilePathList(directory2);
        Set<String> set = new HashSet<>();
        set.addAll(list1);
        set.addAll(list2);
        List<String> result = new ArrayList<>(set);
        Collections.sort(result);
        return result;
    }

    public static Map<String, Patch> diffDirectories(Path original, Path revised) {
        List<String> fileList = getUnionRelativeFilePathList(original, revised);
        Map<String, Patch> map = fileList.stream()
                .collect(Collectors.toMap(
                        p -> p,
                        p -> diffFilesByRelativePath(p, original, revised)
                ));
        return map;
    }

    public static Patch diffFiles(Path original, Path revised) throws IOException {
        File file1 = original.toFile();
        throwIsNotValidForDiff(file1);

        File file2 = revised.toFile();
        throwIsNotValidForDiff(file2);

        List<String> contents1 = getContentsFromFile(file1);
        List<String> contents2 = getContentsFromFile(file2);

        return DiffUtils.diff(contents1, contents2);
    }

    private static Patch diffFilesByRelativePath(String path, Path originalDir, Path revisedDir) {
        Path originalFile = originalDir.resolve(path);
        Path revisedFile = revisedDir.resolve(path);
        try {
            return diffFiles(originalFile, revisedFile);
        } catch (IOException ex) {
            return new Patch();
        }
    }

    private static List<String> getContentsFromFile(File file) throws IOException {
        if (!file.exists()) {
            return new ArrayList<>();
        }
        return FileUtils.readLines(file, StandardCharsets.US_ASCII);
    }

    private static void throwIsNotValidForDiff(File file) throws IOException {
        boolean isValid = file.isFile() || !file.exists();
        if (!isValid) {
            throw new IOException("Path " + file.getName() + " is not file");
        }
    }

    public static void patch(Path directory, Map<String, Patch> patches) {
        patches.forEach((String path, Patch patch) -> {
            File file = directory.resolve(path).toFile();
            List<String> lines;
            if (file.isFile()) {
                try {
                    lines = FileUtils.readLines(file, StandardCharsets.US_ASCII);
                } catch (IOException ex) {
                    throw new RuntimeException("Cannot read file: " + file.getName(), ex);
                }
            } else if (!file.exists()) {
                lines = new ArrayList<>();
            } else {
                throw new RuntimeException("File " + file.getName() + "is a directory");
            }
            try {
                List<String> newLines = (List<String>) DiffUtils.patch(lines, patch);
                if (newLines.size() == 0) {
                    file.delete();
                } else {
                    FileUtils.writeLines(file, newLines, false);
                }
            } catch (PatchFailedException ex) {
                throw new RuntimeException("Cannot patch file: " + file.getName(), ex);
            } catch (IOException ex) {
                throw new RuntimeException("Cannot write file: " + file.getName(), ex);
            }
        });
    }
}
