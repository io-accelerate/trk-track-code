package tdl.record.sourcecode.test;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;

public class GitTestHelper {

    public static void addAndCommit(Git git) throws GitAPIException {
        git.add()
                .addFilepattern(".")
                .call();
        git.commit().setAll(true).setMessage("Commit").call();
    }
}
