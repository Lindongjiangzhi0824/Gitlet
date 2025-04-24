package gitlet;

// TODO: any imports you need here

import java.io.File;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Date; // TODO: You'll likely use this in this class
import java.util.HashMap;

import static gitlet.Refs.*;
import static gitlet.Utils.*;

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

    }
    public void addBlob(String fileName, String blobName){
        this.blobMap.put(fileName, blobName);
    }
    public void removeBlob(String fileName){
        this.blobMap.remove(fileName);
    }
    public String getHashName(){
        return sha1(this);
    }
    public String message() {
        return message;
    }

    public Commit setMessage(String message) {
        this.message = message;
        return this;
    }

    public String directParent() {
        return directParent;
    }

    public Commit setDirectParent(String directParent) {
        this.directParent = directParent;
        return this;
    }

    public String otherParent() {
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

    public HashMap<String, String> blobMap() {
        return blobMap;
    }

    public Commit setBlobMap(HashMap<String, String> blobMap) {
        this.blobMap = blobMap;
        return this;
    }
    /* ======================== 以上为getter和setter ======================*/
    /* TODO: fill in the rest of this class. */

    public static Commit getHeadCommit(){
        /* To obtain HEAD pointer, this pointer point to newest commit. */
        String headContent = readContentsAsString(HEAD_POINT);
        String headHashName = headContent.split(":")[1];
        File commitFile = join(COMMIT_OBJ_DIR, headHashName);
        /* Obtain commit file. */
        Commit commit = readObject(commitFile, Commit.class);

        return commit;
    }
}
