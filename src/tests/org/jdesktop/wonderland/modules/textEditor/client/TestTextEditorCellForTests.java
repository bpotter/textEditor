package org.jdesktop.wonderland.modules.textEditor.client;

import org.junit.Test;

import static junit.framework.Assert.assertEquals;

/**
 * Created with IntelliJ IDEA.
 * User: Bob
 * Date: 26/11/12
 * Time: 6:05 PM
 * To change this template use File | Settings | File Templates.
 */
public class TestTextEditorCellForTests{

    @Test
    public void testInsertString() throws Exception {
        String initalString = "abcdef";
        String returnString = "abc123def";

        TextEditorCellForTests cell = new TextEditorCellForTests(null,null);
        cell.setWorkingString(initalString);
        cell.insertLocalText(3, "123");
        assertEquals("did not insert correctly", returnString, cell.getWorkingString());

    }
}
