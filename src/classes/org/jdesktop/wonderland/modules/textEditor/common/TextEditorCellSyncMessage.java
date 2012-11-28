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
package org.jdesktop.wonderland.modules.textEditor.common;

import org.jdesktop.wonderland.common.cell.CellID;

/**
 *
 * @author Bob Potter <bpotter@acm.org>
 *
 *
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
