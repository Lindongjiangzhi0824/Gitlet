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
        String firstArg = args[0];
        if (args.length == 0){
            Utils.exitWithError("Must have at least one argument");
        }
        String text;
        switch(firstArg) {
            case "init":
                // TODO: handle the `init` command
                Repository.setupPersistence();
                break;
            case "add":
                // TODO: handle the `add [filename]` command
                String addFileName = args[1];
                addStage
                break;
            // TODO: FILL THE REST IN
        }
    }

    public static void validateNumArgs(String cmd,String[] args, int n) {
        if(args.length != n){
            throw new RuntimeException{
                String.format("Invalid number of arguments for: %s.", cmd));
            }
        }
    }
}
