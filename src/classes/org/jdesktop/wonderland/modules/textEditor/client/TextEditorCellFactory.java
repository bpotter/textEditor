package org.jdesktop.wonderland.modules.textEditor.client;

import org.jdesktop.wonderland.client.cell.registry.annotation.CellFactory;
import org.jdesktop.wonderland.client.cell.registry.spi.CellFactorySPI;
import org.jdesktop.wonderland.common.cell.state.CellServerState;
import org.jdesktop.wonderland.modules.textEditor.common.TextEditorCellClientState;
import org.jdesktop.wonderland.modules.textEditor.common.TextEditorCellServerState;

import java.awt.*;
import java.util.Properties;

/**
 * Created with IntelliJ IDEA.
 * User: Bob
 * Date: 19/10/12
 * Time: 10:01 PM
 * To change this template use File | Settings | File Templates.
 */
@CellFactory
public class TextEditorCellFactory implements CellFactorySPI {
    public String[] getExtensions() {
        return new String[] {"txt", "log"};
    }

    public <T extends CellServerState> T getDefaultCellServerState(Properties properties) {
        TextEditorCellServerState state = new TextEditorCellServerState();

        TextEditorCellClientState clientState = null;
        if (properties != null) {
            String uri = properties.getProperty("content-uri");
            if (uri != null) {
                // read the file
                clientState = new TextEditorCellClientState();
                clientState.setText(TextEditorImportExportHelper.importFile(uri));

            }
        }
        if (clientState == null) {
            clientState = new TextEditorCellClientState();
        }
        state.setText(clientState.getText());
        return (T) state;}

    public String getDisplayName() {
        return "Text Editor";

    }

    public Image getPreviewImage() {
        return null;
    }
}
