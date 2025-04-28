package gitlet;

import java.io.File;
import java.io.Serializable;

import static gitlet.Refs.*;
import static gitlet.Utils.*;

public class Blob implements Serializable {
    private String content;
    private File filePath;
    private String hashName;

    public Blob(String content, String hashName) {
        this.content = content;
        this.hashName = hashName;
        this.filePath = join(BLOBS_FOLDER, hashName);
    }

    public File getFilePath() {
        return filePath;
    }

    /**
     *  save bolb object to BLOB_FOLDER file
     */
    public void saveBlob(){
        if(!filePath.exists()){
            writeContents(filePath, this.content);
        }
    }

    /**
     *  Based on blobName obtain the contents of Blob, blob name is a hashcode.
     *  If don't have this file ,return null.
     *
     *  @return content of Blob
     */
    public static String getBlobContentFromName(String blobName){
        String blobContent = null;
        File blobFile = join(BLOBS_FOLDER, blobName);
        if(blobFile.isFile() && blobFile.exists()){
            blobContent = readContentsAsString(blobFile);
        }

        return blobContent;
    }
}

