# Gitlet Design Document

### init初始化过程

1. 检查当前目录下是否有 .gitlet文件夹
2. 没有这个文件夹就创建文件夹
3. 在此目录下再创建objects, refs, HEAD三个文件夹

#### objects文件夹

***

1. 对象设计
2. 对象存储（序列化），每个对象对应不同的子文件夹
   1. CommitObject文件夹（存放序列化的Commit对象文件）
   2. 

3. 对象命名（通过 sha1算法）



### 1. Commit -Object design

Commit整体是一个链表，设计一个ArrayList<Commit> 。有一个head指针指向最后一个提交的Commit对象。

* 对象命名（通过sha1）
* 提交日期（有时区信息） + 提交作者
* 生成提交日志
* 有一个空间存放每个文件的对象指针（对象指向最新的文件内容）

### 2.Blobs设计

动态数组，初始化一个对象数组，容量不够时就自动增加容量（初始化为capacity = 30）, 容量 >= 80% 自动扩容为原来的两倍

对象设计：

* 文件名
* 版本号
* sha1值



![](http://tuchuang.zyj0824.top/picture/20250411210119591.png)



`.git` 目录是 Git 仓库的核心，存放所有版本控制相关的元数据和对象。以下是各个文件/目录的作用详解：

---

### **核心文件**
1. **`HEAD`**  
   • **作用**：指向当前检出的分支（或提交），例如 `ref: refs/heads/master` 表示当前在 `master` 分支。
   • **修改场景**：切换分支时自动更新。

2. **`config`**  
   • **作用**：当前仓库的本地配置（优先级高于全局配置 `~/.gitconfig`）。
   • **内容**：用户名、远程仓库地址、别名等。
   • **修改方式**：`git config` 命令或直接编辑。

3. **`index`**  
   • **作用**：暂存区（Stage）的二进制文件，记录即将提交的文件状态。
   • **查看内容**：`git ls-files --stage`。

4. **`COMMIT_EDITMSG`**  
   • **作用**：保存最后一次提交的注释（编辑提交信息时的临时文件）。
   • **修改场景**：运行 `git commit` 时自动生成。

---

### **核心目录**
5. **`objects/`**  
   • **作用**：存储所有 Git 对象（Blob、Tree、Commit、Tag），每个对象以 SHA-1 哈希命名。
   • **子目录**：`pack/`（压缩后的对象包）、`info/`（对象元信息）。
   • **查看对象内容**：`git cat-file -p <hash>`。

6. **`refs/`**  
   • **作用**：存储所有引用（分支、标签、远程分支等）。
   • **子目录**：
     ◦ `heads/`：本地分支（如 `refs/heads/master`）。
     ◦ `tags/`：标签（如 `refs/tags/v1.0`）。
     ◦ `remotes/`：远程分支（如 `refs/remotes/origin/master`）。

7. **`logs/`**  
   • **作用**：记录分支和 HEAD 的变更历史（用于恢复误操作）。
   • **查看内容**：`git reflog`。

---

### **辅助文件/目录**
8. **`hooks/`**  
   • **作用**：存放 Git 钩子脚本（如 `pre-commit`、`post-receive`），用于自动化任务。
   • **默认状态**：样例文件以 `.sample` 结尾，需重命名后生效。

9. **`info/`**  
   • **作用**：存放仓库的额外信息。
   • **关键文件**：`exclude`（本地忽略规则，类似 `.gitignore`，但仅对当前仓库生效）。

10. **`description`**  
    ◦ **作用**：供 GitWeb（Git 的网页界面）显示仓库描述。
    ◦ **实际用途**：较少使用，可忽略。

---

### **总结**
• **用户可操作**：`config`（配置）、`hooks/`（钩子）、`info/exclude`（本地忽略文件）。
• **Git 自动管理**：`HEAD`、`index`、`objects/`、`refs/`、`logs/`。
• **无需手动修改**：`COMMIT_EDITMSG`、`description`。

⚠️ **警告**：直接修改 `.git` 内的文件可能导致仓库损坏（除非明确知道操作后果）。建议优先通过 Git 命令操作。
