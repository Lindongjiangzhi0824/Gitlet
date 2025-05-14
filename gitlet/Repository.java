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
        System.out.println("===");
        System.out.println("commit " + commit.getHashName());
        System.out.println("Date: " + dateToTimeStamp(commit.timestamp()));
        System.out.println(commit.getMessage());
        System.out.println("\n");
    }

    /**
     * @param field         打印的标题区域
     * @param files         文件夹下的所有文件名集合
     * @param branchName    指定的branch
     */
    public static void printStatusPerField(String field, Collection<String> files,
                                           String branchName) {
        System.out.println("=== " + field + " ===");
        if(field.equals("Branches")) {
            for(var file : files) {
                // 如果这个分支是当前分支
                if(file.equals(branchName)) {
                    System.out.println("*" + file);
                }else {
                    System.out.println(file);
                }
            }
        }else{
            for(var file : files) {
                System.out.println(file);
            }
        }
        System.out.println("\n");
    }
    /**
     *  对Modifications Not Staged For Commit这个区域的输出
     * @param field 打印的标题
     * @param modifiedFiles 标记为 modified 的文件
     * @param deletedFiles  标记为 deleted 的文件
     */
    public static void printStatusWithStatus(String field, Collection<String> modifiedFiles,
                                           Collection<String> deletedFiles) {

        System.out.println("=== " + field + " ===");
        for(String file : modifiedFiles) {
            System.out.println(file + " " + "(modified)");
        }
        for(String file : deletedFiles) {
            System.out.println(file + " " + "(deleted)");
        }
        System.out.println("\n");
    }

    /**
     * 检查当前目录下是否有没 track 的文件
     * @param commit
     * @return
     */
    public static boolean untrackFileExists(Commit commit){
        List<String> workFileNames = plainFilenamesIn(CWD);
        Set<String> currTrackSet = commit.getBlobMap().keySet();

        for(String fileName : workFileNames) {
            if(!currTrackSet.contains(fileName)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 处理两边文件都被修改的情况
     * @param headCommit
     * @param otherHeadCommit
     * @param splitTrackName
     */
    public static void processConflict(Commit headCommit, Commit otherHeadCommit, String splitTrackName) {
        String otherBlobFile = "";
        String otherBlobContent = "";
        String headBlobFile = "";
        String headBlobContent = "";

        HashMap<String, String> headCommitBlobMap = headCommit.getBlobMap();
        HashMap<String, String> otherHeadCommitBlobMap = otherHeadCommit.getBlobMap();

        // 发生了冲突（文件都被修改了）
        message("Encountered conflict in merge");

        // 获取冲突的两个分支上不同的文件内容
        if(otherHeadCommitBlobMap.containsKey(splitTrackName)) {
            otherBlobFile = otherHeadCommitBlobMap.get(splitTrackName);
            otherBlobContent = otherHeadCommitBlobMap.get(splitTrackName);
        }
        if(headCommitBlobMap.containsKey(splitTrackName)) {
            headBlobFile = headCommitBlobMap.get(splitTrackName);
            headBlobContent = headCommitBlobMap.get(splitTrackName);
        }

        // 修改 workFile 中的内容
        StringBuilder resContent = new StringBuilder();
        resContent.append("<<<<<< HEAD\n");
        resContent.append(headBlobContent);
        resContent.append("=========" + "\n");
        resContent.append(otherBlobContent);
        resContent.append(">>>>>>" + "\n");

        String resContentString = resContent.toString();
        writeContents(join(CWD, splitTrackName), resContentString);
        addStage(splitTrackName);
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
     * merge 时自动 commit
     * @param commitMsg
     * @param branchName
     */
    public static void commitFileForMerge(String commitMsg, String branchName) {
        List<String> addStageFiles = plainFilenamesIn(ADD_STAGE);
        List<String> rmStageFiles = plainFilenamesIn(REMOVE_STAGE);
        if(addStageFiles.isEmpty() && rmStageFiles.isEmpty()){
            throw new GitletException("ERROR: No changes added to commit");
        }

        if(commitMsg == null){
            throw new GitletException("Please enter a commit message.");
        }

        // 获取最新commit
        Commit oldCommit = getHeadCommit();
        Commit branchHeadCommit = getBranchHeadCommit(branchName, null);

        // 创建新的 commit , newCommit根据oldCommit调整
        Commit newCommit = new Commit(oldCommit);
        newCommit.setDirectParent(oldCommit.getHashName());
        newCommit.setTimestamp(new Date(System.currentTimeMillis()));
        newCommit.setMessage(commitMsg);
        newCommit.setOtherParent(branchHeadCommit.getHashName());

        // 对每个 addstage 中的filename进行路径读取，保存进commit的BlobMap
        for(String stageFileName : addStageFiles){
            // 这里好像有问题，没有转换成哈希码，只读了文件的内容
            String hashName = readContentsAsString(join(ADD_STAGE, stageFileName));
            newCommit.addBlob(stageFileName, hashName);
            join(ADD_STAGE, stageFileName).delete();
        }

        HashMap<String, String> blobMap = newCommit.getBlobMap();

        //对每一个rmstage中的fileName进行其路径的读取，删除commit的blobMap中对应的值
        for(String stageFileName : rmStageFiles){
            if(blobMap.containsKey(stageFileName)){
                join(BLOBS_FOLDER, blobMap.get(stageFileName)).delete(); // 删除blob中的文件
                newCommit.removeBlob(stageFileName);
            }
            join(REMOVE_STAGE, stageFileName).delete();
        }

        newCommit.saveCommit();
        // 更新 HEAD 和 master 指针
        saveHEAD(getHeadBranchName(), newCommit.getHashName());
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

    /**
     * java gitlet.Main global-log
     */
    public static void printGlobalLog(){
        List<String> commitFileName = plainFilenamesIn(COMMITS_DIR);
        for(String fileName : commitFileName){
            Commit commit = getCommit(fileName);
            printCommitLog(commit);
        }
    }

    public static void findCommit(String commitMsg) {
        Commit headCommit = getHeadCommit();
        Commit commit = headCommit;
        boolean found = false;

        List<String> commitFiles = plainFilenamesIn(COMMITS_DIR);
        for(String fileName : commitFiles){
            Commit temp = getCommit(fileName);
            if(temp.getMessage().contains(commitMsg)){
                message(temp.getHashName());
                found = true;
            }
        }
        // 没找到匹配的 commitMsg
        if(!found){
            System.out.println("Found no commit with that message.");
        }
    }

    /**
     * java gitlet.Main status
     * 1.如果 gitlet 没有初始化，则提示文件夹不存在并返回0
     * 2.获取并打印 HEAD , addstage, removestage 文件夹目录下的所有文件列表
     * 3.打印 Modifications Not Staged For Commit （修改了，但是没有提交到暂存区）
     *  1.文件在暂存区存在，但是在工作区不存在（加入 deletedFilesList）
     *  2.工作区文件与 addStage 中文件内容不一致（加入 modifiedFilesList）
     */
    public static void showStatus(){
        File gitletFile = join(CWD, ".gitlet");
        if(!gitletFile.exists()){
            message("No initialized Gitlet directory.");
            exit(0);
        }
        /* 获取当前分支*/
        Commit headCommit = getHeadCommit();
        String branchName = getHeadBranchName();

        List<String> filesHead = plainFilenamesIn(HEADS_DIR);
        List<String> filesInAdd = plainFilenamesIn(ADD_STAGE);
        List<String> filesInRemove = plainFilenamesIn(REMOVE_STAGE);
        // blobMap ; fileName + hashName
        HashMap<String, String> blobMap = headCommit.getBlobMap();
        Set<String> trackFileSet = blobMap.keySet();

        LinkedList<String> modifiedFilesList = new LinkedList<>();
        LinkedList<String> deletedFilesList = new LinkedList<>();
        LinkedList<String> untrackFilesList = new LinkedList<>();

        printStatusPerField("Branches",filesHead,branchName);
        printStatusPerField("Staged Files",filesInAdd,branchName);
        printStatusPerField("Removed Files",filesInRemove,branchName);

        // 暂存区与 addstage 的关系
        for(String fileAdd : filesInAdd){
            if(!join(CWD, fileAdd).exists()){
                deletedFilesList.add(fileAdd);
                continue;
            }
            String workFileContent = readContentsAsString(join(CWD, fileAdd));
            String addStageBlobName = readContentsAsString(join(ADD_STAGE, fileAdd));
            String addStageFileContent = readContentsAsString(join(BLOBS_FOLDER, addStageBlobName));
            if(!workFileContent.equals(addStageFileContent)){
                modifiedFilesList.add(fileAdd);
            }
        }

        // 暂存区与removestage的关系分析
        for(String trackFile : trackFileSet){
            // 头节点的BlobMap代表了什么
            if(trackFile.isEmpty() || trackFile==null){
                continue;
            }
            File workFile = join(CWD, trackFile);
            File fileInRmStage = join(REMOVE_STAGE, trackFile);
            if(!workFile.exists()){
                if(!fileInRmStage.exists()){
                    deletedFilesList.add(trackFile);
                }
                continue;
            }
            if(!filesInAdd.contains(trackFile)){
                String workFileContent = readContentsAsString(workFile);
                String blobContent = readContentsAsString(join(BLOBS_FOLDER,
                        blobMap.get(trackFile)));
                if(!workFileContent.equals(blobContent)){
                    // 当正在track的文件被修改，但addStage中无此文件，则进入modifiedFilesList
                    modifiedFilesList.add(trackFile);
                }
            }
        }
        printStatusWithStatus("Modifications Not Staged For Commit",
                modifiedFilesList, deletedFilesList);
        List<String> workFiles = plainFilenamesIn(CWD);
        for(String workFile : workFiles){
            if(!filesInAdd.contains(workFile) &&
            !filesInRemove.contains(workFile) &&
            !trackFileSet.contains(workFile)){
                untrackFilesList.add(workFile);
                continue;
            }
            if(filesInRemove.contains(workFile)){
                untrackFilesList.add(workFile);
            }
            printStatusPerField("Untracked Files", untrackFilesList, branchName);
        }

    }

    /**
     * 切换指定 id 分支 , 把云端存档更新到本地工作目录, 也可以更新指定的文件, 也可以切换一个分支
     * java gitlet.Main checkout -- [file name]
     * java gitlet.Main checkout [commit id] -- [file name]
     * java gitlet.Main checkout [branch name]
     * @param args
     */
    public static void checkOut(String[] args){
        String filenama;
        if(args.length == 2){
            // git checkout branchName
            checkOutBranch(args[1]);
        } else if(args.length == 4){
            // git checkout [commit id] -- [file name]
            if(!args[2].equals("--")){
                message("Incorrect operands.");
                exit(0);
            }
            /* 获取 Blob 对象*/
            filenama = args[3];
            String commitId = args[1];
            Commit commit = getHeadCommit();

            if(getCommitFromId(commitId) == null){
                message("Commit id is not exists.");
                exit(0);
            }else{
                commit = getCommitFromId(commitId);
            }

            if(!commit.getBlobMap().containsKey(filenama)){
                message("File does not exist in this commit.");
                exit(0);
            }
            String blobName = commit.getBlobMap().get(filenama);
            String targetBlobContent = getBlobContentFromName(blobName);

            /* 更新当前工作目录 ,用 Blob 对象中的内容覆盖工作目录*/
            File fileInWorkDir = join(CWD, filenama);
            overWriteFileWithBlob(fileInWorkDir, targetBlobContent);
        } else if (args.length == 3) {
            // git checkout -- [file name]
            /* 获取 Blob 对象中的内容 */
            filenama = args[2];
            Commit headCommit = getHeadCommit();
            if(!headCommit.getBlobMap().containsKey(filenama)){
                message("File does not exist in this commit.");
                exit(0);
            }
            String blobName = headCommit.getBlobMap().get(filenama);
            String targetBlobContent = getBlobContentFromName(blobName);

            /* 将Blob对象中的内容覆盖工作目录中的内容*/
            File fileInWorkDir = join(CWD, filenama);
            overWriteFileWithBlob(fileInWorkDir, targetBlobContent);
        }

        }

    /**
     * 从 A 分支 切换到 B 分支
     * @param branchName
     */
    private static void checkOutBranch(String branchName) {
        Commit headCommit = getHeadCommit();
        // 如果切换的是当前的分支，需要检查更新一下工作目录文件是否一致 ？
        if(branchName.equals(getHeadBranchName())){
            message("No need to checkout the current branch.");
            exit(0);
        }
        // 获取 branchName 的 head 对应的 commit
        Commit branchHeadCommit = getBranchHeadCommit(branchName, "No such branch exists.");
        HashMap<String, String> branchBlobMap = branchHeadCommit.getBlobMap();
        Set<String> fileNameSet = branchBlobMap.keySet();
        List<String> workFileNames = plainFilenamesIn(CWD);

        // 检查是当前目录下否有没有 tarck 的文件， 切换分支后会更新当前目录文件（所以切换前需要保存）
        if(Repository.untrackFileExists(headCommit)){
            message("There is an untracked file in the way; "
                    + "delete it, or add and commit it first.");
            exit(0);
        }
        /* 检测完未 track 的文件后重新写入文件夹 */
        for(var trackFile : fileNameSet){
            // 每个 trackFile 都是一个commit中追踪的fileName
            File workFile = join(CWD, trackFile);
            String blobHash = branchBlobMap.get(trackFile);
            String blobFromNameContent = getBlobContentFromName(blobHash);
            writeContents(workFile, blobFromNameContent);
        }
        /* 将当前给定的分支作为当前分支*/
        saveHEAD(branchName, branchHeadCommit.getHashName());
    }

    /**
     * java gitlet.Main branch [branch name]
     * @param branchName
     */
    public static void createBranch(String branchName){
        Commit headCommit = getHeadCommit();
        // 获取当前已有的 branch 分支列表
        List<String> fileNameInHeadDir = plainFilenamesIn(HEADS_DIR);
        // 此 branch 已经存在
        if(fileNameInHeadDir.contains(branchName)){
            message("Branch name already exists.");
            exit(0);
        }
        saveBranch(branchName, headCommit.getHashName());
    }

    /**
     * java gitlet.Main rm-branch [branch name]
     * @param branchName
     */
    public static void removeBranch(String branchName){
        // 检查是否有该 branchName 存在
        File branchFile = join(HEADS_DIR, branchName);
        if(!branchFile.exists()){
            message("Branch does not exist.");
            exit(0);
        }
        // 检查要删除的分支是否是当前所在分支
        Commit headCommit = getHeadCommit();
        if(getHeadBranchName().equals(branchName)){
            message("Can't remove the current branch.");
            exit(0);
        }
        // 删除这个 branch 的指针文件
        File branchHeadPoint = join(HEADS_DIR, branchName);
        branchHeadPoint.delete();
    }

    /**
     * java gitlet.Main reset [commit id]
     * @param commitId
     */
    public static void reset(String commitId){
        if(getCommitFromId(commitId) == null){
            message("Commit id is not exists.");
            exit(0);
        }
        Commit headCommit = getHeadCommit();
        Commit commit = getCommitFromId(commitId);
        HashMap<String, String> commitBlobMap = commit.getBlobMap();

        // 检查当前目录下是否有没有 track 的文件
        List<String> workFileNames = plainFilenamesIn(CWD);
        if(untrackFileExists(headCommit)){
            Set<String> currTrackSet = headCommit.getBlobMap().keySet();
            Set<String> resetTrackSet = commit.getBlobMap().keySet();
            boolean isUntrackInBoth = false; // 作用？

            // workfile 没有在 headCommit 中，也没有在 commit 中， 将其从 addstage 中删除，但CWD中保存
            for(String workFile: workFileNames){
                if(!currTrackSet.contains(workFile) && !resetTrackSet.contains(workFile)){
                    removeStage(workFile); // 逐个清除暂存区中的文件
                    isUntrackInBoth = true;
                }
            }
            if(!isUntrackInBoth){
                throw new GitletException("There is an untracked file in the way; "
                        + "delete it, or add and commit it first.");
            }
        }

        // 清空 CWD 文件夹
        for(String workFile: workFileNames){
            restrictedDelete(join(CWD, workFile));
        }
        // 将 fileNameSet 中每一个跟踪的文件重新写入工作文件夹中
        for(var trackedFileName : commit.getBlobMap().keySet()){
            File workFile = join(CWD, trackedFileName);
            String blobHash = commitBlobMap.get(trackedFileName);
            String blobContentFromName = getBlobContentFromName(blobHash);
            writeContents(workFile, blobContentFromName);
        }

        // 将branchHEAD指向 commit
        saveBranch(getHeadBranchName(),commitId);
        // 将目前的HEAD指针指向 commit
        saveHEAD(getHeadBranchName(),commitId);
    }

    public static void mergeBranch(String branchName){
        checkSafetyInMerge(branchName);
        Commit headCommit = getHeadCommit();
        Commit otherCommit = getBranchHeadCommit(branchName, "No such branch exists."); // 若此分支不存在则返回一个错误信息

        // 获取共同节点
        Commit splitCommit = getSplitCommit(headCommit, otherCommit);
        // 同一个分支
        if (splitCommit.getHashName().equals(otherCommit.getHashName())) {
            // 给定分支是当前分支的一个祖先
            throw new GitletException("Given branch is an ancestor of the current branch.");
        }

        HashMap<String, String> splitCommitBlobMap = splitCommit.getBlobMap();
        Set<String> splitKeySet = splitCommitBlobMap.keySet();
        HashMap<String, String> headCommitBlobMap = headCommit.getBlobMap();
        Set<String> headKeySet = headCommitBlobMap.keySet();
        HashMap<String, String> otherHeadCommitBlobMap = otherCommit.getBlobMap();
        Set<String> otherKeySet = otherHeadCommitBlobMap.keySet();

        // 解决其中一个分支文件被修改，另一方没被修改，或者双方文件内容不一致的冲突
        procesSplitCommit(splitCommit, headCommit, otherCommit);

        // 解决文件被删除的问题
        for(var headTrackName : headKeySet){
            if(!otherHeadCommitBlobMap.containsKey(headTrackName)){
                if(!splitCommitBlobMap.containsKey(headTrackName)){
                    // other 和 split 都没有这个文件
                    continue;
                }else{
                    // split 存在 other 被删除
                    if(!headCommitBlobMap.get(headTrackName)
                            .equals(splitCommitBlobMap.get(headTrackName))){
                        /* HEAD：被修改 */
                        /* 存在 conflict */
                        processConflict(headCommit, otherCommit, headTrackName);
                    }
                }
            } else if (otherHeadCommitBlobMap.containsKey(headTrackName)
                    && !splitCommitBlobMap.containsKey(headTrackName)) {
                // other中存在文件, split中不存在文件，即这是不一致的修改
                if(!otherHeadCommitBlobMap.get(headTrackName)
                        .equals(headCommitBlobMap.get(headTrackName))){
                    processConflict(headCommit,otherCommit, headTrackName);
                }
            }
        }

        for(var otherTrackName : otherKeySet){
            if(!headCommitBlobMap.containsKey(otherTrackName)
                && !splitCommitBlobMap.containsKey(otherTrackName)){
                // head 和 split 中都没有这个文件
                String[] checkOutArgs = {"checkout", otherCommit.getHashName(), "--", otherTrackName};
                checkOut(checkOutArgs);
                addStage(otherTrackName);
            }
        }
        if(splitCommit.getHashName().equals(headCommit.getHashName())){
            message("Current branch fast-forwarded.");
        }

        // 进行一次自动 commit
        String commitMsg = String.format("Merged %s into %s.", branchName, getHeadBranchName());
        commitFileForMerge(commitMsg, branchName);
    }
    public static void procesSplitCommit(Commit splitCommit, Commit headCommit, Commit otherCommit){
        HashMap<String, String> splitCommitBlobMap = splitCommit.getBlobMap();
        Set<String> splitKeySet = splitCommitBlobMap.keySet();
        HashMap<String, String> headCommitBlobMap = headCommit.getBlobMap();
        Set<String> headKeySet = headCommitBlobMap.keySet();
        HashMap<String, String> otherHeadCommitBlobMap = otherCommit.getBlobMap();
        Set<String> otherKeySet = otherHeadCommitBlobMap.keySet();

        for(var splitTrackName: splitKeySet){
            // 如果在 HEAD 中没有被修改
            if(headCommitBlobMap.containsKey(splitTrackName) && headCommitBlobMap.get(splitTrackName).equals(headCommitBlobMap.get(splitTrackName))){
                // 若 other 中存在此文件
                if(otherHeadCommitBlobMap.containsKey(splitTrackName)){
                    // 1. HEAD 未修改， other 中修改了
                    if(!otherHeadCommitBlobMap.get(splitTrackName).equals(splitCommitBlobMap.get(splitTrackName))){
                        // 使用 checkout 将 other 文件覆盖进工作区，同时将其 add 到暂存区
                        String[] checkOutArgs = {"checkout", otherCommit.getHashName(),"--", splitTrackName};
                        checkOut(checkOutArgs);
                        addStage(splitTrackName);
                    }
                }else{
                    // other 中不存在此文件
                    removeStage(splitTrackName);
                }
            }else{
                // 在 HEAD 中修改以及删除
                // otherCommit 在 splitCommit后没有懂 splitTrackName 这个文件
                if(otherHeadCommitBlobMap.containsKey(splitTrackName) &&
                        otherHeadCommitBlobMap.get(splitTrackName)
                                .equals(splitCommitBlobMap.get(splitTrackName))){
                    continue;
                }else{
                    // other 中被修改 或者 被删除
                    if(!otherHeadCommitBlobMap.containsKey(splitTrackName) &&
                        !headCommitBlobMap.get(splitTrackName)
                                .equals(splitTrackName)){
                        // 该文件在两个分支中都被删除了
                        continue;
                    } else if (!otherHeadCommitBlobMap.containsKey(splitTrackName)
                            || !headCommitBlobMap.containsKey(splitTrackName)) {
                        // 只要一方被删除，跳过
                        continue;
                    }else {
                        // 不一致的修改，不包括删除
                        processConflict(headCommit, otherCommit, splitTrackName);
                    }
                }
            }
        }
    }
    /**
     * 检查 merge 指令是否有错误
     * 1.暂存区不能有文件
     * 2.分支不存在，返回一个错误消息
     * 3.合并的分支是当前分支
     * 4.检查是否有没有 track 的文件
     * @param branchName
     */
    public static void checkSafetyInMerge(String branchName){
        List<String> addStageFiles = plainFilenamesIn(ADD_STAGE);
        List<String> rmStageFiles = plainFilenamesIn(REMOVE_STAGE);
        if(!addStageFiles.isEmpty() || !rmStageFiles.isEmpty()){
            throw new GitletException("You have uncommitted changes.");
        }
        Commit headCommit = getHeadCommit();
        String errMsg = "A branch with that name does not exist.";
        Commit otherHeadCommit = getBranchHeadCommit(branchName, errMsg);

        if(getHeadBranchName().equals(branchName)){
            throw new GitletException("Cannot merge a branch with itself.");
        }
        if(untrackFileExists(headCommit)){
            throw new GitletException("There is an untracked file in the way; "
                    + "delete it, or add and commit it first.");
        }
    }
}

