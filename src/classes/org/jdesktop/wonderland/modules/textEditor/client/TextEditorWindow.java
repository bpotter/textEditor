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

import java.util.logging.Logger;
import javax.swing.text.Document;

import org.jdesktop.wonderland.client.hud.HUDComponent;
import org.jdesktop.wonderland.client.hud.HUDObject.DisplayMode;
import org.jdesktop.wonderland.client.jme.JmeClientMain;
import org.jdesktop.wonderland.modules.appbase.client.App2D;
import org.jdesktop.wonderland.modules.appbase.client.swing.WindowSwing;

/**
 *
 * @author Jonathan Kaplan <jonathankap@gmail.com>
 */
public class TextEditorWindow extends WindowSwing {
    private static final Logger logger =
            Logger.getLogger(TextEditorWindow.class.getName());
    
    private TextEditorCell cell;
    private TextEditorPanel editPanel;
//    private CodeControlPanel controls;
    private boolean controlsEnabled = true;
    private HUDComponent controlComponent;
    private DisplayMode displayMode;

    public TextEditorWindow(TextEditorCell cell, App2D app, int width, int height,
                            boolean topLevel, Vector2f pixelScale)
    {
        super(app, Type.PRIMARY, width, height, topLevel, pixelScale);

        setTitle("Text Editor");

        this.cell = cell;
        this.editPanel = new TextEditorPanel(this, cell.getContentType());
        
        // parent to Wonderland main window for proper focus handling
        JmeClientMain.getFrame().getCanvas3DPanel().add(editPanel);
        setComponent(editPanel);

        setDisplayMode(DisplayMode.WORLD);
//        showControls(false);
    }

    public Document getDocument() {
        return editPanel.getDocument();
    }

    public TextEditorPanel getEditPanel() {
        return editPanel;
    }

//    public void setControlsEnabled(boolean enabled) {
//        this.controlsEnabled = enabled;
//
//        if (controls != null) {
//            controls.setRunEnabled(enabled);
//        }
//    }

    /**
     * Sets the display mode for the control panel to in-world or on-HUD
     * @param displayMode the control panel display mode
     */
    public void setDisplayMode(DisplayMode displayMode) {
        this.displayMode = displayMode;
    }

    /**
     * Gets the control panel display mode
     * @return the display mode of the control panel: in-world or on HUD
     */
    public DisplayMode getDisplayMode() {
        return displayMode;
    }

    /**
     * Shows or hides the HUD controls.
     * The controls are shown in-world or on-HUD depending on the selected
     * DisplayMode.
     *
     * @param visible true to show the controls, hide to hide them
     */
//    public void showControls(final boolean visible) {
//        EventQueue.invokeLater(new Runnable() {
//
//            public void run() {
//                logger.warning("show controls: " + visible);
//                HUD mainHUD = HUDManagerFactory.getHUDManager().getHUD("main");
//
//                try {
//                    if (controlComponent == null) {
//                        // create control panel
//                        controls = new CodeControlPanel(cell, TextEditorWindow.this);
//                        controls.setRunEnabled(controlsEnabled);
//
//                        // create HUD control panel
//                        controlComponent = mainHUD.createComponent(controls, cell);
//                        controlComponent.setPreferredLocation(Layout.SOUTH);
//
//                        // add HUD control panel to HUD
//                        mainHUD.addComponent(controlComponent);
//                    }
//                } catch (Throwable t) {
//                    logger.log(Level.WARNING, "Error creating component", t);
//                    if (t instanceof RuntimeException) {
//                        throw (RuntimeException) t;
//                    }
//                }
//
//                SwingUtilities.invokeLater(new Runnable() {
//
//                    public void run() {
//                        logger.warning("Change visibility: " + getDisplayMode());
//
//                        // change visibility of controls
//                        if (getDisplayMode() == DisplayMode.HUD) {
//                            if (controlComponent.isWorldVisible()) {
//                                controlComponent.setWorldVisible(false);
//                            }
//
//                            logger.warning("Set component visible: " + visible);
//                            controlComponent.setVisible(visible);
//                        } else {
//                            controlComponent.setWorldLocation(new Vector3f(0.0f, -3.2f, 0.1f));
//                            if (controlComponent.isVisible()) {
//                                controlComponent.setVisible(false);
//                            }
//
//                            logger.warning("Set component world visible: " + visible);
//                            controlComponent.setWorldVisible(visible); // show world view
//                        }
//
//                        updateControls();
//                    }
//                });
//            }
//        });
//    }
//
//    public boolean showingControls() {
//        return ((controlComponent != null) && (controlComponent.isVisible() || controlComponent.isWorldVisible()));
//    }

//    protected void updateControls() {
//        if (controls != null) {
//            boolean onHUD = (getDisplayMode() == DisplayMode.HUD);
//            controls.setOnHUD(onHUD);
//        }
//    }
}
