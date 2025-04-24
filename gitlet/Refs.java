package gitlet;

import java.io.File;

import static gitlet.Utils.join;
import static gitlet.Utils.writeContents;

public class Refs {
    /** The current working directory. */
    public static final File CWD = new File(System.getProperty("user.dir"));
    /** The .gitlet directory. */
    static final File GITLET_DIR = join(CWD, ".gitlet");
    public static final File COMMIT_OBJ_DIR = join(GITLET_DIR, "objects");
    public static final File REFS_DIR = join(GITLET_DIR, "refs");
    public static final File HEADS_DIR = join(REFS_DIR, "heads");
    public static final File HEAD_POINT = join(REFS_DIR, "HEAD");

    public static final File MASTER_DIR = join(HEADS_DIR, "master");
    public static final File HEAD = join(GITLET_DIR, "HEAD");
    public static final File ADD_STAGE = join(GITLET_DIR, "addstage");
    public static final File BLOBS_FOLDER = join(COMMIT_OBJ_DIR, "blobs");
    public static final File REMOVE_STAGE = join(GITLET_DIR, "removestage");

    /**
     * Create a file , the path is join(HEAD_DIR, branchName)
     * then write to hashName for its contents
     * @param branchName
     * @param hashName
     */
    public static void saveBranch(String branchName, String hashName) {
        File branchHead = join(HEADS_DIR, branchName);
        writeContents(branchHead, hashName);
    }

    /**
     * In HEAD file write current branch and hash value.
     * Save the point to HEAD into .gitlet/refs/HEAD folder
     * @param branchName
     * @param branchHeadCommitHash
     */
    public static void saveHEAD(String branchName, String branchHeadCommitHash) {
        writeContents(HEAD_POINT, branchName + ":" + branchHeadCommitHash);
    }
}
