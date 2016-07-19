package org.xdevs23.cli.dtt.handler;

import org.xdevs23.cli.dtt.DroidThemeTransformer;

public class CliHandler {

    public static final int EXIT_CODE = 255;

    public static int process(String[] args) {
        if(args == null || args.length <= 0)
            return 0;
        String mainArg = args[0];
        String[] argArgs = new String[args.length - 1];
        System.arraycopy(args, 1, argArgs, 0, args.length - 1);

        switch(mainArg) {
            case "quit":
            case "exit": return EXIT_CODE;
            default: break;
        }

        return 0;
    }

    /**
     * Give the user a command line
     */
    public static void openInterface() {
        while(true) {
            DroidThemeTransformer.print("DTT> ");
            if(process(System.console().readLine().split(" ")) == EXIT_CODE) return;
        }
    }

}
