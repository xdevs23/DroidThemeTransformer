package org.xdevs23.cli.dtt.handler;

import org.xdevs23.cli.dtt.misc.GPLv3License;
import org.xdevs23.cli.dtt.misc.HelpText;
import org.xdevs23.cli.dtt.transformer.ThemeTransformer;

import static org.xdevs23.cli.dtt.DroidThemeTransformer.cout;
import static org.xdevs23.cli.dtt.DroidThemeTransformer.print;
import static org.xdevs23.cli.dtt.handler.cli.Commands.EXIT_CLI;
import static org.xdevs23.cli.dtt.handler.cli.Commands.HELP;
import static org.xdevs23.cli.dtt.handler.cli.Commands.LICENSE;
import static org.xdevs23.cli.dtt.handler.cli.Commands.QUIT_CLI;
import static org.xdevs23.cli.dtt.handler.cli.Commands.TRANSFORM;

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
                ThemeTransformer.startNewTransform(cmdArgs);
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
