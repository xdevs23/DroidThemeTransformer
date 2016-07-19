package org.xdevs23.cli.dtt.misc;

/**
 * Again, simple java class for the help
 */
public final class HelpText {

    public static final String[] helpText = {
        "",
            "Help for DroidThemeTransformer",
            "",
            "Usage: <command> <options|arguments>",
            "",
            "Commands: ",
            "   help                Displays this help",
            "   license             Displays the license",
            "   exit                ",
            "   quit                Exit this application",
            "   transform           Transform a theme",
            "",
            "Options ending with an equality sign (=) need the argument directly after the sign, " +
            "no kind of spaces/whitespaces are allowed directly after the equality sign as every " +
            "such option needs an argument.",
            "",
            "Options for 'transform': ",
            "",
            "   to=                 What the theme should be transformed to",
            "                       Valid arguments are: cmte, layers",
            "",
    };

}
