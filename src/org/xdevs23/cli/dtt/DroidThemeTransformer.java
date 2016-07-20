package org.xdevs23.cli.dtt;

import org.xdevs23.cli.dtt.handler.CliHandler;

import java.io.IOException;

public class DroidThemeTransformer {

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

    public static void cout(String... msgs) {
        for ( String s : msgs )
            System.out.println(s);
    }

    public static void print(String... msgs) {
        for ( String s : msgs )
            System.out.print(s);
    }

    public static String readLine() {
        return System.console().readLine();
    }

    public void run(String[] args) throws IOException {
        cout(welcomeMessage);
        CliHandler.openInterface();
    }

    public static void dontCare() {
        // I don't care
    }

}
