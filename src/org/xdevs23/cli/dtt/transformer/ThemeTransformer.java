package org.xdevs23.cli.dtt.transformer;

import java.util.ArrayList;
import java.util.function.Consumer;

import static org.xdevs23.cli.dtt.DroidThemeTransformer.cout;
import static org.xdevs23.cli.dtt.DroidThemeTransformer.print;

public class ThemeTransformer {

    private static final String
            CMTE        = "cmte",
            LAYERS      = "layers"
                    ;

    boolean isCommonResolved = false;

    private ThemeTransformer() {

    }

    public static ThemeTransformer newInstance() {
        return new ThemeTransformer();
    }

    public static void startNewTransform(String[] args) {
        newInstance().transformTheme(args);
    }

    protected static boolean needResolveColor(String input) {
         return input.startsWith("@*common:color/") || input.startsWith("@color/");
    }

    protected static String resolveColor(String input,
                               ArrayList<String> avColVal, ArrayList<String> colKeys) {
        String inValue, outValue;
        inValue = input;
        if (needResolveColor(inValue)) {
            int indexForR = colKeys.lastIndexOf(
                    inValue
                            .replace("@*common:color/", "")
                            .replace("@color/", "")
            );
            outValue = indexForR == -1 ? inValue : avColVal.get(indexForR);
        } else outValue = inValue;
        return outValue;
    }

    protected void checkResolveNecessary(ArrayList<String> colorValues) {
        isCommonResolved = true;
        colorValues.forEach(new Consumer<String>() {
            @Override
            public void accept(String s) {
                if(needResolveColor(s))
                    isCommonResolved = false;
            }
        });
    }

    private void transformTheme(String[] args) {
        if(args == null || args.length <= 0
                || !args[0].contains("=") || args[0].indexOf("=") == args[0].length() - 1) {
            cout("Please specify a valid theme type");
            return;
        }

        boolean noPrompt = (args.length == 3 && args[1].contains("id") && args[2].contains("od"));
        String inputDir = "", outputDir = "";
        if(noPrompt) {
            inputDir  = args[1].split("=")[1];
            outputDir = args[2].split("=")[1];
        }

        final String themeType = args[0].split("=")[1];
        switch(themeType) {
            case CMTE:
                cout(
                        "You decided to transform a layers theme into a CM theme.",
                        "Let's start!"
                );
                cout("Sorry, but unfortunately CMTE is not implemented yet.");
                break;
            case LAYERS:
                if(!CmToLayersTransformer.startTransform(inputDir, outputDir, noPrompt, this))
                    cout("An error occured while transforming.");
                break;
            default:
                print("Theme type ", themeType, " is unknown.", "\n");
                break;
        }
    }

}
