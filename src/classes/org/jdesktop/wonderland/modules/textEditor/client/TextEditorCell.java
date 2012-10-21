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
package org.jdesktop.wonderland.modules.textEditor.client;

import com.jme.math.Vector2f;
import java.math.BigInteger;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.jdesktop.wonderland.client.cell.CellCache;
import org.jdesktop.wonderland.client.cell.ChannelComponent.ComponentMessageReceiver;
import org.jdesktop.wonderland.client.cell.annotation.UsesCellComponent;
import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.common.cell.CellStatus;
import org.jdesktop.wonderland.common.cell.messages.CellMessage;
import org.jdesktop.wonderland.common.cell.state.CellClientState;
import org.jdesktop.wonderland.modules.appbase.client.App2D;
import org.jdesktop.wonderland.modules.appbase.client.cell.App2DCell;
import org.jdesktop.wonderland.modules.textEditor.common.*;
import org.jdesktop.wonderland.modules.textEditor.common.TextEditorCellClientState;
import org.jdesktop.wonderland.modules.sharedstate.client.SharedMapCli;
import org.jdesktop.wonderland.modules.sharedstate.client.SharedMapEventCli;
import org.jdesktop.wonderland.modules.sharedstate.client.SharedMapListenerCli;
import org.jdesktop.wonderland.modules.sharedstate.client.SharedStateComponent;
import org.jdesktop.wonderland.modules.sharedstate.common.SharedBoolean;
import org.jdesktop.wonderland.modules.sharedstate.common.SharedInteger;
import org.jdesktop.wonderland.modules.sharedstate.common.SharedString;

/**
 *
 * @author Jonathan Kaplan <jonathankap@gmail.com>
 */
