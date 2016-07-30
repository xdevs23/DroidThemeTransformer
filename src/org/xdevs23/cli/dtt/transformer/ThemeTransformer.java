package org.xdevs23.cli.dtt.transformer;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.function.Consumer;

import static org.xdevs23.cli.dtt.DroidThemeTransformer.cout;
import static org.xdevs23.cli.dtt.DroidThemeTransformer.print;

public class ThemeTransformer {

    private static final String
            CMTE        = "cmte",
            OMS         = "oms"     // Substratum
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

    protected static Node resolveNode(Node node, ArrayList<String> acv, ArrayList<String> ck,
                                      boolean recursively) {
        if(node == null) return null;
        if(node.getTextContent() != null && needResolveColor(node.getTextContent()))
            node.setTextContent(resolveColor(node.getTextContent(), acv, ck));
        if(node.getAttributes() != null) {
            for (int i = 0; i < node.getAttributes().getLength(); i++) {
                if(node.getAttributes().item(i) != null) {
                    String attrName  = node.getAttributes().item(i).getNodeName();
                    String attrValue = node.getAttributes().item(i).getNodeValue();
                    if(needResolveColor(attrValue))
                        attrValue = resolveColor(attrValue, acv, ck);
                    node.getAttributes().item(i).setNodeValue(attrValue);
                }
            }
        }
        if(recursively && node.getChildNodes() != null && node.getChildNodes().getLength() > 0) {
            for (int i = 0; i < node.getChildNodes().getLength(); i++) {
                resolveNode(node.getChildNodes().item(i), acv, ck, true);
            }
        }
        return node;
    }

    protected static ManagedNodeList resolveNodes(Element rootElement,
                                                  ArrayList<String> acv, ArrayList<String> ck) {
        ManagedNodeList list = new ManagedNodeList();
        for (int i = 0; i < rootElement.getChildNodes().getLength(); i++)
            list.addNode(resolveNode(rootElement.getChildNodes().item(i), acv, ck, true));

        return list;
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
                        "You decided to transform a Substratum theme into a CM theme.",
                        "Let's start!"
                );
                cout("Sorry, but unfortunately CMTE is not implemented yet.");
                break;
            case OMS:
                if(!CmToOmsTransformer.startTransform(inputDir, outputDir, noPrompt, this))
                    cout("An error occured while transforming.");
                break;
            default:
                print("Theme type ", themeType, " is unknown.", "\n");
                break;
        }
    }

    public static class ManagedNodeList implements NodeList {
        private ArrayList<Node> mNodes = new ArrayList<>();

        @Override
        public Node item(int index) {
            return mNodes.get(index);
        }

        @Override
        public int getLength() {
            return mNodes.size();
        }

        public void addNode(Node node) {
            mNodes.add(node);
        }

        public Node[] getAll() {
            return mNodes.toArray(new Node[getLength()]);
        }
    }

}
