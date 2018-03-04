package tdl.record.sourcecode.test;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.revwalk.RevCommit;

import java.util.List;
import java.util.stream.Collectors;

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
        for (Object ignored : commits) {
            count++;
        }
        return count;
    }

    public static List<String> getTags(Git git) throws GitAPIException {
        return git.tagList().call().stream()
                .map(Ref::getName)
                .map(s -> s.replaceAll("refs/tags/", ""))
                .collect(Collectors.toList());
    }

}
