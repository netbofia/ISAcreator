/**
 ISAcreator is a component of the ISA software suite (http://www.isa-tools.org)

 License:
 ISAcreator is licensed under the Common Public Attribution License version 1.0 (CPAL)

 EXHIBIT A. CPAL version 1.0
 �The contents of this file are subject to the CPAL version 1.0 (the �License�);
 you may not use this file except in compliance with the License. You may obtain a
 copy of the License at http://isa-tools.org/licenses/ISAcreator-license.html.
 The License is based on the Mozilla Public License version 1.1 but Sections
 14 and 15 have been added to cover use of software over a computer network and
 provide for limited attribution for the Original Developer. In addition, Exhibit
 A has been modified to be consistent with Exhibit B.

 Software distributed under the License is distributed on an �AS IS� basis,
 WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License for
 the specific language governing rights and limitations under the License.

 The Original Code is ISAcreator.
 The Original Developer is the Initial Developer. The Initial Developer of the
 Original Code is the ISA Team (Eamonn Maguire, eamonnmag@gmail.com;
 Philippe Rocca-Serra, proccaserra@gmail.com; Susanna-Assunta Sansone, sa.sanson@gmail.com;
 http://www.isa-tools.org). All portions of the code written by the ISA Team are
 Copyright (c) 2007-2011 ISA Team. All Rights Reserved.

 EXHIBIT B. Attribution Information
 Attribution Copyright Notice: Copyright (c) 2008-2011 ISA Team
 Attribution Phrase: Developed by the ISA Team
 Attribution URL: http://www.isa-tools.org
 Graphic Image provided in the Covered Code as file: http://isa-tools.org/licenses/icons/poweredByISAtools.png
 Display of Attribution Information is required in Larger Works which are defined in the CPAL as a work which combines Covered Code or portions thereof with code not governed by the terms of the CPAL.

 Sponsors:
 The ISA Team and the ISA software suite have been funded by the EU Carcinogenomics project (http://www.carcinogenomics.eu), the UK BBSRC (http://www.bbsrc.ac.uk), the UK NERC-NEBC (http://nebc.nerc.ac.uk) and in part by the EU NuGO consortium (http://www.nugo.org/everyone).
 */

package org.isatools.isacreator.gui.menu;

import org.apache.log4j.Logger;
import org.isatools.errorreporter.ui.ErrorReporterView;
import org.isatools.isacreator.gui.ISAcreator;
import org.isatools.isacreator.io.importisa.ISAtabImporter;
import org.jdesktop.fuse.InjectedResource;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;

/**
 * ImportFilesMenu provides the interface to allow users to import previously saved ISATAB
 * submissions into the software for editing/viewing.
 *
 * @author Eamonn Maguire
 * @date Mar 3, 2010
 */


public class ImportFilesMenu extends AbstractImportFilesMenu {

    private static Logger log = Logger.getLogger(ImportFilesMenu.class.getName());

    @InjectedResource
    private ImageIcon panelHeader, listImage, searchButton, searchButtonOver,
            loadButton, loadButtonOver, backButton, backButtonOver, filterLeft, filterRight;

    private JLabel back;


    public ImportFilesMenu(ISAcreatorMenu menu) {
        super(menu);
        setPreferredSize(new Dimension(400, 400));
    }

    public JPanel createAlternativeExitDisplay() {

        JPanel previousButtonPanel = new JPanel(new GridLayout(1, 1));
        previousButtonPanel.setOpaque(false);

        back = new JLabel(backButton, JLabel.LEFT);
        back.addMouseListener(new MouseAdapter() {

            public void mousePressed(MouseEvent event) {
                problemScroll.setVisible(false);
                menu.getMain().setGlassPanelContents(menu.getMainMenuGUI());
            }

            public void mouseEntered(MouseEvent event) {
                back.setIcon(backButtonOver);
            }

            public void mouseExited(MouseEvent event) {
                back.setIcon(backButton);
            }
        });

        back.setOpaque(false);

        previousButtonPanel.add(back);

        return previousButtonPanel;
    }

    public ImageIcon getSearchButton() {
        return searchButton;
    }

    public ImageIcon getSearchButtonOver() {
        return searchButtonOver;
    }

    public ImageIcon getLoadButton() {
        return loadButton;
    }

    public ImageIcon getLoadButtonOver() {
        return loadButtonOver;
    }

    public void getSelectedFileAndLoad() {
        if (previousFileList.getSelectedIndex() != -1) {
            // select file from list
            for (File candidate : previousFiles) {
                if (candidate.getName()
                        .equals(previousFileList.getSelectedValue()
                                .toString())) {
                    menu.showProgressPanel(loadISAanimation);
                    loadFile(ISAcreator.DEFAULT_ISATAB_SAVE_DIRECTORY + File.separator +
                            candidate.getName() + File.separator);
                }
            }
        }
    }

