package org.xdevs23.cli.dtt.transformer;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xdevs23.cli.dtt.DroidThemeTransformer;
import org.xdevs23.debugutils.StackTraceParser;
import org.xdevs23.file.FileUtils;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FilenameFilter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import static org.xdevs23.cli.dtt.DroidThemeTransformer.cout;
import static org.xdevs23.cli.dtt.DroidThemeTransformer.print;
import static org.xdevs23.cli.dtt.DroidThemeTransformer.readLine;
import static org.xdevs23.cli.dtt.transformer.ThemeTransformer.needResolveColor;
import static org.xdevs23.cli.dtt.transformer.ThemeTransformer.needResolveDimen;
import static org.xdevs23.cli.dtt.transformer.ThemeTransformer.resolveColor;
import static org.xdevs23.cli.dtt.transformer.ThemeTransformer.resolveDimen;

public class CmToOmsTransformer {

    private CmToOmsTransformer() {

    }

    private static CmToOmsTransformer newInstance() {
        return new CmToOmsTransformer();
    }

    protected static boolean startTransform(String inputDir, String outputDir, boolean noPrompt,
                                         ThemeTransformer transformer) {
        return newInstance().transform(inputDir, outputDir, noPrompt, transformer);
    }

    private LoadedDirs loadDirs(boolean noPrompt, String inputDir, String outputDir) {
        LoadedDirs loadedDirs = new LoadedDirs();
        boolean isDataLoaded = false;
        String cmDir = "", resultDir = "", cmCommonResDir = "";
        File cmDirF = null, resultDirF = null, cmCommonResDirF = null;
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
            if(!noPrompt) print("CM theme overlays directory: ");
            cmDir = (noPrompt ? inputDir : readLine());
            cmCommonResDir = (new File(cmDir, "common/res/values/")).getAbsolutePath();
            cmDirF = new File(cmDir);
            cmCommonResDirF = new File(cmCommonResDir);
            if(!noPrompt) print("Output directory: ");
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
                    return null;
                }
                print("Do you wish to save those details? [Y/n] ");
                if (!readLine().toLowerCase().startsWith("n"))
                    FileUtils.writeFileString((new File(".", "details.txt")).getAbsolutePath(),
                            cmDirF.getAbsolutePath() + "\n" +
                                    resultDirF.getAbsolutePath()
                    );
            }
        }
        loadedDirs.add(0, cmDir);
        loadedDirs.add(1, cmCommonResDir);
        loadedDirs.add(2, resultDir);
        loadedDirs.getFiles().add(0, cmDirF);
        loadedDirs.getFiles().add(1, cmCommonResDirF);
        loadedDirs.getFiles().add(2, resultDirF);
        loadedDirs.setDataLoaded(isDataLoaded);
        return loadedDirs;
    }

    private ByteArrayInputStream getOutXmlSkeleton() {
        return new ByteArrayInputStream(("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
                "<!-- Generated by DroidThemeTransformer -->\n\n" +
                "<resources xmlns:android=\"http://schemas.android.com/apk/res/android\"></resources>").getBytes(StandardCharsets.UTF_8));
    }

    public boolean transform(String inputDir, String outputDir, boolean noPrompt,
                             ThemeTransformer themeTransformer) {
        cout(
                "You decided to transform a CM theme into a Substratum theme.",
                "Let's start!",
                ""
        );
        String cmDir = "", resultDir = "", cmCommonResDir = "";
        File cmDirF = null, resultDirF = null, cmCommonResDirF;
        boolean isDataLoaded = false;

        // Load the files
        LoadedDirs loadedDirs = loadDirs(noPrompt, inputDir, outputDir);
        if(loadedDirs == null) return false;

        isDataLoaded = loadedDirs.isDataLoaded();
        cmDir = loadedDirs.get(0);
        cmCommonResDir = loadedDirs.get(1);
        resultDir = loadedDirs.get(2);
        cmDirF = loadedDirs.getFiles().get(0);
        cmCommonResDirF = loadedDirs.getFiles().get(1);
        resultDirF = loadedDirs.getFiles().get(2);

        cout("");
        cout("Let the magic begin!");
        cout("");
        // Prepare stuff for XML reading
        DocumentBuilderFactory factory;
        DocumentBuilder builder;
        Document doc;
        ArrayList<String> commonColorKeys, commonColorValues, dimensKeys, dimensValues;
        try {
            cout("Collecting colors...");
            factory =
                    DocumentBuilderFactory.newInstance();
            builder = factory.newDocumentBuilder();
            commonColorKeys   = new ArrayList<>();
            commonColorValues = new ArrayList<>();
            dimensKeys        = new ArrayList<>();
            dimensValues      = new ArrayList<>();
            // Collect common colors
            File[] commonResFiles = (new File(cmCommonResDir)).listFiles();
            if(commonResFiles == null || commonResFiles.length <= 0) {
                cout("Something went wrong: no common color files found " +
                        "(commonResFiles is null)");
                return false;
            }
            File[] dimenFiles = new File[1];
            try {
                File[] overlays = cmDirF.listFiles();
                if(overlays == null || overlays.length <= 0) {
                    cout("No overlays found.");
                    return false;
                }

                for ( File file : overlays ) {
                    if (file.getName().equalsIgnoreCase("common")) continue;

                    print("  - Processing overlay ", file.getName(), "...\n");

                    File[] colorFiles = (new File(file, "res/values/")).listFiles(
                            new FilenameFilter() {
                                @Override
                                public boolean accept(File dir, String name) {
                                    // Only accept files with colors.xml filename
                                    // as we are only processing colors
                                    return name.equalsIgnoreCase("colors.xml");
                                }
                            });
                    DocumentBuilderFactory
                            inFactory,
                            outFactory;
                    DocumentBuilder
                            inBuilder,
                            outBuilder;
                    Document
                            inDocument,
                            outDocument;

                    if (colorFiles != null && colorFiles.length > 0) {
                        // We need input (CMTE) and output (oms)
                        inFactory = DocumentBuilderFactory.newInstance();
                        outFactory = DocumentBuilderFactory.newInstance();
                        inBuilder = inFactory.newDocumentBuilder();
                        // Only one colors.xml can be available, so use colorFiles
                        // to get the file
                        try {
                            inDocument = inBuilder.parse(colorFiles[0]);
                        } catch(Exception ex) {
                            cout("Failed to parse XML for colors in overlay " + file.getName());
                            cout(StackTraceParser.parse(ex));
                            return false;
                        }
                        // Needs to be a new document
                        outDocument = builder.parse(getOutXmlSkeleton());
                        Element inRoot = inDocument.getDocumentElement();
                        NodeList inColorNodes = inRoot.getElementsByTagName("color");
                        for (int i = 0; i < inColorNodes.getLength(); i++) {
                            String inValue, colorName;
                            colorName = inColorNodes.item(i).getAttributes()
                                    .getNamedItem("name").getNodeValue();
                            inValue = inColorNodes.item(i).getTextContent();
                            commonColorKeys.add(colorName);
                            commonColorValues.add(inValue);
                        }
                    }

                    dimenFiles = (new File(file, "res/values/")).listFiles(
                            new FilenameFilter() {
                                @Override
                                public boolean accept(File dir, String name) {
                                    // Only accept files with colors.xml filename
                                    // as we are only processing colors
                                    return name.equalsIgnoreCase("dimens.xml");
                                }
                            });

                    /// Dimens

                    if (dimenFiles != null && dimenFiles.length > 0) {
                        for (File dfile : dimenFiles) {
                            doc = builder.parse(dfile);
                            Element root = doc.getDocumentElement();
                            NodeList dimenNodes = root.getElementsByTagName("dimen");
                            for (int i = 0; i < dimenNodes.getLength(); i++) {
                                dimensKeys.add(dimenNodes.item(i).getAttributes()
                                        .getNamedItem("name").getNodeValue());
                                dimensValues.add(dimenNodes.item(i).getTextContent());
                            }
                        }
                    }

                    /// --
                }
            } catch(Exception ex2) {
                // Ignore
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

            cout("Resolving color references in common definitions...");

            int depth = 1;
            themeTransformer.isCommonResolved = true;
            themeTransformer.checkResolveNecessary(commonColorValues);
            while(!themeTransformer.isCommonResolved) {
                cout("  - Stage " + depth);
                for (int i = 0; i < commonColorValues.size(); i++) {
                    if(needResolveColor(commonColorValues.get(i)))
                        commonColorValues.set(i, resolveColor(commonColorValues.get(i),
                                commonColorValues, commonColorKeys));
                }
                themeTransformer.checkResolveNecessary(commonColorValues);
                depth++;
            }
            depth = 0;

            cout("Resolving dimensions...");

            depth = 1;
            themeTransformer.isDimenResolved = true;
            themeTransformer.checkDimenResolveNecessary(dimensValues);
            while(!themeTransformer.isDimenResolved) {
                cout("  - Stage " + depth);
                for (int i = 0; i < dimensValues.size(); i++) {
                    if(needResolveDimen(dimensValues.get(i)))
                        dimensValues.set(i, resolveDimen(dimensValues.get(i),
                                dimensValues, dimensKeys));
                }
                themeTransformer.checkDimenResolveNecessary(dimensValues);
                depth++;
            }
            depth = 0;

            // Replace the reference to common colors (looks like @*commom:color/color_name)
            // with the actual color. Layers don't support common color reference
            // in that way, so we need to replace them first.
            cout("Processing overlays...");
            File[] overlays = cmDirF.listFiles();
            if(overlays == null || overlays.length <= 0) {
                cout("No overlays found.");
                return false;
            }
            for ( File file : overlays ) {
                if (file.getName().equalsIgnoreCase("common")) continue;

                print("  - Processing overlay ", file.getName(), "...\n");

                File[] colorFiles = (new File(file, "res/values/")).listFiles(
                        new FilenameFilter() {
                            @Override
                            public boolean accept(File dir, String name) {
                                // Only accept files with colors.xml filename
                                // as we are only processing colors
                                return name.equalsIgnoreCase("colors.xml");
                            }
                        });
                DocumentBuilderFactory
                        inFactory,
                        outFactory;
                DocumentBuilder
                        inBuilder,
                        outBuilder;
                Document
                        inDocument,
                        outDocument;

                if(colorFiles != null && colorFiles.length > 0) {
                    cout("    - Processing colors...");
                    cout("     - Resolving common colors...");
                    // We need input (CMTE) and output (oms)
                    inFactory = DocumentBuilderFactory.newInstance();
                    outFactory = DocumentBuilderFactory.newInstance();
                    inBuilder = inFactory.newDocumentBuilder();
                    outBuilder = outFactory.newDocumentBuilder();
                    // Only one colors.xml can be available, so use colorFiles
                    // to get the file
                    try {
                        inDocument = inBuilder.parse(colorFiles[0]);
                    } catch(Exception ex) {
                        cout("Failed to parse XML for colors in overlay " + file.getName());
                        cout(StackTraceParser.parse(ex));
                        return false;
                    }
                    // Needs to be a new document
                    outDocument = builder.parse(getOutXmlSkeleton());
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

                    cout("     - Writing new color file...");
                    // Now save the new file
                    File resultFile = new File(resultDirF, "assets/overlays/" +
                            file.getName() + "/res/values/colors.xml");
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

                /// Dimens

                dimenFiles = (new File(file, "res/values/")).listFiles(
                        new FilenameFilter() {
                            @Override
                            public boolean accept(File dir, String name) {
                                // Only accept files with colors.xml filename
                                // as we are only processing colors
                                return name.equalsIgnoreCase("dimens.xml");
                            }
                        });

                /// Dimens


                if(dimenFiles != null && dimenFiles.length > 0) {
                    cout("     - Processing dimensions...");
                    // We need input (CMTE) and output (oms)
                    inFactory = DocumentBuilderFactory.newInstance();
                    outFactory = DocumentBuilderFactory.newInstance();
                    inBuilder = inFactory.newDocumentBuilder();
                    outBuilder = outFactory.newDocumentBuilder();
                    // Only one colors.xml can be available, so use colorFiles
                    // to get the file
                    try {
                        inDocument = inBuilder.parse(dimenFiles[0]);
                    } catch (Exception ex) {
                        cout("Failed to parse XML for dimen in overlay " + file.getName());
                        cout(StackTraceParser.parse(ex));
                        return false;
                    }
                    // Needs to be a new document
                    outDocument = builder.parse(getOutXmlSkeleton());
                    Element inRoot = inDocument.getDocumentElement();
                    Element outRoot = outDocument.getDocumentElement();
                    NodeList inDimenNodes = inRoot.getElementsByTagName("dimen");
                    for (int i = 0; i < inDimenNodes.getLength(); i++) {
                        String inValue, outValue, dimenName;
                        dimenName = inDimenNodes.item(i).getAttributes()
                                .getNamedItem("name").getNodeValue();
                        inValue = inDimenNodes.item(i).getTextContent();
                        outValue = resolveDimen(inValue, dimensValues, dimensKeys);
                        Element newDimenNode = outDocument.createElement("dimen");
                        newDimenNode.setAttribute("name", dimenName);
                        newDimenNode.setTextContent(outValue);
                        outDocument.getDocumentElement().appendChild(newDimenNode);
                    }

                    cout("     - Writing new dimen file...");
                    // Now save the new file
                    File resultFile = new File(resultDirF, "assets/overlays/" +
                            file.getName() + "/res/values/dimens.xml");
                    if (!resultFile.getParentFile().mkdirs())
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
                /// ---

                ArrayList<File> styleFiles = new ArrayList<>();

                for (File valuesFolder : new File(file, "res/").listFiles(new FilenameFilter() {
                    @Override
                    public boolean accept(File dir, String name) {
                        return name.startsWith("values");
                    }
                })) {
                    cout("  Found values folder " + valuesFolder.getName());
                    for ( File f : (new File(file, "res/" + valuesFolder.getName())).listFiles(
                            new FilenameFilter() {
                                @Override
                                public boolean accept(File dir, String name) {
                                    // Only accept files with styles.xml filename
                                    // as we are only processing styles
                                    return name.equalsIgnoreCase("styles.xml");
                                }
                            }))
                        styleFiles.add(f);
                }

                if(styleFiles.size() > 0) {
                    for(File styleFile : styleFiles) {
                        cout("    - Processing styles...");
                        String valuesFolder = styleFile.getParent();
                        valuesFolder = valuesFolder.substring(
                                valuesFolder.lastIndexOf("/") + 1, valuesFolder.length());

                        // We need input (CMTE) and output (layers)
                        inFactory = DocumentBuilderFactory.newInstance();
                        outFactory = DocumentBuilderFactory.newInstance();
                        inBuilder = inFactory.newDocumentBuilder();
                        outBuilder = outFactory.newDocumentBuilder();

                        cout("     - Resolving colors and dimensions...");
                        // Only one styles.xml can be available, so use stylesFiles
                        // to get the file.
                        try {
                            inDocument = inBuilder.parse(styleFile);
                        } catch (Exception ex) {
                            cout("Failed to parse XML for styles in overlay " + file.getName());
                            cout(StackTraceParser.parse(ex));
                            return false;
                        }
                        outDocument = outBuilder.parse(getOutXmlSkeleton());
                        Element inRoot = inDocument.getDocumentElement();
                        Element outRoot = outDocument.getDocumentElement();
                        NodeList styleNodes = inRoot.getElementsByTagName("style");
                        for (int i = 0; i < styleNodes.getLength(); i++) {
                            final NodeList inInStyleNodesTmp = styleNodes.item(i).getChildNodes();
                            NodeList inInStyleNodes = new NodeList() {
                                private ArrayList<Node> nodes = new ArrayList<>();

                                public Node addAllNonEmptyNodes(NodeList list) {
                                    for (int x = 0; x < list.getLength(); x++) {
                                        Node node = list.item(x);
                                        if (node != null && node.getAttributes() != null &&
                                                node.getAttributes().getLength() > 0 &&
                                                node.getTextContent() != null &&
                                                node.getAttributes().getNamedItem("name") != null)
                                            nodes.add(node);
                                    }
                                    return null;
                                }

                                @Override
                                public Node item(int index) {
                                    return (index == -1 ? addAllNonEmptyNodes(inInStyleNodesTmp)
                                            : nodes.get(index));
                                }

                                @Override
                                public int getLength() {
                                    return nodes.size();
                                }
                            };

                            inInStyleNodes.item(-1);

                            Element[] newStyleNodes = new Element[inInStyleNodes.getLength()];
                            String styleName, styleParent = "";
                            boolean hasStyleParent = styleNodes.item(i).getAttributes().getLength() > 1;
                            styleName = styleNodes.item(i).getAttributes().getNamedItem("name")
                                    .getNodeValue();
                            if (hasStyleParent) styleParent = styleNodes.item(i).getAttributes()
                                    .getNamedItem("parent").getNodeValue();
                            for (int ix = 0; ix < inInStyleNodes.getLength(); ix++) {
                                String inValue, outValue, nameAttrValue;
                                NamedNodeMap attrs = inInStyleNodes.item(ix).getAttributes();
                                Node nameAttr = (attrs == null ? null : attrs.getNamedItem("name"));
                                nameAttrValue = (nameAttr == null ? "" : nameAttr.getNodeValue());
                                if (!(inInStyleNodes.item(ix).getTextContent().contains("color/") ||
                                        inInStyleNodes.item(ix).getTextContent().startsWith("#"))) {
                                    outValue = inInStyleNodes.item(ix).getTextContent();
                                } else {
                                    inValue = inInStyleNodes.item(ix).getTextContent();
                                    outValue = resolveColor(inValue, commonColorValues, commonColorKeys);
                                }
                                if (inInStyleNodes.item(ix).getTextContent().contains("dimen/")) {
                                    inValue = inInStyleNodes.item(ix).getTextContent();
                                    outValue = resolveDimen(inValue, dimensValues, dimensKeys);
                                }
                                Element newInsideStyleNode = outDocument.createElement("item");
                                newInsideStyleNode.setAttribute("name", nameAttrValue);
                                newInsideStyleNode.setTextContent(outValue);
                                newStyleNodes[ix] = newInsideStyleNode;
                            }
                            Element newStyleElement = outDocument.createElement("style");
                            if (styleName.length() > 0)
                                newStyleElement.setAttribute("name", styleName);
                            if (hasStyleParent) newStyleElement.setAttribute("parent", styleParent);
                            for (int ix = 0; ix < newStyleNodes.length; ix++) {
                                Element element = newStyleNodes[ix];
                                if (element == null)
                                    cout("      Warning: element " + ix + " is null in style " + styleName);
                                else newStyleElement.appendChild(element);
                            }
                            outDocument.getDocumentElement().appendChild(newStyleElement);
                        }

                        cout("     - Writing new style file...");
                        // Now save the new file
                        File resultFile = new File(resultDirF, "assets/overlays/" +
                                file.getName() + "/res/" + valuesFolder + "/styles.xml");
                        if (!resultFile.getParentFile().mkdirs())
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
                }

                File[] drawableFiles = (new File(file, "res/drawable/")).listFiles(
                        new FilenameFilter() {
                            @Override
                            public boolean accept(File dir, String name) {
                                return name.matches(".*[.]xml"); // .xml only
                            }
                        });

                if(drawableFiles != null && drawableFiles.length > 0) {
                    cout("    - Processing drawables...");
                    // We need input (CMTE) and output (layers)
                    inFactory = DocumentBuilderFactory.newInstance();
                    outFactory = DocumentBuilderFactory.newInstance();
                    inBuilder = inFactory.newDocumentBuilder();
                    outBuilder = outFactory.newDocumentBuilder();

                    for (int dxf = 0; dxf < drawableFiles.length; dxf++) {
                        String drawableName = drawableFiles[dxf].getName();
                        try {
                            inDocument = inBuilder.parse(drawableFiles[dxf]);
                        } catch (Exception ex) {
                            cout("Failed to parse XML for drawable " + drawableFiles[dxf]
                                    + "in overlay " + file.getName());
                            cout(StackTraceParser.parse(ex));
                            return false;
                        }

                        StringBuilder outDocumentSkeleton = new StringBuilder();
                        outDocumentSkeleton.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n");
                        outDocumentSkeleton
                                .append("<")
                                .append(inDocument.getDocumentElement().getNodeName())
                                .append(" ")
                                ;
                        NamedNodeMap allInAttrs = inDocument.getDocumentElement().getAttributes();
                        if(allInAttrs != null)
                            for ( int attrId = 0; attrId < allInAttrs.getLength(); attrId++ )
                                outDocumentSkeleton
                                        .append(allInAttrs.item(attrId).getNodeName())
                                        .append("=\"")
                                        .append(allInAttrs.item(attrId).getNodeValue())
                                        .append("\" ")
                                ;
                        outDocumentSkeleton
                                .append("></")
                                .append(inDocument.getDocumentElement().getNodeName())
                                .append(">")
                                ;

                        outDocument = inBuilder.parse(new ByteArrayInputStream(
                                outDocumentSkeleton.toString().getBytes(StandardCharsets.UTF_8)
                        ));

                        Element inRoot = inDocument.getDocumentElement();
                        Element outRoot = outDocument.getDocumentElement();
                        cout("      - Resolving dimensions and colors...");
                        ThemeTransformer.ManagedNodeList newList =
                            ThemeTransformer.resolveNodes(inRoot, commonColorValues, commonColorKeys,
                                    dimensValues, dimensKeys);
                        for (Node node : newList.getAll())
                            outRoot.appendChild(outDocument.importNode(node, true));

                        cout("      - Writing new drawable file...");
                        // Now save the new file
                        File resultFile = new File(resultDirF, "assets/overlays/" +
                                file.getName() + "/res/drawable/" + drawableName);
                        if (!resultFile.getParentFile().mkdirs())
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
                }

                cout("    - Copying remaining resource files...");

                File[] resDirs = (new File(file.getAbsolutePath(), "res/")).listFiles();
                if(resDirs != null)
                    for ( File resDir : resDirs ) {
                        File[] resFiles = resDir.listFiles(new FilenameFilter() {
                            @Override
                            public boolean accept(File dir, String name) {
                                return ! ((dir.getName().matches("drawable") &&
                                            name.matches(".*[.]xml")) ||
                                            (name.toLowerCase()
                                                    .matches("colors[.]xml|styles[.]xml|dimens[.]xml")));
                            }
                        });
                        if(resFiles != null)
                            for ( File resFile : resFiles ) {
                                File copyFile = new File(resultDirF,
                                        "assets/overlays/" + file.getName()
                                                + "/res/"
                                                + resDir.getName() + "/"
                                                + resFile.getName());
                                if(copyFile.exists() &&
                                        copyFile.delete()) DroidThemeTransformer.dontCare();
                                if(!copyFile.getParentFile().mkdirs())
                                    DroidThemeTransformer.dontCare();
                                FileUtils.copy(resFile, copyFile);
                            }
                    }
            }
            cout("All overlays processed!");
            cout("The resources are transformed now. Please take a template and use the transformed " +
                    "files for your Substratum theme.");
        } catch(Exception ex) {
            cout(StackTraceParser.parse(ex));
            return false;
        }
        return true;
    }

    private class LoadedDirs extends ArrayList<String> {

        private boolean mDataLoaded = false;
        private ArrayList<File> files = new ArrayList<>();

        public boolean isDataLoaded() {
            return mDataLoaded;
        }

        public void setDataLoaded(boolean loaded) {
            mDataLoaded = loaded;
        }

        public ArrayList<File> getFiles() {
            return files;
        }

    }


}
