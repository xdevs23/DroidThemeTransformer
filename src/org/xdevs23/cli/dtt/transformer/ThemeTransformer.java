package org.xdevs23.cli.dtt.transformer;

import static org.xdevs23.cli.dtt.DroidThemeTransformer.print;
import static org.xdevs23.cli.dtt.DroidThemeTransformer.cout;

public class ThemeTransformer {

    private static final String
            CMTE        = "cmte",
            LAYERS      = "layers"
                    ;

    private ThemeTransformer() {

    }

    public static ThemeTransformer newInstance() {
        return new ThemeTransformer();
    }

    public static void startNewTransform(String[] args) {
        newInstance().transformTheme(args);
    }

    private void transformTheme(String[] args) {
        if(args == null || args.length <= 0
                || !args[0].contains("=") || args[0].indexOf("=") == args[0].length() - 1) {
            cout("Please specify a valid theme type");
            return;
        }

        final String themeType = args[0].split("=")[1];
        switch(themeType) {
            case CMTE:
                cout(
                        "You decided to transform a layers theme into a CM theme.",
                        "Let's start!"
                );

                break;
            case LAYERS:
                cout(
                        "You decided to transform a CM theme into a layers theme.",
                        "Let's start!"
                );

                break;
            default:
                print("Theme type ", themeType, " is unknown.", "\n");
                break;
        }
    }

}
