package tdl.record.sourcecode.snapshot.file;

import org.eclipse.jgit.lib.Repository;

import java.util.HashSet;
import java.util.Set;

class TagManager {

    private final Set<String> existingTags;

    public TagManager() {
        existingTags = new HashSet<>();
    }

    static boolean isTag(String tag) {
        return tag != null && tag.trim().length() > 0;
    }

    String asValidTag(String tag) {
        String normalizedTag = Repository.normalizeBranchName(tag);
        String trimmedTag = normalizedTag.substring(0, Math.min(normalizedTag.length(), Segment.TAG_SIZE));

        String selectedTag = ensureUnique(trimmedTag);
        existingTags.add(selectedTag);
        return selectedTag;
    }

    private String ensureUnique(String trimmedTag) {
        String selectedTag = "";
        if (existingTags.contains(trimmedTag)) {
            for (int i = 2; i < 100; i++) {
                String candidate = String.format("%s_%d", trimmedTag, i);
                if (!existingTags.contains(candidate)) {
                    selectedTag = candidate;
                    break;
                }
            }
        }

        if (selectedTag.isEmpty()) {
            selectedTag = trimmedTag;
        }
        return selectedTag;
    }
}
