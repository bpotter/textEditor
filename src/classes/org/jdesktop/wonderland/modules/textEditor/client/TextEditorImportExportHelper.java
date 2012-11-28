/**
 * Open Wonderland
 *
 * Copyright (c) 2012, Open Wonderland Foundation, All Rights Reserved
 *
 * Redistributions in source code form must reproduce the above
 * copyright and this condition.
 *
 * The contents of this file are subject to the GNU General Public
 * License, Version 2 (the "License"); you may not use this file
 * except in compliance with the License. A copy of the License is
 * available at http://www.opensource.org/licenses/gpl-license.php.
 *
 * The Open Wonderland Foundation designates this particular file as
 * subject to the "Classpath" exception as provided by the Open Wonderland
 * Foundation in the License file that accompanied this code.
 */
package org.jdesktop.wonderland.modules.textEditor.client;

import org.jdesktop.wonderland.client.cell.asset.AssetUtils;
import org.jdesktop.wonderland.modules.textEditor.common.TextEditorCellClientState;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.logging.Logger;

/**
 *
 * @author Bob Potter <bpotter@acm.org>
 *
 *
 *
 */
public class TextEditorImportExportHelper {

    private static final Logger logger =
            Logger.getLogger(TextEditorImportExportHelper.class.getName());

    private TextEditorCell cell;

    public TextEditorImportExportHelper(TextEditorCell cell) {
        this.cell = cell;
    }

    public static String importFile (String uri ) {
        logger.severe("import file " + uri);
        URL url = null;
        try {
            url = AssetUtils.getAssetURL(uri);
            InputStreamReader inputStreamReader = new InputStreamReader(url.openStream());
            return readContent(inputStreamReader);
        } catch (IOException e) {
            logger.severe("File not found or cannot be accessed - " + uri);
        }



        return null;
    }

    private static String readContent(InputStreamReader inputStreamReader) throws IOException {
        BufferedReader reader = new BufferedReader(inputStreamReader);


        StringBuffer buffer = new StringBuffer();
        String line;
        while ((line = reader.readLine() ) != null)
          buffer.append(line).append('\n');
        reader.close();
        return buffer.toString();
    }

    public void exportFile(File selectedFile) {


        try {
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(new FileOutputStream(selectedFile));
            String outputText = cell.getCurrentText();
            if (System.getProperty("os.name").toLowerCase().contains("windows")) {
              outputText = outputText.replaceAll("\n", "\r\n");
            }
            outputStreamWriter.write(outputText);
            outputStreamWriter.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }


    }

    public static String importFile(File selectedFile) {
        logger.severe("import file " + selectedFile.getName());
        try {
            InputStreamReader inputStreamReader= new InputStreamReader(new FileInputStream(selectedFile));
            return  readContent(inputStreamReader);
        } catch (IOException e) {
            logger.severe("File not found or cannot be accessed - " + selectedFile);
        }

        return null;

    }
}
