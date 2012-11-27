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
import org.jdesktop.wonderland.client.cell.CellCache;
import org.jdesktop.wonderland.client.cell.ChannelComponent.ComponentMessageReceiver;
import org.jdesktop.wonderland.client.cell.annotation.UsesCellComponent;
import org.jdesktop.wonderland.client.contextmenu.ContextMenuActionListener;
import org.jdesktop.wonderland.client.contextmenu.ContextMenuItem;
import org.jdesktop.wonderland.client.contextmenu.ContextMenuItemEvent;
import org.jdesktop.wonderland.client.contextmenu.SimpleContextMenuItem;
import org.jdesktop.wonderland.client.contextmenu.cell.ContextMenuComponent;
import org.jdesktop.wonderland.client.contextmenu.spi.ContextMenuFactorySPI;
import org.jdesktop.wonderland.client.scenemanager.event.ContextEvent;
import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.common.cell.CellStatus;
import org.jdesktop.wonderland.common.cell.messages.CellMessage;
import org.jdesktop.wonderland.common.cell.state.CellClientState;
import org.jdesktop.wonderland.modules.appbase.client.App2D;
import org.jdesktop.wonderland.modules.appbase.client.cell.App2DCell;
import org.jdesktop.wonderland.modules.sharedstate.client.SharedMapCli;
import org.jdesktop.wonderland.modules.sharedstate.client.SharedMapEventCli;
import org.jdesktop.wonderland.modules.sharedstate.client.SharedMapListenerCli;
import org.jdesktop.wonderland.modules.sharedstate.client.SharedStateComponent;
import org.jdesktop.wonderland.modules.sharedstate.common.SharedBoolean;
import org.jdesktop.wonderland.modules.sharedstate.common.SharedInteger;
import org.jdesktop.wonderland.modules.sharedstate.common.SharedString;
import org.jdesktop.wonderland.modules.textEditor.common.*;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;

/**
 * @author Jonathan Kaplan <jonathankap@gmail.com>
 */