    public void loadFile(final String dir) {


        // show infinite panel. if successful, hide glass panel and show either the errors, or the data entry panel containing the submission
        Thread performer = new Thread(new Runnable() {
            public void run() {
                try {

                    final ISAtabImporter iISA = new ISAtabImporter(menu.getMain());
                    boolean successfulImport = iISA.importFile(dir);
                    if (successfulImport && iISA.getMessages().size() == 0) {
                        // success, so load

                        menu.stopProgressIndicator();
                        menu.resetViewAfterProgress();
                        menu.hideGlassPane();
                        menu.getMain().setCurrentPage(menu.getMain().getDataEntryEnvironment());

                    } else if (successfulImport) {
                        log.error("The following problems were encountered when importing the ISAtab files in " + dir);


                        SwingUtilities.invokeLater(new Runnable() {
                            public void run() {
                                ErrorReporterView view = new ErrorReporterView(iISA.getMessages());
                                view.createGUI();

                                ErrorReportWrapper errorReportWithControls = new ErrorReportWrapper(view, true);
                                errorReportWithControls.createGUI();
                                errorReportWithControls.setPreferredSize(new Dimension(400, 400));

                                errorReportWithControls.addPropertyChangeListener(ErrorReportWrapper.BACK_BUTTON_CLICKED_EVENT, new PropertyChangeListener() {
                                    public void propertyChange(PropertyChangeEvent propertyChangeEvent) {
                                        System.out.println("going back");
                                        menu.changeView(ImportFilesMenu.this);
                                        revalidate();
                                    }
                                });

                                errorReportWithControls.addPropertyChangeListener(ErrorReportWrapper.CONTINUE_BUTTON_CLICKED_EVENT, new PropertyChangeListener() {
                                    public void propertyChange(PropertyChangeEvent propertyChangeEvent) {
                                        System.out.println("continuing to load");
                                        menu.hideGlassPane();
                                        menu.getMain().setCurrentPage(menu.getMain().getDataEntryEnvironment());
                                    }
                                });

                                menu.stopProgressIndicator();
                                menu.resetViewAfterProgress();
                                menu.changeView(errorReportWithControls);

                                revalidate();
                            }
                        });

                    } else {
                        log.error("The following problems were encountered when importing the ISAtab files in " + dir);


                        SwingUtilities.invokeLater(new Runnable() {
                            public void run() {
                                ErrorReporterView view = new ErrorReporterView(iISA.getMessages());
                                view.createGUI();

                                ErrorReportWrapper errorReportWithControls = new ErrorReportWrapper(view, false);
                                errorReportWithControls.createGUI();
                                errorReportWithControls.setPreferredSize(new Dimension(400, 400));

                                errorReportWithControls.addPropertyChangeListener(ErrorReportWrapper.BACK_BUTTON_CLICKED_EVENT, new PropertyChangeListener() {
                                    public void propertyChange(PropertyChangeEvent propertyChangeEvent) {
                                        System.out.println("going back");
                                        menu.changeView(ImportFilesMenu.this);
                                        revalidate();
                                    }
                                });

                                menu.stopProgressIndicator();
                                menu.resetViewAfterProgress();
                                menu.changeView(errorReportWithControls);

                                revalidate();

                            }
                        });
                    }
                } catch (OutOfMemoryError outOfMemory) {
                    System.gc();
                    menu.stopProgressIndicator();
                    menu.resetViewAfterProgress();

                    problemReport.setText("ISAcreator ran out of memory whilst loading. We have cleared memory now and you can try again. " +
                            "Alternatively, if this fails you may want to increase the memory available to ISAcreator...");
                    problemScroll.setVisible(true);
                    revalidate();
                } catch (Exception e) {
                    menu.stopProgressIndicator();
                    menu.resetViewAfterProgress();
                    e.printStackTrace();
                    problemReport.setText("Unexpected problem occurred." + e.getMessage());
                    problemScroll.setVisible(true);
                    revalidate();
                }
            }
        });
        performer.start();
    }

    public File[] getPreviousFiles() {

        previousFileList.clearItems();


        File f = new File(ISAcreator.DEFAULT_ISATAB_SAVE_DIRECTORY);

        if (!f.exists() || !f.isDirectory()) {
            f.mkdir();
        }

        previousFiles = f.listFiles();

        for (File prevSubmission : previousFiles) {
            if (prevSubmission.isDirectory()) {
                previousFileList.addItem(prevSubmission.getName());
            }
        }

        return previousFiles;
    }

    public String getBorderTitle() {
        return "select ISA-TAB for import";
    }

    public ImageIcon getPanelHeaderImage() {
        return panelHeader;
    }


    public void setListRenderer() {
        previousFileList.setCellRenderer(new ImportFilesListCellRenderer(listImage));
    }

    @Override
    public ImageIcon getLeftFilterImage() {
        return filterLeft;
    }

    @Override
    public ImageIcon getRightFilterImage() {
        return filterRight;
    }
}