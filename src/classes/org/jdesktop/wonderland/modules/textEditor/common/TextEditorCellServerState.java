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

import java.io.Serializable;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import org.jdesktop.wonderland.common.cell.state.annotation.ServerState;
import org.jdesktop.wonderland.modules.appbase.common.cell.App2DCellServerState;

/**
 * The WFS server state class for TextEditorCellMO
 * @author Jonathan Kaplan <jonathankap@gmail.com>
 */
@XmlRootElement(name = "text-editor-cell")
@ServerState
public class TextEditorCellServerState extends App2DCellServerState implements Serializable {
    @XmlElement
    private String text;
    @XmlElement
    private String fileName;
    @XmlElement
    private String contentType;

    public TextEditorCellServerState() {
    }

    public String getServerClassName() {
        return "org.jdesktop.wonderland.modules.textEditor.server.TextEditorCellMO";
    }

    @XmlTransient
    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
    @XmlTransient
    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
    @XmlTransient
    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }
}
