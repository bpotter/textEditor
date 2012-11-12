package org.jdesktop.wonderland.modules.textEditor.common;

import org.jdesktop.wonderland.common.cell.CellID;

/**
 * Created with IntelliJ IDEA.
 * User: Bob
 * Date: 24/10/12
 * Time: 5:02 PM
 * To change this template use File | Settings | File Templates.
 */
public class TextEditorCellSyncMessage extends TextEditorCellMessage {

    public TextEditorCellSyncMessage(CellID cellID, long version) {
        super(cellID, version);
    }

    private String text;

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public void setVersion(long version){
        this.version = version;
    }

    @Override
    public String toString() {
        return "TextEditorCellSyncMessage - Version " + version;
    }
}
