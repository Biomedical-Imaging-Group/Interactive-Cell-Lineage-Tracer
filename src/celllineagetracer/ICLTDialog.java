/*
 * Interface Cell Lineage Tracer (ICLT)
 * 
 * Author: Daniel Sage and Chiara Toniolo, EPFL
 * 
 * Conditions of use: You are free to use this software for research or
 * educational purposes. In addition, we expect you to include adequate
 * citations and acknowledgments whenever you present or publish results that
 * are based on it.
 * 
 * Reference: Book chapter, 2023
 * Quantification of Mycobacterium tuberculosis growth in cell-based infection 
 * assays by time-lapse fluorescence microscopy
 * Chiara Toniolo, Daniel Sage, John D. McKinney, Neeraj Dhar
 */

/*
 * Copyright 2014-2023 Biomedical Imaging Group at the EPFL.
 * 
 * This file is part of Interface Cell Lineage Tracer (ICLT).
 * 
 * ICLT is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * ICLT is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * ICLT. If not, see <http://www.gnu.org/licenses/>.
 */

package celllineagetracer;

import java.awt.BorderLayout;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;

import additionaluserinterface.GridPanel;
import additionaluserinterface.Settings;
import celllineagetracer.canvas.DisplayControlPanel;
import celllineagetracer.cell.CellsPanel;
import celllineagetracer.outline.OutlinesPanel;
import celllineagetracer.pixelclass.PixelClassesPanel;
import celllineagetracer.polyline.DrawParameters;
import ij.IJ;
import ij.gui.GUI;
import ij.gui.StackWindow;

public class ICLTDialog extends JDialog implements ActionListener {
	private JButton bnClose = new JButton("Close");
	private JButton bnNew = new JButton("New...");
	private JButton bnOpen = new JButton("Open...");
	private JButton bnSave = new JButton("Save...");
	private JTextField txtFile;
	private JTabbedPane tab = new JTabbedPane();
	private StackWindow windowOriginal;
	private Supervisor supervisor;
	private Settings settings;

	public ICLTDialog(Supervisor supervisor, StackWindow windowOriginal, Settings settings) {
		super(new JFrame(), "Interactive Cell Lineage Tracer");

		this.supervisor = supervisor;
		this.windowOriginal = windowOriginal;
		this.settings = settings;

		String path = Supervisor.imp.getOriginalFileInfo().directory;
		String name = Supervisor.imp.getTitle();
		this.txtFile = new JTextField(path + Tools.changeExtension(name, "csv"));

		GridPanel pnFile = new GridPanel();
		pnFile.place(1, 0, 4, 1, this.txtFile);
		pnFile.place(2, 0, this.bnNew);
		pnFile.place(2, 1, this.bnOpen);
		pnFile.place(2, 2, this.bnSave);
		pnFile.place(2, 3, this.bnClose);

		OutlinesPanel panelOutlines = new OutlinesPanel(supervisor);
		panelOutlines.setBorder(BorderFactory.createEtchedBorder());
		CellsPanel panelCells = new CellsPanel(supervisor);
		panelCells.setBorder(BorderFactory.createEtchedBorder());
		JPanel pnLineage = new JPanel(new BorderLayout());
		pnLineage.add(panelOutlines, "Center");
		pnLineage.add(panelCells, "North");

		PixelClassesPanel panelPC = new PixelClassesPanel(supervisor);
		JPanel pnClass = new JPanel();
		pnClass.setLayout(new BoxLayout(pnClass, 3));
		pnClass.add(panelPC);
		pnClass.add(new Labelization().getPanel());

		JPanel pnMeasure = new JPanel();
		pnMeasure.setLayout(new BoxLayout(pnMeasure, 3));
		pnMeasure.add(new Measure().getPanel());
		pnMeasure.add(new Rendering(settings).getPanel());

		JPanel pnSettings = new JPanel();
		pnSettings.setLayout(new BoxLayout(pnSettings, 3));
		pnSettings.add(new DisplayControlPanel(supervisor, settings));
		pnSettings.add(new DrawParameters().getPanel(settings));

		this.tab.setTabLayoutPolicy(0);
		this.tab.add("<html>Lineage</html>", pnLineage);
		this.tab.add("<html>Class</html>", pnClass);
		this.tab.add("<html>Measure</html>", pnMeasure);
		this.tab.add("<html>Settings</html>", pnSettings);
		this.tab.add("<html>Help</html>", new Help());

		setLayout(new BorderLayout());
		add(this.tab, "Center");
		add(pnFile, "South");

		this.txtFile.setDropTarget(new LocalDropTarget());
		this.bnOpen.setDropTarget(new LocalDropTarget());
		panelOutlines.getScrollPane().setDropTarget(new LocalDropTarget());
		panelCells.getScrollPane().setDropTarget(new LocalDropTarget());

		settings.record("txtFile", this.txtFile, path + Tools.changeExtension(name, "csv"));

		this.bnNew.addActionListener(this);
		this.bnOpen.addActionListener(this);
		this.bnSave.addActionListener(this);
		this.bnClose.addActionListener(this);

		pack();

		GUI.center(this);
		setModal(false);
		setVisible(true);

		settings.loadRecordedItems();
	}

