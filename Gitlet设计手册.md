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

将指定的文件放入 addstage 文件夹中，将文件内容创建为 blob 文件， 以内容的 hash 值作为文件名来保存刀 objects/blobs文件夹中

将当前存在的文件副本添加到暂存stage区域

暂存已暂存的文件会用新内容覆盖暂存区域中的上一个条目。暂存区域应该位于 .gitlet 中

**如果文件的当前工作版本与当前commit中的版本相同，请不要暂存要添加的文件， 如果它已经存在，将其从暂存区域中删除（当文件被更改、添加，然后更改回其原始版本时，可能会发生这种情况）。**

```txt
.gitlet (folder)
    |── objects (folder) 
        |-- commits
        |-- blobs
            |-- <hash>  <----- 加入的file.txt文件内容
    |── refs (folder)
        |── heads (folder) 
            |-- master (file)
            |-- other file     
        |-- HEAD (file)     
    |-- addstage (folder)       
        |-- file.txt  <----- 保存blob文件的路径
    |-- removestage (folder)

file.txt  <----- 加入的文件
```

## commit

java gitlet/Main.java commit [message]

将 addstage 和 removestage 中的文件一个个进行响应操作， addStage中添加， removeStage中的进行删除，将跟踪文件的快照，并保存到当前提交和暂存的区域中，方便以后可以恢复，以及创建新的提交

提交将只是更新它正在跟踪的文件的内容，这些文件在提交时已暂存以进行添加，

```txt
.gitlet (folder)
    |── objects (folder) 
        |-- commits
            | -- <hash> <----- 添加进的commit文件，内容是对应的blob文件名
        |-- blobs
            |-- <hash>  
    |── refs (folder)
        |── heads (folder) 
            |-- master (file)
            |-- other file     
        |-- HEAD (file)     
    |-- addstage (folder)       
        |-- file.txt  
    |-- removestage (folder)
file.txt  <----- commit的文件
```

## rm

`java gitlet.Main rm [file name]`

如果文件当前已经暂存进行添加，先取消暂存文件

如果在当前提交中跟踪了该文件，请暂存该文件以供删除，如果用户尚未从工作目录中删除该文件，则从工作目录删除此文件（如果在commit中跟踪此文件，则可以对它remove, 如果没有跟踪就不能删除）

```
.gitlet (folder)
    |── objects (folder) 
        |-- commits
            | -- <hash> 
        |-- blobs
            |-- <hash>  
    |── refs (folder)
        |── heads (folder) 
            |-- master (file)
            |-- other file     
        |-- HEAD (file)     
    |-- addstage (folder)       <----- 若是在addstage中有则删除
    |-- removestage (folder)
        |-- file.txt  <----- 添加
file.txt  <----- 若是在被track状态，则进行删除；若不是在track，就不能删除
```



## log

`java gitlet.Main log`

输出 log , 内容是从当前 HEAD 指向的 commit以及所有的 parents 格式如下：

```
===
commit a0da1ea5a15ab613bf9961fd86f010cf74c7ee48
Date: Thu Nov 9 20:00:05 2017 -0800
A commit message.

===
commit 3e8bf1d794ca2e9ef8a4007275acf3751c7170ff
Date: Thu Nov 9 17:01:33 2017 -0800
Another commit message.

===
commit e881c9575d180a215d1a636545b8fd9abfb1d2bb
Date: Wed Dec 31 16:00:00 1969 -0800
initial commit

```

## glob-log

`java gitlet.Main glob-log`

输出所有的commit文件

## find

`java gitlet.Main find [commit message]`

输出所有的commit文件

打印出包含给定消息的所有提交ID，每行一个

如果有多个这样的提交，它会在单独的行上打印 id

提交消息命令是单操作数， eg. `java gitlet.Main find "initial commit"`

## status

`java gitlet.Main status`

显示当前分支， 并用 * 标记当前分支，同时显示暂存/待添加/删除的文件，格式如下：

```
=== Branches ===
*master
other-branch
  
=== Staged Files ===
wug.txt
wug2.txt
  
=== Removed Files ===
goodbye.txt
  
=== Modifications Not Staged For Commit ===
junk.txt (deleted)
wug3.txt (modified)
  
=== Untracked Files ===
random.stuff
```

## checkout

**核心：就是回到之前的某个commit提交节点**

` java gitlet.Main checkout -- [file name]`

获取 head commit 中存在的文件版本， 并将其放入工作目录，覆盖已经存在的文件版本（如果有）。文件的新版本不会暂存。。

` java gitlet.Main checkout [commit id] -- [file name]`

获取提交中具有指定 ID 版本的文件系统， 并将其放入到工作目录中，覆盖已经存在的文件版本。文件的最新版本不被暂存

` java gitlet.Main checkout [branch name]`

获取给定分支 head 处提交的所有文件，并将他们放在工作目录中，覆盖已经存在的文件版本。同时在此命令结束时，会将把给定的分支视为当前分支 [HEAD] 。

注意：

1. 当切换到另一个分支时，Gitlet会强制让工作目录的内容与目标分支的最新提交完全一致，如果当前分支中存在一些文件（且这些文件已经被提交到当前分支），但是这些文件不存在于目标分支的最新提交中，那这些文件会被系统删除
2. 当切换到另一个分支时，系统会清空暂存区，如果切换的是当前分支本身，则暂存区不变

## branch

`java gitlet.Main branch [branch name]`

创建一个具有给定名称的新分支，并将其指向当前头部提交。分支是对提交节点的引用(sha1标识)的名称。这个命令不会立即切换到新的分支，在调用branch之前，代码应该使用名为 "master" 的默认分支运行。

```
.gitlet (folder)
    |── objects (folder) 
        |-- commits
            | -- <hash> 
        |-- blobs
            |-- <hash>  
    |── refs (folder)
        |── heads (folder) 
            |-- master (file)
            |-- other file     <----- 指向当前头部提交
        |-- HEAD (file)     
    |-- addstage (folder)       
    |-- removestage (folder)
file.txt  
```

## rm-branch

`java gitlet.Main rm-branch [branch name]`

删除具有给定名称的分支，仅删除与分支关联的指针（就说heads下的文件）；它不代表删除在分支下创建的多有提交。

```
.gitlet (folder)
    |── objects (folder) 
        |-- commits
            | -- <hash> 
        |-- blobs
            |-- <hash>  
    |── refs (folder)
        |── heads (folder) 
            |-- master (file)
            |-- other file     <----- 将此文件删除
        |-- HEAD (file)     
    |-- addstage (folder)       
    |-- removestage (folder)
file.txt  
```

