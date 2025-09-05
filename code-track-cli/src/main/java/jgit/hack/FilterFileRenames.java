package jgit.hack;

import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.patch.FileHeader;
import org.eclipse.jgit.patch.Patch;

import java.util.*;
import java.util.stream.Collectors;

public class FilterFileRenames {

    public List<FileHeader> apply(Patch p) {
        // Detect renames
        List<? extends FileHeader> files = p.getFiles();
        Map<String, Set<DiffEntry.ChangeType>> operationsPerFile = new HashMap<>();
        for (FileHeader fh : files) {
            String targetPath = getTargetPath(fh);
            DiffEntry.ChangeType type = fh.getChangeType();
            Set<DiffEntry.ChangeType> setForFile = operationsPerFile.getOrDefault(targetPath, new HashSet<>());
            setForFile.add(type);
            operationsPerFile.put(targetPath, setForFile);
        }

        // Collect paths the have been renamed
        Set<String> renames = operationsPerFile.entrySet().stream().filter(entry ->
                entry.getValue().contains(DiffEntry.ChangeType.ADD) &&
                        entry.getValue().contains(DiffEntry.ChangeType.DELETE))
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());

        //Filter DELETE operations for given paths
        List<FileHeader> filteredFiles = new ArrayList<>();
        for (FileHeader fh : files) {
            String targetPath = getTargetPath(fh);
            DiffEntry.ChangeType type = fh.getChangeType();

            //noinspection StatementWithEmptyBody
            if (renames.contains(targetPath) && type == DiffEntry.ChangeType.DELETE) {
                // skip
            } else {
                filteredFiles.add(fh);
            }
        }
        return filteredFiles;
    }

    private static String getTargetPath(FileHeader fh) {
        String targetPath;
        {
            DiffEntry.ChangeType type = fh.getChangeType();
            if (type == DiffEntry.ChangeType.ADD) {
                targetPath = fh.getNewPath();
            } else {
                targetPath = fh.getOldPath();
            }
        }
        return targetPath.toLowerCase();
    }
}
