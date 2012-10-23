package org.jdesktop.wonderland.modules.textEditor.client;

import org.jdesktop.wonderland.client.cell.asset.AssetUtils;
import org.jdesktop.wonderland.modules.textEditor.common.TextEditorCellClientState;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.logging.Logger;

/**
 * Created with IntelliJ IDEA.
 * User: Bob
 * Date: 20/10/12
 * Time: 10:06 PM
 * To change this template use File | Settings | File Templates.
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
            BufferedReader reader = new BufferedReader(inputStreamReader);


            StringBuffer buffer = new StringBuffer();
            String line;
            while ((line = reader.readLine() ) != null)
              buffer.append(line).append('\n');
            reader.close();
            return buffer.toString();
        } catch (IOException e) {
            logger.severe("File not found or cannot be accessed - " + uri);
        }



        return null;
    }

    public void exportFile(File selectedFile) {


        try {
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(new FileOutputStream(selectedFile));
            outputStreamWriter.write(cell.getCurrentText());
            outputStreamWriter.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }


    }
}
