package org.xdevs23.cli.dtt;

import java.io.IOException;

public class Main {

    public static void main(String args[]) throws IOException {
        try {
            // Start the magic
            (new DroidThemeTransformer()).run(args);
        } catch(IOException ex) {
            DroidThemeTransformer.cout("An error occured: " + ex.getMessage());
            throw ex;
        }
    }

}
