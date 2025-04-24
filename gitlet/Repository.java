package gitlet;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.sql.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;

import static gitlet.Commit.*;
import static gitlet.Refs.*;
import static gitlet.Utils.*;
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
        BLOBS_FOLDER.mkdir();
        REFS_DIR.mkdir();
        HEADS_DIR.mkdir();
        MASTER_DIR.mkdir();
        HEAD.mkdir();
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
    public static String dateToTimestamp(Date date) {
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
    }
}
