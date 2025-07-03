package ca.fxco.gitmergepipeline.utils;

import org.eclipse.jgit.annotations.Nullable;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.util.io.DisabledOutputStream;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class GitUtils {

    public static RevCommit getCommit(Repository repo, String branch) throws IOException {
        ObjectId branchId = repo.resolve(branch);
        if (branchId == null) {
            throw new RuntimeException("Branch not found: " + branch);
        }
        try (RevWalk walk = new RevWalk(repo)) {
            return walk.parseCommit(branchId);
        }
    }

    public static RevCommit getCommonAncestor(Repository repo, String branch1, String branch2,
                                              @Nullable String baseBranch)
            throws IOException {
        try (RevWalk walk = new RevWalk(repo)) {
            RevCommit c1 = getCommit(repo, branch1);
            RevCommit c2 = getCommit(repo, branch2);

            walk.setRevFilter(org.eclipse.jgit.revwalk.filter.RevFilter.MERGE_BASE);
            walk.markStart(walk.parseCommit(c1));
            walk.markStart(walk.parseCommit(c2));
            RevCommit base = walk.next();

            if (baseBranch != null) {
                return getCommit(repo, baseBranch);
            }

            return base;
        }
    }

    public static List<DiffEntry> getChangedFiles(Repository repo, RevCommit base, RevCommit c1, RevCommit c2)
            throws IOException, GitAPIException {
        List<DiffEntry> diffs = new ArrayList<>();

        try (Git git = new Git(repo)) {
            ObjectId baseTree = base != null ? base.getTree() : null;
            ObjectId tree1 = c1.getTree();
            ObjectId tree2 = c2.getTree();

            try (DiffFormatter df = new DiffFormatter(DisabledOutputStream.INSTANCE)) {
                df.setRepository(repo);
                df.setDetectRenames(true);

                CanonicalTreeParser baseParser = new CanonicalTreeParser();
                //CanonicalTreeParser newParser = new CanonicalTreeParser();

                List<DiffEntry> diff1;
                List<DiffEntry> diff2 = new ArrayList<>();

                if (baseTree != null) {
                    baseParser.reset(repo.newObjectReader(), baseTree);
                    CanonicalTreeParser tree1Parser = new CanonicalTreeParser();
                    tree1Parser.reset(repo.newObjectReader(), tree1);
                    diff1 = df.scan(baseParser, tree1Parser);

                    baseParser.reset(repo.newObjectReader(), baseTree);
                    CanonicalTreeParser tree2Parser = new CanonicalTreeParser();
                    tree2Parser.reset(repo.newObjectReader(), tree2);
                    diff2 = df.scan(baseParser, tree2Parser);
                } else {
                    CanonicalTreeParser tree1Parser = new CanonicalTreeParser();
                    tree1Parser.reset(repo.newObjectReader(), tree1);
                    CanonicalTreeParser tree2Parser = new CanonicalTreeParser();
                    tree2Parser.reset(repo.newObjectReader(), tree2);
                    diff1 = df.scan(tree1Parser, tree2Parser);
                }

                for (DiffEntry diff : diff1) {
                    if (!diffs.contains(diff)) {
                        diffs.add(diff);
                    }
                }
                for (DiffEntry diff : diff2) {
                    if (!diffs.contains(diff)) {
                        diffs.add(diff);
                    }
                }
            }
        }

        return diffs;
    }

    public static Path checkoutFile(Repository repo, RevCommit commit, String path) throws IOException {
        Path tempFile = Files.createTempFile("mergefile-", "-" + path.replace('/', '_'));
        try (TreeWalk treeWalk = TreeWalk.forPath(repo, path, commit.getTree())) {
            if (treeWalk == null) {
                return tempFile; // File doesn't exist in this commit
            }
            ObjectId blobId = treeWalk.getObjectId(0);
            Files.write(tempFile, repo.open(blobId).getBytes());
        }
        return tempFile;
    }
}