public class TextEditorCell extends App2DCell
        implements SharedMapListenerCli {
    @UsesCellComponent
    private SharedStateComponent state;

    @UsesCellComponent
    ContextMenuComponent menuComponent;

    private final StringBuffer codeTemplate;

    private TextEditorApp textEditorApp;
    private TextEditorWindow textEditorWindow;
    private TextEditorCellClientState clientState;
    DocumentHandler handler;
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

    public void requestSync() {
        handler.syncDocument();
    }

    public String getCurrentText() {
        Document document = ((TextEditorWindow) getApp().getPrimaryWindow()).getDocument();
        try {
            return document.getText(0, document.getLength() - 1);
        } catch (BadLocationException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        return "";
    }

    public SharedStateComponent getState() {
        return state;
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

            menuComponent.addContextMenuFactory(new TextEditorContextMenuFactory());
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
            handler = new DocumentHandler(textEditorWindow.getDocument(),
                    clientState.getVersion(),
                    clientState.getText());
            textEditorWindow.getEditPanel().setFileLabel(clientState.getFileName());
            channel.addMessageReceiver(TextEditorCellInsertMessage.class, handler);
            channel.addMessageReceiver(TextEditorCellDeleteMessage.class, handler);
            channel.addMessageReceiver(TextEditorCellSyncMessage.class, handler);
            channel.addMessageReceiver(TextEditorNewContentMessage.class, handler);
            channel.addMessageReceiver(TextEditorCellMultiChangeMessage.class, handler);

            // both the app and the user want this window to be visible
            textEditorWindow.setVisibleApp(true);
            textEditorWindow.setVisibleUser(this, true);

            syncState();

        } else if (status == CellStatus.DISK) {
            channel.removeMessageReceiver(TextEditorCellInsertMessage.class);
            channel.removeMessageReceiver(TextEditorCellDeleteMessage.class);
            channel.removeMessageReceiver(TextEditorCellSyncMessage.class);
            channel.removeMessageReceiver(TextEditorNewContentMessage.class);
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

    protected TextEditorCell getCell() {

        return this;
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
                        smec.getPropertyName().equals(TextEditorConstants.SWAP_COUNT)) {
                    updateOperationCounts();
                } else if (smec.getPropertyName().equals(TextEditorConstants.STATUS)) {
//                    updateStatus(smec.getSenderID());
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
//                updateStatus(BigInteger.ZERO);
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
//            textEditorWindow.getEditPanel().setOperationCount(getCount.getValue(),
//                                                        swapCount.getValue());
        }
    }

    private void updateHighlightLine() {
        if (settings.containsKey(TextEditorConstants.HIGHLIGHT_LINE)) {
            SharedInteger line = settings.get(TextEditorConstants.HIGHLIGHT_LINE,
                    SharedInteger.class);
            textEditorWindow.getEditPanel().highlightLine(line.getValue());
        } else {
            textEditorWindow.getEditPanel().clearHighlight();
        }
    }

//    private void updateStatus(BigInteger senderId) {
//        SharedString val = settings.get(TextEditorConstants.STATUS,
//                SharedString.class);
//
//        boolean ourChange = senderId.equals(getCellCache().getSession().getID());
//
//        if (val != null && val.getValue().equals(TextEditorConstants.RUN)) {
//            if (!ourChange) {
//                // someone else started running the code
//                textEditorWindow.setControlsEnabled(false);
//            }
//
////            textEditorWindow.getEditPanel().setRunning(true);
//        } else {
//            if (!ourChange) {
//                // someone else requested a stop -- make sure we are stopped
////                scriptManager.stop();
//            }
//
//            textEditorWindow.setControlsEnabled(true);
////            textEditorWindow.getEditPanel().setRunning(false);
//        }
//    }

    private void updateHighlightVals() {
        SharedString val = settings.get(TextEditorConstants.HIGHLIGHT_VALUES,
                SharedString.class);
        if (val != null) {
//            textEditorWindow.getEditPanel().setHighlightedVals(val.getValue());
        }
    }

    public String getContentType() {
        return clientState.getContentType();
    }

    private class DocumentHandler
            implements DocumentListener, ComponentMessageReceiver {
        private static final String REMOTE_CHANGE = "remoteChange";

        private Document document;
        private long receivedVersion;
        private long appliedVersion;
        private int localChangeCount;
        private final List<TextEditorCellMessage> queue =
                Collections.synchronizedList(new ArrayList<TextEditorCellMessage>());

        public DocumentHandler(final Document document, final long version,
                               final String initialText) {
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


        public void syncDocument() {
            channel.send(new TextEditorCellSyncMessage(getCellID(), 0));
        }

        public void insertUpdate(DocumentEvent de) {
            // only send messages for changes that originated locally
            if (isRemoteChange(de)) {
                return;
            }

            try {
                String text = de.getDocument().getText(de.getOffset(), de.getLength());

                localChange();
                TextEditorCellInsertMessage message = new TextEditorCellInsertMessage(getCellID(), getAppliedVersion(),
                        de.getOffset(), text);
                queue.add(message);
                channel.send(message);

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

            TextEditorCellDeleteMessage message = new TextEditorCellDeleteMessage(getCellID(), getAppliedVersion(),
                    de.getOffset(), de.getLength());
            queue.add(message);
            channel.send(message);

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
                    if (!(message instanceof TextEditorCellSyncMessage)) {
                        if (!checkMessageVersion((TextEditorCellMessage) message)) {
                            return;
                        }
                    } else {
                        // sync messages should be processed by all clients
                        message.setSenderID(BigInteger.valueOf(0));
                        receivedVersion = ((TextEditorCellSyncMessage) message).getVersion();
                    }
                    // go ahead and deliver or queue the message, depending
                    // on if we have outstanding local changes
                    deliverOrQueueMessage((TextEditorCellMessage) message);
                }
            });
        }

        private synchronized void handleMessage(TextEditorCellMessage message) {
            logger.info("message received " + message);
            if (message instanceof TextEditorCellInsertMessage) {
                handleInsert((TextEditorCellInsertMessage) message);
            } else if (message instanceof TextEditorCellDeleteMessage) {
                handleDelete((TextEditorCellDeleteMessage) message);
            } else if (message instanceof TextEditorCellMultiChangeMessage) {
                handleMulti((TextEditorCellMultiChangeMessage) message);
            } else if (message instanceof TextEditorCellSyncMessage) {
                handleSync((TextEditorCellSyncMessage) message);
            }

            // update the applied version
            setAppliedVersion(message.getVersion());
        }


        private void handleSync(TextEditorCellSyncMessage sync) {
            try {
                document.remove(0, document.getLength());
                document.insertString(0, sync.getText(), null);
            } catch (BadLocationException e) {
                logger.severe("Could not sync to version " + sync.getVersion());
            }
        }


        private void handleInsert(TextEditorCellInsertMessage insert) {
            try {
                setRemoteChange(true);
                logger.info("insert at " + insert.getOffset() + " '" + insert.getText() + "'");
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
        private synchronized void deliverOrQueueMessage(TextEditorCellMessage message) {
            if (message.getSenderID().equals(getCellCache().getSession().getID())) {
                // we have received notification that one of our own messages
                // has been processed. Update the count of local changes
                if (localChangeCount > 0) {
                    localChangeCount--;
                    queue.remove(0);
                }
                if (localChangeCount == 0) {

//                    // if the change count is now zero, we are sync'ed up
//                    // with the server, so it is a good time to process
//                    // the outstanding changes
//                    for (TextEditorCellMessage m : queue) {
//                        handleMessage(m);
//                    }
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
                // we need to fix the offset to match the local version
                int offset = message.getOffset();
                logger.info("message type: " + message.getClass().getSimpleName());
                logger.info("initial offset " + offset);
                for (TextEditorCellMessage cellMessage : queue) {
                    logger.info("cell message " + cellMessage.getClass().getSimpleName());
                    logger.info("offset " + cellMessage.getOffset());

                    if (cellMessage.getOffset() < offset) {
                        if (cellMessage instanceof TextEditorCellDeleteMessage) {
                            int length = ((TextEditorCellDeleteMessage) cellMessage).getLength();
                            logger.info("length: " + length);
                            offset -= length;
                        } else if (cellMessage instanceof TextEditorCellInsertMessage) {
                            int length = ((TextEditorCellInsertMessage) cellMessage).getText().getBytes().length;
                            logger.info("length: " + length);
                            offset += length;
                        }
                    }
                }
                logger.info("resulting offset " + offset);
                message.setOffset(offset);


                // we do want to queue the message
                //queue.add(message);

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

    class TextEditorContextMenuFactory implements ContextMenuFactorySPI {
        public ContextMenuItem[] getContextMenuItems(ContextEvent contextEvent) {
//            return new ContextMenuItem[]{new SimpleContextMenuItem("Import File", null, new TextEditorImportFileListener()),
//                    new SimpleContextMenuItem("Export File", null, new TextEditorExportFileListener())
            return new ContextMenuItem[]{new SimpleContextMenuItem("Export File", null, new TextEditorExportFileListener())
            };
        }
    }

    class TextEditorImportFileListener implements ContextMenuActionListener {

        public void actionPerformed(ContextMenuItemEvent event) {
            importFile();

        }
    }

    public void importFile() {
        JFileChooser jChooser = new JFileChooser();
        jChooser.addChoosableFileFilter(null);
        int response = jChooser.showOpenDialog(null);
        if (response == JFileChooser.APPROVE_OPTION) {
            TextEditorImportExportHelper helper = new TextEditorImportExportHelper(getCell());
            TextEditorNewContentMessage message = new TextEditorNewContentMessage(this.getCellID(), 1);
            message.setText(TextEditorImportExportHelper.importFile(jChooser.getSelectedFile()));
            channel.send(message);


        }
    }

    class TextEditorExportFileListener implements ContextMenuActionListener {

        public void actionPerformed(ContextMenuItemEvent event) {
            exportFile();

        }
    }

    public void exportFile() {
        JFileChooser jChooser = new JFileChooser();
//            jChooser.setFileFilter(new FileNameExtensionFilter(BUNDLE.getString("Csv_file"), "cardwall.csv"));

        jChooser.addChoosableFileFilter(null);

        int response = jChooser.showSaveDialog(null);
        if (response == JFileChooser.APPROVE_OPTION) {
            TextEditorImportExportHelper helper = new TextEditorImportExportHelper(getCell());
            helper.exportFile(jChooser.getSelectedFile());

        }
    }
}


