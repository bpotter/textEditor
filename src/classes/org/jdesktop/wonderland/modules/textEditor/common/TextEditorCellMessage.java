/**
 * Open Wonderland
 *
 * Copyright (c) 2010-2012, Open Wonderland Foundation, All Rights Reserved
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
import org.jdesktop.wonderland.common.cell.messages.CellMessage;

/**
 * Base class for versioned messages for the TextEditorCell
 *
 * @author Bob Potter <bpotter@acm.org>
 *
 * From code developed by
 * @author Jonathan Kaplan <jonathankap@gmail.com>
 *
 *
 */
public abstract class TextEditorCellMessage extends CellMessage {
    protected long version;
    protected int offset;

    public TextEditorCellMessage(CellID cellID, long version) {
        super (cellID);

        this.version = version;
    }

    public long getVersion() {
        return version;
    }

    public int getOffset() {
        return offset;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }
}
