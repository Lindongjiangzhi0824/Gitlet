package gitlet;

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
                    Repository.setupPersistence();
                    break;
                case "add":
                    // TODO: handle the `add [filename]` command
                    String addFileName = args[1];
                    addStage(addFileName);
                    break;
                // TODO: FILL THE REST IN
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
