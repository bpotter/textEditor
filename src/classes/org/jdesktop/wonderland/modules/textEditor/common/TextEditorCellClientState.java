/**
 * Open Wonderland
 *
 * Copyright (c) 2010, Open Wonderland Foundation, All Rights Reserved
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

import org.jdesktop.wonderland.modules.appbase.common.cell.App2DCellClientState;

/**
 * Client state for code cell
 * @author Jonathan Kaplan <jonathankap@gmail.com>
 */
public class TextEditorCellClientState extends App2DCellClientState {
    private String text;
    private String fileName;
    private String contentType;
    private long version;

    public TextEditorCellClientState() {
    }

    public long getVersion() {
        return version;
    }

    public void setVersion(long version) {
        this.version = version;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getFileName() {
        if (fileName == null) {
            return " ";
        }
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getContentType() {
        if (contentType == null) {
            contentType = "text/plain";
        }
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }
}
