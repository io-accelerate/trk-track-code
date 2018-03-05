package tdl.record.sourcecode.snapshot.file;

import org.eclipse.jgit.lib.Repository;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

class TagManager {

    private final Set<String> existingTags;

    TagManager() {
        existingTags = new HashSet<>();
    }

    static boolean isTag(String tag) {
        return tag != null && tag.trim().length() > 0;
    }

    public void addExisting(List<String> tags) {
        existingTags.addAll(tags);
    }

    String asUniqueTag(String tag) {
        String sanitisedTag = sanitize(tag);
        String selectedTag = ensureUnique(sanitisedTag);
        existingTags.add(selectedTag);
        return selectedTag;
    }

    String asValidTag(String tag) {
        String trimmedTag = sanitize(tag);
        existingTags.add(trimmedTag);
        return trimmedTag;
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

    private static String sanitize(String tag) {
        String normalizedTag = Repository.normalizeBranchName(tag);
        return normalizedTag.substring(0, Math.min(normalizedTag.length(), Segment.TAG_SIZE));
    }
}
