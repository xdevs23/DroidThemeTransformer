package org.xdevs23.cli.dtt.handler;

import org.xdevs23.cli.dtt.DroidThemeTransformer;
import org.xdevs23.cli.dtt.misc.GPLv3License;
import org.xdevs23.cli.dtt.misc.HelpText;

import static org.xdevs23.cli.dtt.handler.cli.Commands.*;
import static org.xdevs23.cli.dtt.DroidThemeTransformer.print;
import static org.xdevs23.cli.dtt.DroidThemeTransformer.cout;

public class CliHandler {

    public static final int EXIT_CODE = 255;

    public static int process(String[] args) {
        if(args == null || args.length <= 0)
            return 0;
        String cmd = args[0];
        String[] cmdArgs = new String[args.length - 1];
        System.arraycopy(args, 1, cmdArgs, 0, args.length - 1);

        switch(cmd) {
            case QUIT_CLI:
            case EXIT_CLI: return EXIT_CODE;
            case HELP:
                cout(HelpText.helpText);
                break;
            case TRANSFORM:
                break;
            case LICENSE:
                cout(GPLv3License.license);
                break;
            case "":
                cout("Please specify a command.");
                break;
            default:
                cout("Command '" + cmd + "' not recognized.");
                break;
        }

        return 0;
    }

    /**
     * Give the user a command line
     */
    public static void openInterface() {
        while(true) {
            print("DTT> ");
            if(process(System.console().readLine()
                    .trim() // Remove whitespaces before and after the whole line
                    // Ensure every argument is separated only by one space
                    .replaceAll("(([ ])(([ ])+))", " ")
                    .split(" ") // Split the arguments into an array
                ) == EXIT_CODE) // Exit the appliation if EXIT_CODE is returned
                return;
        }
    }

}
