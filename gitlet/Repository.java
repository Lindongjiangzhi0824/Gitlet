package gitlet;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

import static gitlet.Commit.*;
import static gitlet.Refs.*;
import static gitlet.Utils.*;
import static gitlet.Blob.*;
import static java.lang.System.exit;

// TODO: any imports you need here

/** Represents a gitlet repository.
 *  TODO: It's a good idea to give a description here of what else this Class
 *  does at a high level.
 *
 *  @author TODO
 */
public class Repository {
    /**
     * TODO: add instance variables here.
     *
     * List all instance variables of the Repository class here with a useful
     * comment above them describing what that variable represents and how that
     * variable is used. We've provided two examples for you.
     */

    /* TODO: fill in the rest of this class. */
    public static void setupPersistence() {
        GITLET_DIR.mkdir();
        COMMIT_OBJ_DIR.mkdir();
        COMMITS_DIR.mkdir();
        BLOBS_FOLDER.mkdir();
        REFS_DIR.mkdir();
        HEADS_DIR.mkdir();
        ADD_STAGE.mkdir();
        REMOVE_STAGE.mkdir();
    }


    public static void checkArgsEmpty(String[] args) {
        if(args.length == 0) {
            System.out.println("Please enter a command.");
            exit(0);
        }
    }

    /**
     * To get Date obj a format to transform the object to string.
     * @param date : Date obj
     * @return : timestamp in standard format
     */
    public static String dateToTimeStamp(Date date) {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
        return dateFormat.format(date);
    }

    public static void printCommitLog(Commit commit) {

    }

    /* -------------------------功能函数------------------------------ */

    /**
     * java gitlet.Main init
     */
    public static void initPersistence(){
        if(GITLET_DIR.exists()) {
            System.out.println("A Gitlet version-control system already exists in the current directory.");
            exit(0);
        }
        // Create the folder in need
        setupPersistence();
        // create timestamp, Commit and save commit into files
        Date timestampInit = new Date(0);
        Commit initialCommit = new Commit("initial commit","", timestampInit,null,null);
        initialCommit.saveCommit();

        // Save the hashName to heads dir
        String commitHashName = initialCommit.getHashName();
        String branchName = "master";
        saveBranch(branchName, commitHashName);

        // 将此时的HEAD指针指向commit中的代表head的文件
        saveHEAD("master", commitHashName);
    }

    public static void addStage(String addFileName){
        if(addFileName == null || addFileName.isEmpty()){
            throw new GitletException("please enter a file name.");
        }
        File fileAdded = join(CWD, addFileName);
        if(!fileAdded.exists()){
            throw new GitletException("File does not exist.");
        }
        String fileAddedContent = readContentsAsString(fileAdded);

        Commit headCommit = getHeadCommit();
        HashMap<String, String> headCommitBlobMap = headCommit.getBlobMap();
        /* 如果文件已经被 track */
        if(headCommitBlobMap.containsKey(addFileName)){
            String fileAddedInHash = headCommit.getBlobMap().get(addFileName);
            String commitContent = getBlobContentFromName(fileAddedInHash);

             /* 如果暂存内容和想要添加内容一致，则不将其纳入暂存区，
            同时将其从暂存区删除（如果存在）,同时将其从removal区移除 */
            if(commitContent.equals(fileAddedContent)){
                List<String> filesAdd = plainFilenamesIn(ADD_STAGE);
                List<String> filesRm = plainFilenamesIn(REMOVE_STAGE);

                /* 如果在暂存区存在， 从暂存区中删除*/
                if(filesAdd.contains(addFileName)){
                    join(ADD_STAGE, addFileName).delete();
                }

                /* 如果 removal area 存在，从中删除*/
                if(filesRm.contains(addFileName)){
                    join(REMOVE_STAGE, addFileName).delete();
                }

                return; //直接退出
            }
        }
        /* 将文件放入暂存区， blob 文件名是内容的 hash 值，内容是原文件内容*/
        String fileContent = readContentsAsString(fileAdded);
        String blobName = sha1(fileContent);

        Blob blobAdd = new Blob(fileContent, blobName);
        blobAdd.saveBlob();

        /* 不管原先是否存在，都会执行写逻辑*/
        /* addStage中写入指针,文件名是addFileName, 内容是暂存区保存的路径 */
        File blobPoint = join(ADD_STAGE, addFileName);
        writeContents(blobPoint, blobAdd.getFilePath().getName());
    }

