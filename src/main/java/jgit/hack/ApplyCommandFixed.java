package jgit.hack;

import org.eclipse.jgit.api.ApplyResult;
import org.eclipse.jgit.api.GitCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.PatchApplyException;
import org.eclipse.jgit.api.errors.PatchFormatException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.RawText;
import org.eclipse.jgit.internal.JGitText;
import org.eclipse.jgit.lib.FileMode;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.patch.FileHeader;
import org.eclipse.jgit.patch.HunkHeader;
import org.eclipse.jgit.patch.Patch;
import org.eclipse.jgit.util.FileUtils;
import org.eclipse.jgit.util.IO;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.text.MessageFormat;
import java.util.*;

/**
 * WARNING!!!
 * <p>
 * This class is a HACK!!!
 * It had to be copied from the JGit in order to fix a bug in the private method `isNoNewlineAtEndOfFile`
 */
@SuppressWarnings("ALL")
public class ApplyCommandFixed extends GitCommand<ApplyResult> {

    private InputStream in;
    private FilterFileRenames filterFileRenames;

    /**
     * Constructs the command if the patch is to be applied to the index.
     *
     * @param repo
     */
    public ApplyCommandFixed(Repository repo) {
        super(repo);
        filterFileRenames = new FilterFileRenames();
    }

    /**
     * @param in the patch to apply
     * @return this instance
     */
    public ApplyCommandFixed setPatch(InputStream in) {
        checkCallable();
        this.in = in;
        return this;
    }

    /**
     * Executes the {@code ApplyCommand} command with all the options and
     * parameters collected by the setter methods (e.g.
     * {@link #setPatch(InputStream)} of this class. Each instance of this class
     * should only be used for one invocation of the command. Don't call this
     * method twice on an instance.
     *
     * @return an {@link ApplyResult} object representing the command result
     * @throws GitAPIException
     * @throws PatchFormatException
     * @throws PatchApplyException
     */
    @Override
    public ApplyResult call() throws GitAPIException, PatchFormatException,
            PatchApplyException {
        checkCallable();
        ApplyResult r = new ApplyResult();
        try {
            final Patch p = new Patch();
            try {
                p.parse(in);
            } finally {
                in.close();
            }
            if (!p.getErrors().isEmpty())
                throw new PatchFormatException(p.getErrors());


            List<FileHeader> files = filterFileRenames.apply(p);
            for (FileHeader fh : files) {
                DiffEntry.ChangeType type = fh.getChangeType();
                File f = null;
                switch (type) {
                    case ADD:
                        f = getFile(fh.getNewPath(), true);
                        apply(f, fh);
                        break;
                    case MODIFY:
                        f = getFile(fh.getOldPath(), false);
                        apply(f, fh);
                        break;
                    case DELETE:
                        f = getFile(fh.getOldPath(), false);
                        if (!f.delete())
                            throw new PatchApplyException(MessageFormat.format(
                                    JGitText.get().cannotDeleteFile, f));
                        break;
                    case RENAME:
                        f = getFile(fh.getOldPath(), false);
                        File dest = getFile(fh.getNewPath(), false);
                        try {
                            FileUtils.rename(f, dest,
                                    StandardCopyOption.ATOMIC_MOVE);
                        } catch (IOException e) {
                            throw new PatchApplyException(MessageFormat.format(
                                    JGitText.get().renameFileFailed, f, dest), e);
                        }
                        break;
                    case COPY:
                        f = getFile(fh.getOldPath(), false);
                        byte[] bs = IO.readFully(f);
                        FileOutputStream fos = new FileOutputStream(getFile(
                                fh.getNewPath(),
                                true));
                        try {
                            fos.write(bs);
                        } finally {
                            fos.close();
                        }
                }
                r.addUpdatedFile(f);
            }
        } catch (IOException e) {
            throw new PatchApplyException(MessageFormat.format(
                    JGitText.get().patchApplyException, e.getMessage()), e);
        }
        setCallable(false);
        return r;
    }

    private File getFile(String path, boolean create)
            throws PatchApplyException {
        File f = new File(getRepository().getWorkTree(), path);
        if (create)
            try {
                if (f.exists()) {
                    Files.write(f.toPath(), new byte[0], StandardOpenOption.TRUNCATE_EXISTING);
                } else {
                    File parent = f.getParentFile();
                    FileUtils.mkdirs(parent, true);
                    FileUtils.createNewFile(f);
                }
            } catch (IOException e) {
                throw new PatchApplyException(MessageFormat.format(
                        JGitText.get().createNewFileFailed, f), e);
            }
        return f;
    }

