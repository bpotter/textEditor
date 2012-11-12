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
package org.jdesktop.wonderland.modules.textEditor.server;

import com.sun.sgs.app.ManagedReference;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.jdesktop.wonderland.common.ExperimentalAPI;
import org.jdesktop.wonderland.common.cell.ClientCapabilities;
import org.jdesktop.wonderland.common.cell.messages.CellMessage;
import org.jdesktop.wonderland.common.cell.state.CellClientState;
import org.jdesktop.wonderland.common.cell.state.CellServerState;
import org.jdesktop.wonderland.common.messages.ErrorMessage;
import org.jdesktop.wonderland.modules.appbase.server.cell.App2DCellMO;
import org.jdesktop.wonderland.modules.textEditor.common.*;
import org.jdesktop.wonderland.modules.textEditor.common.TextEditorCellClientState;
import org.jdesktop.wonderland.modules.textEditor.server.SharedText.AddTransform;
import org.jdesktop.wonderland.modules.textEditor.server.SharedText.DeleteTransform;
import org.jdesktop.wonderland.modules.textEditor.server.SharedText.MultiTransform;
import org.jdesktop.wonderland.modules.textEditor.server.SharedText.OldRevisionException;
import org.jdesktop.wonderland.modules.textEditor.server.SharedText.Transform;
import org.jdesktop.wonderland.modules.sharedstate.server.SharedMapSrv;
import org.jdesktop.wonderland.modules.sharedstate.server.SharedStateComponentMO;
import org.jdesktop.wonderland.modules.sharedstate.common.SharedBoolean;
import org.jdesktop.wonderland.modules.sharedstate.common.SharedInteger;
import org.jdesktop.wonderland.server.cell.AbstractComponentMessageReceiver;
import org.jdesktop.wonderland.server.cell.CellMO;
import org.jdesktop.wonderland.server.cell.annotation.UsesCellComponentMO;
import org.jdesktop.wonderland.server.comms.WonderlandClientID;
import org.jdesktop.wonderland.server.comms.WonderlandClientSender;

/**
 * Server cell for code viewer
 *
 * @author Jonathan Kaplan <jonathankap@gmail.com>
 */
@ExperimentalAPI
public class TextEditorCellMO extends App2DCellMO {
    private static final Logger logger =
            Logger.getLogger(TextEditorCellMO.class.getName());

    @UsesCellComponentMO(SharedStateComponentMO.class)
    private ManagedReference<SharedStateComponentMO> sscRef;

    private SharedText text;
    private String fileName;
    private String contentType;


