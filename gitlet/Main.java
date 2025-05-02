package gitlet;

import java.util.Scanner;

import static gitlet.Repository.*;
/** Driver class for Gitlet, a subset of the Git version-control system.
 *  @author TODO
 */
public class Main {

    /** Usage: java gitlet.Main ARGS, where ARGS contains
     *  <COMMAND> <OPERAND1> <OPERAND2> ... 
     */
    public static void main(String[] args) {
        // TODO: what if args is empty?
        checkArgsEmpty(args);
        String firstArg = args[0];

        try {
            switch(firstArg) {
                case "init":
                    // TODO: handle the `init` command
                    if(args.length != 1){
                        throw new GitletException("Incorrect number of operands");
                    }
                    Repository.initPersistence();
                    break;
                case "add":
                    // TODO: handle the `add [filename]` command
                    String addFileName = args[1];
                    addStage(addFileName);
                    break;
                // TODO: FILL THE REST IN
                case "commit":
                    String commitMsg =  args[1];
                    commitFile(commitMsg);
                    break;
                case "rm":
                    String removeFile = args[1];
                    removeStage(removeFile);
                    break;
                case "log":
                    if(args.length != 1){
                        throw new GitletException("Incorrect number of operands");
                    }
                    printLog();
                    break;
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
