package org.jdesktop.wonderland.modules.textEditor.client;

/**
 * Created with IntelliJ IDEA.
 * User: Bob
 * Date: 26/11/12
 * Time: 5:51 PM
 * To change this template use File | Settings | File Templates.
 */

import org.junit.Test;

import static org.junit.Assert.*;

public class TestMessageSync  {

    @Test
    public void testNothing() {

    }

    @Test
    public void testInsertSync () {
        String startingText = "0123456789";
        String foreignModification = "abc";
        int foreignInsertPoint = 7;

        String localModification = "xyz";
        int localInsertPoint = 2;

        String finalText = "012xyz234567abc89";
        TextEditorCellForTests cell = new TextEditorCellForTests(null, null);
        cell.setWorkingString(startingText);
        cell.insertLocalText(2,localModification);





    }

}