    public TextEditorCellMO() {
        super();
        addComponent(new SharedStateComponentMO(this));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getClientCellClassName(WonderlandClientID clientID, ClientCapabilities capabilities) {
        return "org.jdesktop.wonderland.modules.textEditor.client.TextEditorCell";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void setLive(boolean live) {
        super.setLive(live);
        if (live) {
            // make sure we have a text object
            if (text == null) {
                text = new SharedText();
            }

            // get or create the shared maps we use
            SharedMapSrv statusMap = sscRef.get().get(TextEditorConstants.SETTINGS);

            // write the default server state to the map
            initializeState(statusMap);

            // register for messages
            CodeMessageReceiver receiver = new CodeMessageReceiver(this);
            channelRef.get().addMessageReceiver(TextEditorCellInsertMessage.class, receiver);
            channelRef.get().addMessageReceiver(TextEditorCellDeleteMessage.class, receiver);
            channelRef.get().addMessageReceiver(TextEditorNewContentMessage.class, receiver);
            channelRef.get().addMessageReceiver(TextEditorCellSyncMessage.class, receiver);
        } else {
            channelRef.get().removeMessageReceiver(TextEditorCellInsertMessage.class);
            channelRef.get().removeMessageReceiver(TextEditorCellDeleteMessage.class);
            channelRef.get().removeMessageReceiver(TextEditorNewContentMessage.class);
            channelRef.get().removeMessageReceiver(TextEditorCellSyncMessage.class);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CellClientState getClientState(CellClientState cellClientState,
                                          WonderlandClientID clientID, ClientCapabilities capabilities) {
        if (cellClientState == null) {
            cellClientState = new TextEditorCellClientState();
        }

        ((TextEditorCellClientState) cellClientState).setVersion(text.getVersion());
        ((TextEditorCellClientState) cellClientState).setText(text.getText());
        ((TextEditorCellClientState) cellClientState).setFileName(fileName);

        return super.getClientState(cellClientState, clientID, capabilities);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CellServerState getServerState(CellServerState state) {
        if (state == null) {
            state = new TextEditorCellServerState();
        }

        ((TextEditorCellServerState) state).setText(text.getText());
        ((TextEditorCellServerState) state).setFileName(fileName);
        ((TextEditorCellServerState) state).setContentType(contentType);
        return super.getServerState(state);
    }

    @Override
    public void setServerState(CellServerState state) {
        super.setServerState(state);

        text = new SharedText(((TextEditorCellServerState) state).getText());
        fileName = ((TextEditorCellServerState) state).getFileName();
        contentType = ((TextEditorCellServerState) state).getContentType();
    }

    private void initializeState(SharedMapSrv map) {
        if (map.get(TextEditorConstants.PREF_WIDTH) == null) {
            map.put(TextEditorConstants.PREF_WIDTH,
                    SharedInteger.valueOf(TextEditorConstants.DEFAULT_WIDTH));
        }

        if (map.get(TextEditorConstants.PREF_HEIGHT) == null) {
            map.put(TextEditorConstants.PREF_HEIGHT,
                    SharedInteger.valueOf(TextEditorConstants.DEFAULT_HEIGHT));
        }

        if (map.get(TextEditorConstants.DECORATED) == null) {
            map.put(TextEditorConstants.DECORATED,
                    SharedBoolean.valueOf(true));
        }
    }

    private void handleInsert(WonderlandClientID clientID,
                              WonderlandClientSender sender,
                              TextEditorCellInsertMessage insert) {
        Transform add = new AddTransform(insert.getInsertionPoint(),
                insert.getText());

        try {
            Transform toSend = text.apply(clientID, insert.getVersion(), add);
            channelRef.get().sendAll(clientID, getUpdateMessage(toSend));
        } catch (OldRevisionException ore) {
            logger.log(Level.WARNING, "Old revision detected", ore);
            sender.send(clientID, new ErrorMessage(insert.getMessageID(), ore));
        }
    }

    private void handleDelete(WonderlandClientID clientID,
                              WonderlandClientSender sender,
                              TextEditorCellDeleteMessage delete) {
        Transform del = new DeleteTransform(delete.getDeletionPoint(),
                delete.getLength());

        try {
            Transform toSend = text.apply(clientID, delete.getVersion(), del);
            channelRef.get().sendAll(clientID, getUpdateMessage(toSend));
        } catch (OldRevisionException ore) {
            logger.log(Level.WARNING, "Old revision detected", ore);
            sender.send(clientID, new ErrorMessage(delete.getMessageID(), ore));
        }
    }

    private TextEditorCellMessage getUpdateMessage(Transform transform) {
        if (transform instanceof AddTransform) {
            AddTransform add = (AddTransform) transform;
            return new TextEditorCellInsertMessage(cellID, text.getVersion(),
                    add.getInsertionPoint(),
                    add.getText());
        } else if (transform instanceof DeleteTransform) {
            DeleteTransform delete = (DeleteTransform) transform;
            return new TextEditorCellDeleteMessage(cellID, text.getVersion(),
                    delete.getDeletionPoint(),
                    delete.getLength());
        } else if (transform instanceof MultiTransform) {
            MultiTransform multi = (MultiTransform) transform;
            TextEditorCellMultiChangeMessage out =
                    new TextEditorCellMultiChangeMessage(cellID, text.getVersion());
            for (Transform t : multi.getTransforms()) {
                out.getMessages().add(getUpdateMessage(t));
            }
            return out;
        } else {
            throw new IllegalArgumentException("Unexpected transform: " +
                    transform.getClass().getName());
        }
    }

    private static class CodeMessageReceiver extends AbstractComponentMessageReceiver {
        public CodeMessageReceiver(CellMO cellMO) {
            super(cellMO);
        }

        @Override
        public synchronized void messageReceived(WonderlandClientSender sender,
                                                 WonderlandClientID clientID,
                                                 CellMessage message) {
            if (message instanceof TextEditorCellInsertMessage) {
                ((TextEditorCellMO) getCell()).handleInsert(clientID, sender,
                        (TextEditorCellInsertMessage) message);
            } else if (message instanceof TextEditorCellDeleteMessage) {
                ((TextEditorCellMO) getCell()).handleDelete(clientID, sender,
                        (TextEditorCellDeleteMessage) message);
            } else if (message instanceof TextEditorCellSyncMessage) {
                ((TextEditorCellMO) getCell()).handleSync(clientID, sender,
                        (TextEditorCellSyncMessage) message);
            } else if (message instanceof TextEditorNewContentMessage) {
                ((TextEditorCellMO) getCell()).handleNewContent(clientID, sender,
                        (TextEditorNewContentMessage) message);
            } else {
                logger.warning("Unexpected message type: " +
                        message.getClass().getName());
            }
        }
    }

    private void handleNewContent(WonderlandClientID clientID, WonderlandClientSender sender, TextEditorNewContentMessage message) {
        logger.severe("new Content");
        text.setText(message.getText());

        TextEditorCellSyncMessage syncMessage = new TextEditorCellSyncMessage(message.getCellID(), 1);
        message.setText(text.getText());
        logger.severe("new Content sync message - " + syncMessage.toString());
        channelRef.get().sendAll(clientID, syncMessage);

    }

    private void handleSync(WonderlandClientID clientID, WonderlandClientSender sender,
                            TextEditorCellSyncMessage message) {

        message.setVersion(text.getVersion());
        message.setText(text.getText());
        sender.send(clientID, message);

    }
}