    /**
     * java gitlet/Main.java commit [message]
     * @param commitMsg
     */
    public static void commitFile(String commitMsg) {
        /* 获取 addstage 中的 filename 和 hashname */
        List<String> addStageFiles = plainFilenamesIn(ADD_STAGE);
        List<String> removeStageFiles = plainFilenamesIn(REMOVE_STAGE);
        /* 文件夹里没有任何记录 或 commitMsg 为空*/
        if(addStageFiles.isEmpty() &&  removeStageFiles.isEmpty()){
            throw new GitletException("ERROR:既没添加也没删除任何东西");
        }
        if(commitMsg == null || commitMsg.isEmpty()){
            throw new GitletException("commitMsg为空，你应该输入提交信息！");
        }
        /*获取最新的 commit */
        Commit oldCommit = getHeadCommit();
        /* 创建新的 commit, newCommit 根据 oldCommit 进行调整*/
        Commit newCommit =new Commit(oldCommit);
        newCommit.setDirectParent(oldCommit.getHashName()); //指定父节点
        newCommit.setTimestamp(new Date(System.currentTimeMillis()));
        newCommit.setMessage(commitMsg);

        /* 对每一个 addStage 中的 fileName 进行路径读取， 保存进 commit 的 blobMap*/
        for(String stageFileName : addStageFiles){
            String hashName = readContentsAsString(join(ADD_STAGE, stageFileName));
            newCommit.addBlob(stageFileName, hashName);  // newCommit 中更新 blob
            join(ADD_STAGE, stageFileName).delete();
        }

        HashMap<String, String> blobMap = newCommit.getBlobMap();

        /* 对每一个 rmstage 中的 fileName 进行路径读取， 删除commit的 blobMap 中对应的值*/
        for(String stageFileName : removeStageFiles){
            if(blobMap.containsKey(stageFileName)){
                newCommit.removeBlob(stageFileName);
            }
            join(REMOVE_STAGE, stageFileName).delete();
        }

        newCommit.saveCommit();

        /* 更新HEAD指针和当前 branch 的 head 指针*/
        saveHEAD(getHeadBranchName(),newCommit.getHashName());
        saveBranch(getHeadBranchName(), newCommit.getHashName());
    }

    /**
     * java gitlet/Main rm [fileName]
     * @param removeFileName
     */
    public static void removeStage(String removeFileName) {
        if(removeFileName == null || removeFileName.isEmpty()){
            System.out.println("please enter a file name.");
            exit(0);
        }

        /* 如果 暂存目录中不存在此文件，同时在 commit 中不存在此文件*/
        Commit headCommit = getHeadCommit();
        HashMap<String, String> blobMap = headCommit.getBlobMap();
        List<String> addStageFiles = plainFilenamesIn(ADD_STAGE);

        if(!blobMap.containsKey(removeFileName)){
            if(!addStageFiles.isEmpty()){
                System.out.println("No such file, rm failure.");
                exit(0);
            }
        }

        /* 如果 addstage 中存在， 删除*/
        File addStageFile = join(ADD_STAGE, removeFileName);
        if(addStageFile.exists()){
            addStageFile.delete();
        }
        /* 此文件正在被 track 中*/
        if(blobMap.containsKey(removeFileName)){
            /* 添加进 removeStage */
            File remoteFilePoint = new File(REMOVE_STAGE, removeFileName);
            writeContents(remoteFilePoint,"");

            /* 删除工作目录下的文件，仅在这个文件被track的时候进行删除*/
            File fileDeleted = new File(CWD, removeFileName);
            restrictedDelete(fileDeleted);
        }
    }
    /**
     * java gitlet/Main log
     */
    public static void printLog(){
        Commit headCommit = getHeadCommit();
        Commit commit = headCommit;

        while(!commit.getDirectParent().equals("")){
            printCommitLog(commit);
            commit = getCommit(commit.getDirectParent());
        }
        /* 打印第一个提交的日志 */
        printCommitLog(commit);
    }
}
