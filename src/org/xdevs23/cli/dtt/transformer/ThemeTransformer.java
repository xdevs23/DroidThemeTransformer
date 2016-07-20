package org.xdevs23.cli.dtt.transformer;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xdevs23.cli.dtt.DroidThemeTransformer;
import org.xdevs23.debugutils.StackTraceParser;
import org.xdevs23.file.FileUtils;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FilenameFilter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.function.Consumer;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import static org.xdevs23.cli.dtt.DroidThemeTransformer.print;
import static org.xdevs23.cli.dtt.DroidThemeTransformer.cout;
import static org.xdevs23.cli.dtt.DroidThemeTransformer.readLine;

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

    private static boolean needResolveColor(String input) {
         return input.startsWith("@*common:color/") || input.startsWith("@color/");
    }

    private static String resolveColor(String input,
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

    private void checkResolveNecessary(ArrayList<String> colorValues) {
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
                cout(
                        "You decided to transform a CM theme into a layers theme.",
                        "Let's start!",
                        ""
                );
                String cmDir = "", resultDir = "", cmCommonResDir = "";
                File   cmDirF = null, resultDirF = null, cmCommonResDirF;
                boolean isDataLoaded = false;
                if (new File(".", "details.txt").exists() && !noPrompt) {
                    print("Saved data found. Would you like to use that? [Y/n] ");
                    if (!readLine().toLowerCase().startsWith("n")) {
                        cout("  - Reading data...");
                        String loadedData = FileUtils
                                .readFileString((new File(".", "details.txt")).getAbsolutePath(),
                                        "");
                        cout("  - Checking data...");
                        isDataLoaded = (loadedData != null && loadedData.length() > 3 /* 3 x LF */);
                        if (isDataLoaded) {
                            cout("  - Processing data...");
                            String[] dirs = loadedData.split("\n");
                            cmDir = dirs[0];
                            resultDir = dirs[1];
                            cmDirF = new File(cmDir);
                            resultDirF = new File(resultDir);
                            cmCommonResDir = (new File(cmDir, "common/res/values/")).getAbsolutePath();
                            cmCommonResDirF = new File(cmCommonResDir);
                            cout("  => Data loaded successfully");
                        }
                    }
                }

                if(!isDataLoaded) {
                    print("CM theme overlays directory: ");
                    cmDir = (noPrompt ? inputDir : readLine());
                    cmCommonResDir = (new File(cmDir, "common/res/values/")).getAbsolutePath();
                    cmDirF = new File(cmDir);
                    cmCommonResDirF = new File(cmCommonResDir);
                    print("Output directory: ");
                    resultDir = (noPrompt ? outputDir : readLine());
                    resultDirF = new File(resultDir);
                    if(!noPrompt) {
                        cout("");
                        cout("Please confirm the following details: ");
                        cout("",
                                " Input directory: " + cmDirF.getAbsolutePath(),
                                "Output directory: " + resultDirF.getAbsolutePath(),
                                "CM Theme common colors directory: " + cmCommonResDirF.getAbsolutePath()
                        );

                        cout("Press enter if the data is correct or type in 'abort' to stop the operation.");
                        print("  > ");
                        if (readLine().equalsIgnoreCase("abort")) {
                            cout("Aborted.");
                            break;
                        }
                        print("Do you wish to save those details? [Y/n] ");
                        if (!readLine().toLowerCase().startsWith("n"))
                            FileUtils.writeFileString((new File(".", "details.txt")).getAbsolutePath(),
                                    cmDirF.getAbsolutePath() + "\n" +
                                            resultDirF.getAbsolutePath()
                            );
                    }
                }
                cout("");
                cout("Let the magic begin!");
                cout("");
                cout("Collecting common colors...");
                // Prepare stuff for XML reading
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
                    // Collect common colors
                    File[] commonResFiles = (new File(cmCommonResDir)).listFiles();
                    if(commonResFiles == null || commonResFiles.length <= 0) {
                        cout("Something went wrong: no common color files found " +
                                "(commonResFiles is null)");
                        break;
                    }
                    for ( File file : commonResFiles) {
                        cout("  - Found file " + file.getName());
                        doc = builder.parse(file);
                        Element root = doc.getDocumentElement();
                        NodeList colorNodes = root.getElementsByTagName("color");
                        for ( int i = 0; i < colorNodes.getLength(); i++) {
                            commonColorKeys.add(i, colorNodes.item(i).getAttributes()
                                                    .getNamedItem("name").getNodeValue());
                            commonColorValues.add(i, colorNodes.item(i).getTextContent());
                        }
                    }

                    cout("Resolving common color references in common color definitions...");

                    int depth = 1;
                    isCommonResolved = true;
                    checkResolveNecessary(commonColorValues);
                    while(!isCommonResolved) {
                        cout("  - Stage " + depth);
                        for (int i = 0; i < commonColorValues.size(); i++) {
                            if(needResolveColor(commonColorValues.get(i)))
                                commonColorValues.set(i, resolveColor(commonColorValues.get(i),
                                        commonColorValues, commonColorKeys));
                        }
                        checkResolveNecessary(commonColorValues);
                        depth++;
                    }
                    depth = 0;

                    // Replace the reference to common colors (looks like @*commom:color/color_name)
                    // with the actual color. Layers don't support common color reference
                    // in that way, so we need to replace them first.
                    cout("Resolving common colors in overlays...");
                    File[] overlays = cmDirF.listFiles();
                    if(overlays == null || overlays.length <= 0) {
                        cout("No overlays found.");
                        break;
                    }
                    for ( File file : overlays ) {
                        if (file.getName().equalsIgnoreCase("common")) continue;

                        File[] colorFiles = (new File(file, "res/values/")).listFiles(
                                new FilenameFilter() {
                            @Override
                            public boolean accept(File dir, String name) {
                                // Only accept files with color.xml filename
                                // as we are only processing colors
                                return name.equalsIgnoreCase("colors.xml");
                            }
                        });

                        if (colorFiles == null || colorFiles.length <= 0) {
                            print("  - Not processing ", file.getName(), ", no colors.xml found.\n");
                            continue;
                        }

                        print("  - Processing overlay ", file.getName(), "...\n");
                        // We need input (CMTE) and output (layers)
                        DocumentBuilderFactory
                                 inFactory = DocumentBuilderFactory.newInstance(),
                                outFactory = DocumentBuilderFactory.newInstance();
                        DocumentBuilder
                                 inBuilder =  inFactory.newDocumentBuilder(),
                                outBuilder = outFactory.newDocumentBuilder();
                        Document
                                // Only one color.xml can be available, so use colorFiles
                                // to get the file.
                                 inDocument =  inBuilder.parse(colorFiles[0]),
                                // Needs to be a new document
                                outDocument = outBuilder.parse(
                                        new ByteArrayInputStream(
                                                ("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
                                                        "<resources>    \n    \n</resources>")
                                                        .getBytes(StandardCharsets.UTF_8)
                                        )
                                );
                        Element inRoot = inDocument.getDocumentElement();
                        Element outRoot = outDocument.getDocumentElement();
                        NodeList inColorNodes = inRoot.getElementsByTagName("color");
                        for (int i = 0; i < inColorNodes.getLength(); i++) {
                            String inValue, outValue, colorName;
                            colorName = inColorNodes.item(i).getAttributes()
                                    .getNamedItem("name").getNodeValue();
                            inValue = inColorNodes.item(i).getTextContent();
                            outValue = resolveColor(inValue, commonColorValues, commonColorKeys);
                            Element newColorNode = outDocument.createElement("color");
                            newColorNode.setAttribute("name", colorName);
                            newColorNode.setTextContent(outValue);
                            outDocument.getDocumentElement().appendChild(newColorNode);
                        }

                        // Now save the new file
                        File resultFile = new File(resultDirF, "overlays/" +
                                file.getName() + "/res/values/color.xml");
                        if(!resultFile.getParentFile().mkdirs())
                            DroidThemeTransformer.dontCare();
                        TransformerFactory transformerFactory = TransformerFactory.newInstance();
                        Transformer transformer = transformerFactory.newTransformer();
                        DOMSource source = new DOMSource(outDocument);
                        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
                        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount",
                                "4");
                        StreamResult result =
                                new StreamResult(resultFile);
                        transformer.transform(source, result);
                    }
                    cout("All overlays processed!");
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
