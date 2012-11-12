package org.jdesktop.wonderland.modules.textEditor.common;

import org.jdesktop.wonderland.common.cell.CellID;

/**
 * Created with IntelliJ IDEA.
 * User: Bob
 * Date: 24/10/12
 * Time: 7:25 PM
 * To change this template use File | Settings | File Templates.
 */
public class TextEditorNewContentMessage extends TextEditorCellMessage {

    public TextEditorNewContentMessage(CellID cellID, long version) {
        super(cellID, version);
    }

    private String text;

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