    /**
     * @param f
     * @param fh
     * @throws IOException
     * @throws PatchApplyException
     */
    private void apply(File f, FileHeader fh)
            throws IOException, PatchApplyException {
        RawText rt = new RawText(f);
        List<String> oldLines = new ArrayList<>(rt.size());
        for (int i = 0; i < rt.size(); i++)
            oldLines.add(rt.getString(i));

        // Fixes the elusive issue with missing new-line at the end of the target file
//        if (!rt.isMissingNewlineAtEnd())
//            oldLines.add(""); //$NON-NLS-1$

        List<String> newLines = new ArrayList<>(oldLines);
        for (HunkHeader hh : fh.getHunks()) {

            byte[] b = new byte[hh.getEndOffset() - hh.getStartOffset()];
            System.arraycopy(hh.getBuffer(), hh.getStartOffset(), b, 0,
                    b.length);
            RawText hrt = new RawText(b);

            List<String> hunkLines = new ArrayList<>(hrt.size());
            for (int i = 0; i < hrt.size(); i++)
                hunkLines.add(hrt.getString(i));
            int counterRelatedToTheHunk = 0;
            for (int j = 1; j < hunkLines.size(); j++) {
                String hunkLine = hunkLines.get(j);
                int atThisIndex = hh.getNewStartLine() - 1 + counterRelatedToTheHunk;
                switch (hunkLine.charAt(0)) {
                    case ' ':
                        ensureLineContentMatchesDestination(hunkLine.substring(1), newLines, hh, counterRelatedToTheHunk);
                        counterRelatedToTheHunk++;
                        break;
                    case '-':
                        if (hh.getNewStartLine() == 0) {
                            newLines.clear();
                        } else {
                            ensureLineContentMatchesDestination(hunkLine.substring(1), newLines, hh, counterRelatedToTheHunk);
                            newLines.remove(atThisIndex);
                        }
                        break;
                    case '+':
                        newLines.add(atThisIndex, hunkLine.substring(1));
                        counterRelatedToTheHunk++;
                        break;
                }
            }
        }
        if (!isNoNewlineAtEndOfFile(fh))
            newLines.add(""); //$NON-NLS-1$
        if (!rt.isMissingNewlineAtEnd())
            oldLines.add(""); //$NON-NLS-1$
        if (!isChanged(oldLines, newLines))
            return; // don't touch the file
        StringBuilder sb = new StringBuilder();
        for (String l : newLines) {
            // don't bother handling line endings - if it was windows, the \r is
            // still there!
            sb.append(l).append('\n');
        }
        if (sb.length() > 0) {
            sb.deleteCharAt(sb.length() - 1);
        }
        FileWriter fw = new FileWriter(f);
        fw.write(sb.toString());
        fw.close();

        getRepository().getFS().setExecute(f, fh.getNewMode() == FileMode.EXECUTABLE_FILE);
    }

    private static boolean isChanged(List<String> ol, List<String> nl) {
        if (ol.size() != nl.size())
            return true;
        for (int i = 0; i < ol.size(); i++)
            if (!ol.get(i).equals(nl.get(i)))
                return true;
        return false;
    }

    private void ensureLineContentMatchesDestination(String expected,
                                                     List<String> newLines,
                                                     HunkHeader hh,
                                                     int pos) throws PatchApplyException {
        //BUG FIX - if the patch is bigger than the destination, ignore the comparison checks, this prevents some nasty null pointers
        int destinationIndex = hh.getNewStartLine() - 1 + pos;
        if (destinationIndex < newLines.size() && !newLines.get(destinationIndex).equals(
                expected)) {
            throw new PatchApplyException(MessageFormat.format(
                    JGitText.get().patchApplyException, hh));
        }
    }

    private boolean isNoNewlineAtEndOfFile(FileHeader fh) {
        //BUG FIX - The following check prevents a null pointer or an array index out of bounds
        if (fh.getHunks() != null && fh.getHunks().size() > 0) {
            HunkHeader lastHunk = fh.getHunks().get(fh.getHunks().size() - 1);
            RawText lhrt = new RawText(lastHunk.getBuffer());
            return lhrt.getString(lhrt.size() - 1).equals(
                    "\\ No newline at end of file"); //$NON-NLS-1$
        }

        return false;
    }
}