public class TextEditorCell extends App2DCell
        implements  SharedMapListenerCli
{
    @UsesCellComponent
    private SharedStateComponent state;


    private final StringBuffer codeTemplate;

    private TextEditorApp textEditorApp;
    private TextEditorWindow textEditorWindow;
    private TextEditorCellClientState clientState;
    private SharedMapCli settings;

//    private ParentSortable sortable;

    public TextEditorCell(CellID cellID, CellCache cellCache) {
        super(cellID, cellCache);



        codeTemplate = new StringBuffer();
    }

    @Override
    public void setClientState(CellClientState clientState) {
        super.setClientState(clientState);

        this.clientState = (TextEditorCellClientState) clientState;
    }

    @Override
    protected void setStatus(CellStatus status, boolean increasing) {
        super.setStatus(status, increasing);

        if (status == CellStatus.ACTIVE && increasing) {
            settings = state.get(TextEditorConstants.SETTINGS);
            settings.addSharedMapListener(this);


            if (this.getApp() == null) {
                textEditorApp = new TextEditorApp("Text Editor", new Vector2f(0.01f, 0.01f));
                setApp(textEditorApp);
            }

            // tell the app to be displayed in this cell.
            textEditorApp.addDisplayer(this);

            // set initial position above ground
            float placementHeight = getPreferredHeight() + 200;
            placementHeight *= clientState.getPixelScale().y;
            setInitialPlacementSize(new Vector2f(0f, placementHeight));

            // this app has only one window, so it is always top-level
            textEditorWindow = new TextEditorWindow(this, textEditorApp,
                        getPreferredWidth(), getPreferredHeight(),
                        true, clientState.getPixelScale());
            textEditorWindow.setDecorated(getDecorated());
            textEditorApp.setWindow(textEditorWindow);

            // add a listener to update the document
            DocumentHandler handler = new DocumentHandler(textEditorWindow.getDocument(),
                                                          clientState.getVersion(),
                                                          clientState.getText());
            channel.addMessageReceiver(TextEditorCellInsertMessage.class, handler);
            channel.addMessageReceiver(TextEditorCellDeleteMessage.class, handler);
            channel.addMessageReceiver(TextEditorCellMultiChangeMessage.class, handler);

             // both the app and the user want this window to be visible
             textEditorWindow.setVisibleApp(true);
             textEditorWindow.setVisibleUser(this, true);

             syncState();
        } else if (status == CellStatus.DISK) {
            channel.removeMessageReceiver(TextEditorCellInsertMessage.class);
            channel.removeMessageReceiver(TextEditorCellDeleteMessage.class);
            channel.removeMessageReceiver(TextEditorCellMultiChangeMessage.class);

            settings.removeSharedMapListener(this);

            // the cell is no longer visible
            textEditorWindow.setVisibleApp(false);
            App2D.invokeLater(new Runnable() {
                public void run() {
                    textEditorWindow.cleanup();
                    textEditorWindow = null;
                }
            });
        }
    }

    protected int getPreferredWidth() {
        SharedInteger width = settings.get(TextEditorConstants.PREF_WIDTH,
                                           SharedInteger.class);
        return width.getValue();
    }

    protected int getPreferredHeight() {
        SharedInteger height = settings.get(TextEditorConstants.PREF_HEIGHT,
                                           SharedInteger.class);
        return height.getValue();
    }

    protected boolean getDecorated() {
        SharedBoolean decorated = settings.get(TextEditorConstants.DECORATED,
                                               SharedBoolean.class);
        return decorated.getValue();
    }





    public void highlightLine(int lineNumber) {
        settings.put(TextEditorConstants.HIGHLIGHT_LINE, SharedInteger.valueOf(lineNumber));
    }

    public void clearHighlight() {
        settings.remove(TextEditorConstants.HIGHLIGHT_LINE);
    }

//    public void highlightObjects(int... indices) {
//        ((SortCell) getParent()).requestHighlight(indices);
//
//        StringBuffer highlightVals = new StringBuffer();
//        for (int index : indices) {
//            String val;
//            try {
//                val = String.valueOf(sortable.getInternal(index));
//            } catch (IllegalArgumentException iae) {
//                val = "xxx";
//            }
//
//            highlightVals.append("(" + index + ")->" + val + "  ");
//        }
//        settings.put(TextEditorConstants.HIGHLIGHT_VALUES,
//                     SharedString.valueOf(highlightVals.toString()));
//    }

    public void requestStop() {
        settings.put(TextEditorConstants.STATUS, SharedString.valueOf(TextEditorConstants.STOP));
    }

//    public void setMode(Mode mode) {
//        // ignore
//    }
//
//    public void setStatus(Status status) {
//        String str;
//        if (status == Status.STOPPED) {
//            str = TextEditorConstants.STOP;
//        } else {
//            str = TextEditorConstants.RUN;
//        }
//
//        settings.put(TextEditorConstants.STATUS, SharedString.valueOf(str));
//    }

    public void propertyChanged(final SharedMapEventCli smec) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                if (smec.getPropertyName().equals(TextEditorConstants.HIGHLIGHT_LINE)) {
                    updateHighlightLine();
                } else if (smec.getPropertyName().equals(TextEditorConstants.GET_COUNT) ||
                           smec.getPropertyName().equals(TextEditorConstants.SWAP_COUNT))
                {
                    updateOperationCounts();
                } else if (smec.getPropertyName().equals(TextEditorConstants.STATUS)) {
                    updateStatus(smec.getSenderID());
                } else if (smec.getPropertyName().equals(TextEditorConstants.HIGHLIGHT_VALUES)) {
                    updateHighlightVals();
                }
            }
        });
    }

    private void syncState() {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                updateHighlightLine();
                updateOperationCounts();
                updateStatus(BigInteger.ZERO);
                updateHighlightVals();
            }
        });
    }

    private void updateOperationCounts() {
        SharedInteger getCount = settings.get(TextEditorConstants.GET_COUNT,
                                              SharedInteger.class);
        SharedInteger swapCount = settings.get(TextEditorConstants.SWAP_COUNT,
                                               SharedInteger.class);
        if (getCount != null && swapCount != null) {
            textEditorWindow.getCodePanel().setOperationCount(getCount.getValue(),
                                                        swapCount.getValue());
        }
    }

    private void updateHighlightLine() {
        if (settings.containsKey(TextEditorConstants.HIGHLIGHT_LINE)) {
            SharedInteger line = settings.get(TextEditorConstants.HIGHLIGHT_LINE,
                                              SharedInteger.class);
            textEditorWindow.getCodePanel().highlightLine(line.getValue());
        } else {
            textEditorWindow.getCodePanel().clearHighlight();
        }
    }

    private void updateStatus(BigInteger senderId) {
        SharedString val = settings.get(TextEditorConstants.STATUS,
                                        SharedString.class);

        boolean ourChange = senderId.equals(getCellCache().getSession().getID());

        if (val != null && val.getValue().equals(TextEditorConstants.RUN)) {
            if (!ourChange) {
                // someone else started running the code
                textEditorWindow.setControlsEnabled(false);
            }

            textEditorWindow.getCodePanel().setRunning(true);
        } else {
            if (!ourChange) {
                // someone else requested a stop -- make sure we are stopped
//                scriptManager.stop();
            }

            textEditorWindow.setControlsEnabled(true);
            textEditorWindow.getCodePanel().setRunning(false);
        }
    }

    private void updateHighlightVals() {
        SharedString val = settings.get(TextEditorConstants.HIGHLIGHT_VALUES,
                                        SharedString.class);
        if (val != null) {
            textEditorWindow.getCodePanel().setHighlightedVals(val.getValue());
        }
    }

    private class DocumentHandler
            implements DocumentListener, ComponentMessageReceiver
    {
        private static final String REMOTE_CHANGE = "remoteChange";

        private Document document;
        private long receivedVersion;
        private long appliedVersion;
        private int localChangeCount;
        private final List<TextEditorCellMessage> queue =
                new LinkedList<TextEditorCellMessage>();

        public DocumentHandler(final Document document, final long version,
                               final String initialText)
        {
            this.document = document;
            this.receivedVersion = version;
            this.appliedVersion = version;

            document.addDocumentListener(this);
        
            // populate initial text
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    try {
                        setRemoteChange(true);
                        document.insertString(0, initialText, null);
                    } catch (BadLocationException ble) {
                        logger.log(Level.WARNING, "Error setting initial text " +
                                   initialText, ble);
                    } finally {
                        setRemoteChange(false);
                    }
                }
            });
        }

        public void insertUpdate(DocumentEvent de) {
            // only send messages for changes that originated locally
            if (isRemoteChange(de)) {
                return;
            }

            try {
                String text = de.getDocument().getText(de.getOffset(), de.getLength());
                
                localChange();
                channel.send(new TextEditorCellInsertMessage(getCellID(), getAppliedVersion(),
                                                       de.getOffset(), text));

//                scriptManager.stop();
            } catch (BadLocationException ex) {
                logger.log(Level.WARNING, "Error reading inserted text at " +
                           de.getOffset());
            }
        }

        public void removeUpdate(DocumentEvent de) {
            // only send messages for changes that originated locally
            if (isRemoteChange(de)) {
                return;
            }

            localChange();
            channel.send(new TextEditorCellDeleteMessage(getCellID(), getAppliedVersion(),
                                                   de.getOffset(), de.getLength()));

//            scriptManager.stop();
        }

        public void changedUpdate(DocumentEvent de) {
            // ignore
        }

        public void messageReceived(final CellMessage message) {
            // process messages on the AWT event thread, to avoid
            // having to synchronize with the document listeners
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    // make sure the message has a valid version
                    if (!checkMessageVersion((TextEditorCellMessage) message)) {
                        return;
                    }

                    // go ahead and deliver or queue the message, depending
                    // on if we have outstanding local changes
                    deliverOrQueueMessage((TextEditorCellMessage) message);
                }
            });
        }
        
        private void handleMessage(TextEditorCellMessage message) {
            if (message instanceof TextEditorCellInsertMessage) {
                handleInsert((TextEditorCellInsertMessage) message);
            } else if (message instanceof TextEditorCellDeleteMessage) {
                handleDelete((TextEditorCellDeleteMessage) message);
            } else if (message instanceof TextEditorCellMultiChangeMessage) {
                handleMulti((TextEditorCellMultiChangeMessage) message);
            }

            // update the applied version
            setAppliedVersion(message.getVersion());
        }
        
        private void handleInsert(TextEditorCellInsertMessage insert) {
            try {
                setRemoteChange(true);
                document.insertString(insert.getInsertionPoint(), insert.getText(), null);
            } catch (BadLocationException ex) {
                logger.log(Level.WARNING, "Error inserting " + insert.getText(), ex);
            } finally {
                setRemoteChange(false);
            }
        }
        
        private void handleDelete(TextEditorCellDeleteMessage delete) {
            setRemoteChange(true);
            try {
                document.remove(delete.getDeletionPoint(), delete.getLength());
            } catch (BadLocationException ex) {
                logger.log(Level.WARNING, "Error deleting", ex);
            } finally {
                setRemoteChange(false);
            }
        }
        
        private void handleMulti(TextEditorCellMultiChangeMessage multi) {
            for (TextEditorCellMessage message : multi.getMessages()) {
                handleMessage(message);
            }
        }

        /**
         * Queue messages if we have outstanding local changes.  Remote changes
         * will be applied once the server acknowledges all of our local changes.
         */
        private void deliverOrQueueMessage(TextEditorCellMessage message) {
            if (message.getSenderID().equals(getCellCache().getSession().getID())) {
                // we have received notification that one of our own messages
                // has been processed. Update the count of local changes
                localChangeCount--;
                if (localChangeCount == 0) {
                    // if the change count is now zero, we are sync'ed up
                    // with the server, so it is a good time to process
                    // the outstanding changes
                    for (TextEditorCellMessage m : queue) {
                        handleMessage(m);
                    }
                    queue.clear();
                }

                // for a local change, we don't want to make any changes, we
                // just want to update the version number. So we substitute
                // the message with a noop that just increases the version
                // number
                message = new TextEditorCellDeleteMessage(message.getCellID(),
                                                    message.getVersion(),
                                                    0, 0);
            }

            // at this point, we have a remote message. Check if we need to
            // add it to the queue
            if (!queue.isEmpty() || getLocalChangeCount() > 0) {
                // we do want to queue the message
                queue.add(message);
                return;
            }

            // if we got here, the message should be handled immediately
            handleMessage(message);
        }

        private boolean checkMessageVersion(TextEditorCellMessage message) {
            if (message.getVersion() != (receivedVersion + 1)) {
                logger.log(Level.WARNING, "Bad version: " + message.getVersion() +
                           " current: " + receivedVersion);
                return false;
            }

            // now that we have a valid version, update our version to the
            // version from this message
            receivedVersion = message.getVersion();
            return true;
        }

        private synchronized long getAppliedVersion() {
            return appliedVersion;
        }

        private synchronized void setAppliedVersion(long version) {
            this.appliedVersion = version;
        }

        private synchronized void localChange() {
            localChangeCount++;
        }

        private synchronized int getLocalChangeCount() {
            return localChangeCount;
        }

        private void setRemoteChange(boolean remoteChange) {
            document.putProperty(REMOTE_CHANGE, Boolean.valueOf(remoteChange));
        }

        private boolean isRemoteChange(DocumentEvent de) {
            Boolean remoteChange =
                    (Boolean) de.getDocument().getProperty(REMOTE_CHANGE);
            return (remoteChange != null && remoteChange.booleanValue());
        }
    }

//    private class ParentSortable implements Sortable {
//        private SortCell parent;
//
//        int gets = 0;
//        int swaps = 0;
//
//        public ParentSortable(SortCell parent) {
//            this.parent = parent;
//        }
//
//        public int getCount() {
//            return parent.getItemCount();
//        }
//
//        public int get(int i) {
//            gets++;
//            settings.put(TextEditorConstants.GET_COUNT, SharedInteger.valueOf(gets));
//
//            return parent.get(i);
//        }
//
//        int getInternal(int i) {
//            return parent.get(i);
//        }
//
//        public void swap(int i1, int i2) {
//            swaps++;
//            settings.put(TextEditorConstants.SWAP_COUNT, SharedInteger.valueOf(swaps));
//
//            parent.requestSwap(i1, i2);
//        }
//
//        public void reset() {
//            resetCounts();
//
//            parent.requestReset(true);
//        }
//
//        void resetCounts() {
//            gets = 0;
//            swaps = 0;
//
//            settings.put(TextEditorConstants.GET_COUNT, SharedInteger.valueOf(0));
//            settings.put(TextEditorConstants.SWAP_COUNT, SharedInteger.valueOf(0));
//        }
//    }
}
