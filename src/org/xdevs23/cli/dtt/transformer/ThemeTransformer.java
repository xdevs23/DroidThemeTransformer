package org.xdevs23.cli.dtt.transformer;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xdevs23.debugutils.StackTraceParser;

import java.io.File;
import java.util.ArrayList;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import static org.xdevs23.cli.dtt.DroidThemeTransformer.print;
import static org.xdevs23.cli.dtt.DroidThemeTransformer.cout;
import static org.xdevs23.cli.dtt.DroidThemeTransformer.readLine;

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
                cout("Sorry, but unfortunately CMTE is not implemented yet.");
                break;
            case LAYERS:
                cout(
                        "You decided to transform a CM theme into a layers theme.",
                        "Let's start!"
                );
                String cmDir, resultDir;
                print("CM theme overlays directory: ");
                cmDir = readLine();
                print("Output directory: ");
                resultDir = readLine();
                cout("");
                cout("Let the magic begin!");
                cout("");
                cout("Collecting common colors...");
                DocumentBuilderFactory factory;
                DocumentBuilder builder;
                Document doc;
                ArrayList<String> commonColorKeys, commonColorValues;
                try {
                    factory =
                            DocumentBuilderFactory.newInstance();
                    builder = factory.newDocumentBuilder();
                    commonColorKeys   = new ArrayList<>();
                    commonColorValues = new ArrayList<>();
                    for ( File file : (new File(cmDir)).listFiles()) {
                        cout("Found file " + file.getPath());
                        doc = builder.parse(file);
                        Element root = doc.getDocumentElement();
                        NodeList colorNodes = root.getElementsByTagName("color");
                        for ( int i = 0; i < colorNodes.getLength(); i++) {
                            commonColorKeys.add(i, colorNodes.item(i).getAttributes()
                                                    .getNamedItem("name").getNodeValue());
                            commonColorValues.add(i, colorNodes.item(i).getTextContent());
                        }
                    }
                } catch(Exception ex) {
                    cout(StackTraceParser.parse(ex));
                    break;
                }

                break;
            default:
                print("Theme type ", themeType, " is unknown.", "\n");
                break;
        }
    }

}
