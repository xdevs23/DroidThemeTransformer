package org.xdevs23.cli.dtt;

import org.xdevs23.cli.dtt.handler.CliHandler;
import org.xdevs23.dtt.gui.DttGui;
import org.xdevs23.dtt.gui.FromSourceGui;

import java.io.IOException;

public class DroidThemeTransformer {

    private DttGui mGui;

    private static final String[] welcomeMessage = {
        "",
            "DroidThemeTransformer",
            "Copyright (C) 2016 Simao Gomes Viana",
            "",
            "This program comes with ABSOLUTELY NO WARRANTY; for details type `license'.",
            "This is free software, and you are welcome to redistribute it",
            "under certain conditions; type `license' for details.",
            "",
            "For usage and help, type 'help'.",
    };

    public DroidThemeTransformer() {

    }

    private static void printText(String msg) {
        if(FromSourceGui.consoleBox != null)
            FromSourceGui.appendToConsole(msg);
        else System.out.print(msg);
    }

    public static void cout(String... msgs) {
        for ( String s : msgs )
            printText(s + "\n");
    }

    public static void print(String... msgs) {
        for ( String s : msgs )
            printText(s);
    }

    public static String readLine() {
        return System.console().readLine();
    }

    public void run(String[] args) throws IOException {
        if(args != null && args.length > 0) {
            if(args[0].equals("nogui")) {
                cout(welcomeMessage);
                CliHandler.openInterface();
                return;
            }
        }

        // Show UI if no cli
        mGui = new DttGui();
    }

    public static void dontCare() {
        // I don't care
    }

}
