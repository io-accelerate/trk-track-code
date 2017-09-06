package net.petrabarus.java.record_dir_and_upload.snapshot.helpers;

import difflib.Patch;
import java.io.Serializable;
import java.util.Map;

public class DirectoryPatch implements Serializable {

    private Map<String, Patch> patches;

    public DirectoryPatch(Map<String, Patch> patches) {
        this.patches = patches;
    }

    public Map<String, Patch> getPatches() {
        return patches;
    }
}
