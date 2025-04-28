package gitlet;

import java.io.File;

import static gitlet.Utils.*;

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
     * 创建一个文件，路径为 join(HEADS_DIR, branchName)
     * 向其中写入 hashName
     * @param branchName
     * @param hashName
     */
    public static void saveBranch(String branchName, String hashName) {
        File branchHead = join(HEADS_DIR, branchName);
        writeContents(branchHead, hashName);
    }

    /**
     * 在 HEAD 文件中写入当前branch的hash值
     *
     * @param branchName
     * @param branchHeadCommitHash
     */
    public static void saveHEAD(String branchName, String branchHeadCommitHash) {
        writeContents(HEAD_POINT, branchName + ":" + branchHeadCommitHash);
    }

    /**
     * 从 HEAD 文件中直接获取当前 branch 的名字
     */
    public static String getHeadBranchName(){
        String headContent = readContentsAsString(HEAD_POINT);
        String[] splitContent = headContent.split(":");
        String branchName = splitContent[0];
        return branchName;
    }
}
