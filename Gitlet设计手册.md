# Class and Data Structure

***

## Class 1: Commit

对于需要提交的文件类，此类使用最多，每次进行处理的时候都会去构建commit 对象

**Instance Variables**

* String message : 保存commit提交的说明
* Date timestamp : 提交时间，第一个是Date(0), 根据Date对象进行
* String directParent : 当前commit的上一个commit
* String otherParent: 若存在其他merge操作， 会使用此变量，记录 merge [branchName] 中的 branch commit为上一个节点
* HashMap<String, String> blobMap : 文件内容的hashMap, key为 track文件的文件名， value 是其对应的blob的hash名

**Methods**

成员方法：

* getHashName() : 获取commit的 sha-1 hash值， sha-1 包括的内容是 message, timestamp, directParent
* saveCommit() : 将对象保存进 join(COMMIT_DIR, hashName) 中， 文件名为commit的hash名
* addBlob(String fileName, String blobName) : 保存blobMap 中的指定键值对
* removeBlob(String fileName) : 删除blobMap 中指定的键值对

静态方法：

* getHeadCommit() : 用于获取HEAD指针指向的Commit对象
* getBranchHeadCommit(String branchName, String error_msg) : 用于获取branches文件夹中分支文件指向的Commit对象， error_msg 参数是当不存在此branch时需要提供的错误信息
* getCommit(String hashName) : 通过hashname来获取Commit对象，如果在commit文件夹中不存在此文件则返回null
* getCommitFromId(String commitId) : 给定一个commitId, 返回一个相对应的commit对象， 若不存在此commit对象，则返回null, 与getCommit() 区别是支持前缀搜索
* getSplitCommit(Commit commitA, Commit commitB) : 使用BFS方法查找最近的split Commit

## Class 2 : Refs

关于文件指针的类

**Instance Variable**

* REFS_DIR : ".gitlet/refs"文件夹
* HEAD_DIR : ".gitlet/refs/heads" 文件夹
* HEAD_CONTENT_PATH : ".gitlet/HEAD" 文件夹

**Methods**

* saveBranch(String branchName, String hashName) : 创建一个文件，路径是join(HEAD_DIR, branchName), 向其中写入 hashName, 也就是 commitId
* saveHEAD(string branchName, String brachHeadCommitHash) : 在 HEAD文件中写入当前branch的hash值，格式：branchName + ":" + branchHeadCommitHash
* getHeadBranchName() : 从HEAD文件中直接获取branch的名字

## Class 3 : Blob

用于Blob存储相关的类

**Instance Variable**

* private String content : blob中保存的内容
* public File filePath : blob文件的自身路径
* private String hashName : blob文件名，以hash为值

**Methods**

* saveBlob() : 将blob对象保存进 BLOB_FOLDER文件， 内容就是blob文件的content

**静态方法：**

* getBlobContentFromName(String blobName) : 根据blobName 获取Blob的内容， blobName是一个hash值，若此文件不存在，则返回null
* overWriterFileWithBlob(File file, String content) : 将blob.content的内容覆盖进file文件中



# Algorithms

***

## init

java gitlet.Main init

创建一个文件夹环境

```txt
.gitlet (folder)
    |── objects (folder) // 存储commit对象文件
        |-- commits
        |-- blobs
    |── refs (folder)
        |── heads (folder) //指向目前的branch
            |-- master (file)
            |-- other file      //表示其他分支的路径
        |-- HEAD (file)     // 保存HEAD指针的对应hashname
    |-- addstage (folder)       // 暂存区文件夹
    |-- removestage (folder)
```

## add

java gitlet.Main add [file name]
