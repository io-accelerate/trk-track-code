package tdl.record.sourcecode.snapshot.helpers;

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

    static List<String> getRelativeFilePathList(Path directory) {
        File dir = directory.toFile();
        if (!dir.isDirectory()) {
            throw new RuntimeException("Path " + dir.getName() + " is not sourceCodeProvider");
        }
        Collection<File> files = FileUtils.listFiles(dir, TrueFileFilter.INSTANCE, TrueFileFilter.INSTANCE);

        List<String> result = files
                .stream()
                .map(file -> createRelativeUrl(dir, file))
                .collect(Collectors.toList());

        Collections.sort(result);
        return result;
    }

    private static String createRelativeUrl(File base, File file) {
        return base.toURI().relativize(file.toURI()).getPath();
    }

    static List<String> getUnionRelativeFilePathList(Path directory1, Path directory2) {
        List<String> list1 = getRelativeFilePathList(directory1);
        List<String> list2 = getRelativeFilePathList(directory2);
        Set<String> set = new HashSet<>();
        set.addAll(list1);
        set.addAll(list2);
        List<String> result = new ArrayList<>(set);
        Collections.sort(result);
        return result;
    }

    public static DirectoryPatch diffDirectories(Path original, Path revised) {
        List<String> fileList = getUnionRelativeFilePathList(original, revised);
        Map<String, Patch> map = fileList.stream()
                .collect(Collectors.toMap(
                        p -> p,
                        p -> diffFilesByRelativePath(p, original, revised)
                ));
        Map<String, Patch> filteredMap = map.entrySet().stream()
                .filter(p -> !p.getValue().getDeltas().isEmpty())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        return new DirectoryPatch(filteredMap);
    }

    static Patch diffFiles(Path original, Path revised) throws IOException {
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

    public static void patch(Path directory, DirectoryPatch directoryPatch) {
        directoryPatch.getPatches().forEach((String path, Patch patch) -> {
            File file = directory.resolve(path).toFile();
            patchFile(file, patch);
        });
    }

    private static void patchFile(File file, Patch patch) {
        List<String> lines = tryReadLinesFromFile(file);
        try {
            List<String> newLines = (List<String>) DiffUtils.patch(lines, patch);
            if (newLines.isEmpty()) {
                file.delete();
            } else {
                FileUtils.writeLines(file, newLines, false);
            }
        } catch (PatchFailedException ex) {
            throw new RuntimeException("Cannot patch file: " + file.getName(), ex);
        } catch (IOException ex) {
            throw new RuntimeException("Cannot write file: " + file.getName(), ex);
        }
    }

    private static List<String> tryReadLinesFromFile(File file) {
        if (!file.exists()) {
            return new ArrayList<>();
        }

        if (file.isFile()) {
            try {
                return FileUtils.readLines(file, StandardCharsets.US_ASCII);
            } catch (IOException ex) {
                throw new RuntimeException("Cannot read file: " + file.getName(), ex);
            }
        }
        throw new RuntimeException("File " + file.getName() + "is a sourceCodeProvider");
    }
}
