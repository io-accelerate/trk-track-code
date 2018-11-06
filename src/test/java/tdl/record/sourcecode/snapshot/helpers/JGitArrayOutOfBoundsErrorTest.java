package tdl.record.sourcecode.snapshot.helpers;

import org.eclipse.jgit.api.Git;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import tdl.record.sourcecode.test.FileTestHelper;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;

import static org.junit.Assert.assertTrue;
import static tdl.record.sourcecode.snapshot.helpers.GitHelper.addAndCommit;

public class JGitArrayOutOfBoundsErrorTest {

    private static final String lineSeparator = System.lineSeparator();
    private static final StringBuilder BLOCK_OF_CODE_WITH_LAST_LINE_REMOVED =
            new StringBuilder()
                .append(lineSeparator)
                .append(lineSeparator)
                .append("# noinspection PyUnusedLocal" + lineSeparator)
                .append("# skus = unicode string" + lineSeparator)
                .append("def checkout(skus):" + lineSeparator)
                .append("    skus_lookup = {" + lineSeparator)
                .append("        'A': {" + lineSeparator)
                .append("            'price': 50," + lineSeparator)
                .append("            'offer': {" + lineSeparator)
                .append("                'items': 3," + lineSeparator)
                .append("                'price': 130" + lineSeparator)
                .append("            }" + lineSeparator)
                .append("        }," + lineSeparator)
                .append("        'B': {" + lineSeparator)
                .append("            'price': 50," + lineSeparator)
                .append("            'offer': {" + lineSeparator)
                .append("                'items': 2," + lineSeparator)
                .append("                'price': 45" + lineSeparator)
                .append("            }" + lineSeparator)
                .append("        }," + lineSeparator)
                .append("        'C': {" + lineSeparator)
                .append("            'price': 20" + lineSeparator)
                .append("        }," + lineSeparator)
                .append("        'D': {" + lineSeparator)
                .append("            'price': 15" + lineSeparator)
                .append("        }," + lineSeparator)
                .append(lineSeparator)
                .append("    }" + lineSeparator)
                .append("    prices = []" + lineSeparator)
                .append("    skus = skus.upper()" + lineSeparator)
                .append("    for s in set(skus):" + lineSeparator)
                .append("        quantity = skus.count(s)" + lineSeparator)
                .append("        item = skus_lookup[s]" + lineSeparator)
                .append("        item_price = 0" + lineSeparator)
                .append("        if s in skus_lookup:" + lineSeparator)
                .append("            if 'offer' in item:" + lineSeparator)
                .append("                if quantity >= item['offer']['items']:" + lineSeparator)
                .append("                    offer_items = quantity / item['offer']['items']" + lineSeparator)
                .append("                    normal_items = quantity % item['offer']['items']" + lineSeparator)
                .append("                    offer_price = offer_items * item['offer']['price']" + lineSeparator)
                .append("                    normal_price = normal_items * item['price']" + lineSeparator)
                .append("                    item_price = sum([normal_price, offer_price])" + lineSeparator)
                .append("                else:" + lineSeparator)
                .append("                    item_price = item['price'] * quantity" + lineSeparator)
                .append("            else:" + lineSeparator)
                .append("                item_price = item['price'] * quantity" + lineSeparator)
                .append("            prices.append(item_price)" + lineSeparator)
                .append("        else:" + lineSeparator)
                .append("            prices.append(-1)" + lineSeparator)
                .append("    return sum(prices)" + lineSeparator);

    private static final StringBuilder
            BLOCK_OF_CODE_WITH_LAST_EMPTY_LINE = new StringBuilder(BLOCK_OF_CODE_WITH_LAST_LINE_REMOVED).append(lineSeparator);

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Test
    public void applyPatchToSourceWhenTheLastLineAlreadyAppliedBeforeHunkCanBeApplied() throws Exception {
        File sourceDirectory = folder.newFolder();
        File targetDirectory = folder.newFolder();

        Git sourceGitRepo = Git.init().setDirectory(sourceDirectory).call();
        addAndCommit(sourceGitRepo);

        Git targetGitRepo = Git.init().setDirectory(targetDirectory).call();
        addAndCommit(targetGitRepo);

        FileTestHelper.appendStringToFile(sourceDirectory.toPath(), "file1.txt", BLOCK_OF_CODE_WITH_LAST_EMPTY_LINE.toString());
        FileTestHelper.appendStringToFile(targetDirectory.toPath(), "file1.txt", BLOCK_OF_CODE_WITH_LAST_EMPTY_LINE.toString());

        addAndCommit(sourceGitRepo);
        addAndCommit(targetGitRepo);

        FileTestHelper.changeContentOfFile(sourceDirectory.toPath(), "file1.txt", BLOCK_OF_CODE_WITH_LAST_LINE_REMOVED.toString());

        addAndCommit(sourceGitRepo);

        byte[] exportedDiffAsByteArray;
        try (ByteArrayOutputStream exportDiffAsStream = new ByteArrayOutputStream()) {
            GitHelper.exportDiff(sourceGitRepo, exportDiffAsStream);
            exportedDiffAsByteArray = exportDiffAsStream.toByteArray();
        }

        // >>>>>>>>>>>>> This block is necessary to be able to reproduce the issue 25 at hand <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
        // Root cause of the Issue 25: the user is some how pushing code after removing the last empty line (dev-sourcecode-record isn't able
        // to pick this up), which is that nullifying the purpose of applying he hunk to that part of the code leading to an ArrayIndexOutOfBound
        // exception (java.lang.AssertionError: Should not have thrown exception: Index: 50, Size: 50) WITHOUT the fix
        // Possible causes:
        //   IDE post commit action - to remove blank lines at the end of the file and push commits
        //   Post commit git hook action - to remove blank lines at the end of the file and push commits
        FileTestHelper.changeContentOfFile(targetDirectory.toPath(), "file1.txt", BLOCK_OF_CODE_WITH_LAST_LINE_REMOVED.toString());
        addAndCommit(targetGitRepo);
        // <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<

        try (ByteArrayInputStream patch = new ByteArrayInputStream(exportedDiffAsByteArray)) {
            try {
                GitHelper.applyDiff(targetGitRepo, patch);
                assertTrue(FileTestHelper.isDirectoryEqualsWithoutGit(
                        sourceDirectory.toPath(),
                        targetDirectory.toPath())
                );
            } catch (Exception ex) {
                Assert.fail("Should not have thrown exception: " + ex.getMessage());
            }
        }
    }
}