	public synchronized void actionPerformed(ActionEvent e) {
		if (e.getSource() == this.bnClose) {
			if (checkSave()) {
				this.settings.storeRecordedItems();
				dispose();
				if (this.windowOriginal != null) {
					Supervisor.imp.setWindow(this.windowOriginal);
				}
			}
		}
		else if (e.getSource() == this.bnNew) {
			if (checkSave()) {
				new NewDialog(this.supervisor, this.txtFile);
			}
		}
		else if (e.getSource() == this.bnOpen) {
			if (checkSave()) {
				String path = ICLTFile.browseOpen(this.txtFile.getText());
				if (path != null) {
					ICLTFile.load(path);
					this.txtFile.setText(path);
				}
			}
		}
		else if (e.getSource() == this.bnSave) {
			String path = ICLTFile.browseSave(this.txtFile.getText());
			if (path != null) {
				this.txtFile.setText(path);
				ICLTFile.save(this.txtFile.getText());
			}
		}
	}

	public boolean checkSave() {
		if (Supervisor.cells == null) {
			return true;
		}
		if (Supervisor.cells.size() == 0) {
			return true;
		}
		String message = "Save into " + this.txtFile.getText();
		String title = "Cell Lineage Tracer";
		Object[] choices = { "Save", "Don't Save", "Cancel" };
		int reply = JOptionPane.showOptionDialog(this, message, title, 0, 3, null, choices, "Save");
		if (reply == 2) {
			return false;
		}
		if (reply == 0) {
			ICLTFile.save(this.txtFile.getText());
		}
		return true;
	}

	public class LocalDropTarget extends DropTarget {
		public LocalDropTarget() {
		}

		public void drop(DropTargetDropEvent e) {
			e.acceptDrop(1);
			e.getTransferable().getTransferDataFlavors();
			Transferable transferable = e.getTransferable();
			DataFlavor[] flavors = transferable.getTransferDataFlavors();
			DataFlavor[] arrayOfDataFlavor1;
			int j = (arrayOfDataFlavor1 = flavors).length;
			for (int i = 0; i < j; i++) {
				DataFlavor flavor = arrayOfDataFlavor1[i];
				if (flavor.isFlavorJavaFileListType()) {
					try {
						List<File> files = (List) transferable.getTransferData(flavor);
						for (File file : files) {
							if (file.isFile()) {
								ICLTFile.load(file.getAbsolutePath());
								ICLTDialog.this.txtFile.setText(file.getAbsolutePath());
								ICLTDialog.this.supervisor.updateAll(null, true);
							}
						}
					}
					catch (UnsupportedFlavorException ex) {
						IJ.error("Unsupported Flavor Exception");
						ex.printStackTrace();
					}
					catch (IOException ex) {
						IJ.error("IOException " + ex.getMessage());
						ex.printStackTrace();
					}
				}
			}
			e.dropComplete(true);
			super.drop(e);
		}
	}
}
