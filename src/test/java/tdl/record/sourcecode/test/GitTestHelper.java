package tdl.record.sourcecode.test;

import java.util.Iterator;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.revwalk.RevCommit;

public class GitTestHelper {

    public static void addAndCommit(Git git) throws GitAPIException {
        git.add()
                .addFilepattern(".")
                .call();
        git.commit()
                .setAll(true)
                .setMessage("Commit")
                .call();
    }

    public static int getCommitCount(Git git) throws GitAPIException {
        Iterable<RevCommit> commits = git.log().call();
        int count = 0;
        Iterator it = commits.iterator();
        while (it.hasNext()) {
            it.next();
            count++;
        }
        return count;
    }
}
