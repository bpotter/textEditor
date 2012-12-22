package org.jdesktop.wonderland.modules.textEditor.client;

import org.jdesktop.wonderland.client.cell.CellCache;
import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.modules.textEditor.common.TextEditorCellInsertMessage;
import org.jdesktop.wonderland.modules.textEditor.common.TextEditorCellMessage;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: Bob
 * Date: 26/11/12
 * Time: 5:57 PM
 * To change this template use File | Settings | File Templates.
 */
public class TextEditorCellForTests extends TextEditorCell {

    String workingString;

    List<TextEditorCellMessage> messages = new ArrayList<TextEditorCellMessage>();


    public TextEditorCellForTests(CellID cellID, CellCache cellCache) {
        super(null, null);
    }

    public void insertLocalText(int offset, String text) {
        String firstPart = workingString.substring(0, offset);
        String secondPart = workingString.substring(offset);
        workingString = firstPart + text + secondPart;

        //mimic sending message
        messages.add(new TextEditorCellInsertMessage(new CellID(0), 1, offset, text));

    }

    public String getWorkingString() {
        return workingString;
    }

    public void setWorkingString(String workingString) {
        this.workingString = workingString;
    }
}
