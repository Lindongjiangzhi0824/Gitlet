package gitlet;

// TODO: any imports you need here

import java.io.File;
import java.io.Serializable;
import java.util.*;

import static gitlet.Refs.*;
import static gitlet.Repository.dateToTimeStamp;
import static gitlet.Utils.*;
import static java.lang.System.exit;

/** Represents a gitlet commit object.
 *  TODO: It's a good idea to give a description here of what else this Class
 *  does at a high level.
 *
 *  @author TODO
 */
public class Commit implements Serializable {
    /**
     * TODO: add instance variables here.
     *
     * List all instance variables of the Commit class here with a useful
     * comment above them describing what that variable represents and how that
     * variable is used. We've provided one example for `message`.
     */

    /** The message of this Commit. */
    private String message;
    /* The parent of commit, null if it's the first commit.*/
    private String directParent;
    private String otherParent;
    /* the timestamp of commit files*/
    private Date timestamp;
    /* the contents of commit files. */
    private HashMap<String, String> blobMap = new HashMap<>();

    public Commit(String message, String directParent,Date timestamp,
                  String blobFileName, String blobHashName) {
        this.message = message;
        this.directParent = directParent;
        this.timestamp = timestamp;

        if(blobFileName == null || blobFileName.isEmpty()){
            this.blobMap = new HashMap<>();
        }else{
            this.blobMap.put(blobFileName, blobHashName);
        }
    }
    public Commit(Commit directparent){
        this.message = directparent.message;
        this.directParent = directparent.directParent;
        this.timestamp = directparent.timestamp;
        this.blobMap = directparent.blobMap;
    }
    /* To save commit into files in COMMIT_FOLDER, persists the status of object. */
    public void saveCommit(){
        // 1.得到文件名
        String hashname = this.getHashName();
        // 2.创建文件存放路径
        File commitFile = new File(COMMITS_DIR, hashname);
        // 3.将对象序列化写入
        writeObject(commitFile, this);

    }
    public void addBlob(String fileName, String blobName){
        this.blobMap.put(fileName, blobName);
    }
    public void removeBlob(String fileName){
        this.blobMap.remove(fileName);
    }
    public String getHashName(){
        return sha1(this.message, dateToTimeStamp(this.timestamp), this.directParent);
    }
    public String getMessage() {
        return message;
    }

    public Commit setMessage(String message) {
        this.message = message;
        return this;
    }

    public String getDirectParent() {
        return directParent;
    }

    public Commit setDirectParent(String directParent) {
        this.directParent = directParent;
        return this;
    }

    public String getOtherParent() {
        return otherParent;
    }

    public Commit setOtherParent(String otherParent) {
        this.otherParent = otherParent;
        return this;
    }

    public Date timestamp() {
        return timestamp;
    }

    public Commit setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
        return this;
    }

    public HashMap<String, String> getBlobMap() {
        return blobMap;
    }

    public Commit setBlobMap(HashMap<String, String> blobMap) {
        this.blobMap = blobMap;
        return this;
    }
    /* ======================== 以上为getter和setter ======================*/
    /* TODO: fill in the rest of this class. */

    /**
     * 获取 HEAD 指针指向的Commit对象
     * @return
     */
    public static Commit getHeadCommit(){
        /* To obtain HEAD pointer, this pointer point to newest commit. */
        String headContent = readContentsAsString(HEAD_POINT);
        String headHashName = headContent.split(":")[1];
        File commitFile = join(COMMITS_DIR, headHashName);
        /* Obtain commit file. */
        Commit commit = readObject(commitFile, Commit.class);

        return commit;
    }

    /**
     * 用于获取branches文件夹中分类文件指向Commit对象
     * @param branchName
     * @param errorMsg
     * @return 反序列化后的对象（还原）
     */
    public static Commit getBranchHeadCommit(String branchName, String errorMsg){
        File branchFile = join(HEADS_DIR, branchName);
        if(!branchFile.exists()){
            System.out.println(errorMsg);
            exit(0);
        }
        /* 获取头指针，这个指针指向最新的 commit */
        String headHashName = readContentsAsString(branchFile);
        File commitFile = join(COMMITS_DIR, headHashName);
        /*获取commit文件*/
        Commit commit = readObject(commitFile, Commit.class);

        return commit;
    }

    /**
     * 通过 hash 值来返回 Commit 对象
     * @param hashName
     * @return 对应的 Commit 对象
     */
    public static Commit getCommit(String hashName){
        List<String> commitFiles = plainFilenamesIn(COMMITS_DIR);
        /* 如果在 commit 文件夹中不存在次文件 */
        if(!commitFiles.contains(hashName)){
            return null;
        }

        File commitFile = join(COMMITS_DIR, hashName);
        Commit commit = readObject(commitFile, Commit.class);
        return commit;
    }

    /**
     * 给定一个 commitId, 返回一个相对应的 commit 对象， 若没有这个 commit 对象，则返回 null
     * @param commitId
     * @return commit or null
     */
    public static Commit getCommitFromId(String commitId){
        Commit commit = null;

        /* 从commit文件夹中寻找*/
        String resCommitId = null;
        List<String> commitFileNames = plainFilenamesIn(COMMITS_DIR);
        /* 匹配前缀 */
        for(String commitFileName : commitFileNames){
            if(commitFileName.startsWith(commitId)){
                resCommitId = commitFileName;
                break;
            }
        }

        if(resCommitId == null){
            return null;
        }else{
            File commitFile = join(COMMITS_DIR, commitId);
            commit = readObject(commitFile, Commit.class);
        }

        return commit;
    }

    /**
     * 获取两个分支的共同节点， 从 directParent 搜索
     * @param commitA
     * @param commitB
     * @return
     */
    public static Commit getSplitCommit(Commit commitA, Commit commitB){
        Commit p1 = commitA, p2 = commitB;
        /* 遍历提交链 */
        Deque<Commit> dequecommitA = new ArrayDeque<>();
        Deque<Commit> dequecommitB = new ArrayDeque<>();

        /* 保存访问过的节点 */
        HashSet<String> visitedInCommitA = new HashSet<>();
        HashSet<String> visitedInCommitB = new HashSet<>();

        dequecommitA.add(p1);
        dequecommitB.add(p2);

        while(!dequecommitA.isEmpty() && !dequecommitB.isEmpty()){
            if(!dequecommitA.isEmpty()){
                /* commitA 的队列中存在可遍历对象 */
                Commit currA =  dequecommitA.poll();
                if(visitedInCommitB.contains(currA.getHashName())){
                    return currA;
                }
                visitedInCommitA.add(currA.getHashName());
                addParentsToDeque(currA, dequecommitA);
            }

            if(!dequecommitB.isEmpty()){
                Commit currB =  dequecommitB.poll();
                if(visitedInCommitA.contains(currB.getHashName())){
                    return currB;
                }
                visitedInCommitB.add(currB.getHashName());
                addParentsToDeque(currB, dequecommitB);
            }
        }
        return null;
    }

    /**
     * 将此节点的父节点 （或两个父节点） 放入队列中
     * @param commit
     * @param dequeCommit
     */
    private static void addParentsToDeque(Commit commit, Queue<Commit> dequeCommit){
        if(!commit.getDirectParent().isEmpty()){
            dequeCommit.add(getCommitFromId(commit.getDirectParent()));
        }

        if(commit.getOtherParent() != null){
            dequeCommit.add(getCommitFromId(commit.getOtherParent()));
        }
    }

}
