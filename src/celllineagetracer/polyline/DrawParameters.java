/*
 * Interactive Cell Lineage Tracer (ICLT)
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
 * This file is part of Interactive Cell Lineage Tracer (ICLT).
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

package celllineagetracer.polyline;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import additionaluserinterface.GridPanel;
import additionaluserinterface.Settings;
import additionaluserinterface.SpinnerDouble;
import additionaluserinterface.SpinnerInteger;

public class DrawParameters
  implements ChangeListener, ActionListener
{
  private SpinnerInteger spnMagAperture = new SpinnerInteger(1, 1, 100, 1);
  private SpinnerDouble spnMagDataTerm = new SpinnerDouble(1.0D, -100000.0D, 100000.0D, 1.0D);
  private SpinnerDouble spnMagRegTerm = new SpinnerDouble(1.0D, -100000.0D, 100000.0D, 1.0D);
  private JComboBox<String> cmbMagCost = new JComboBox<String>(new String[] { "Value", "Gradient" });
  private SpinnerInteger spnOptAperture = new SpinnerInteger(1, 1, 100, 1);
  private SpinnerDouble spnOptDataTerm = new SpinnerDouble(1.0D, -100000.0D, 100000.0D, 1.0D);
  private SpinnerDouble spnOptRegTerm = new SpinnerDouble(1.0D, -100000.0D, 100000.0D, 1.0D);
  private JComboBox<String> cmbOptCost = new JComboBox<String>(new String[] { "Value", "Gradient" });
  private SpinnerDouble spnTolerance = new SpinnerDouble(4.7D, 0.1D, 100.0D, 0.1D);
  private SpinnerInteger spnSmooth = new SpinnerInteger(2, 0, 1000, 1);
  public static int magAperture = 1;
  public static double magDataTerm = 1.0D;
  public static double magRegTerm = 1.0D;
  public static int magCost = 0;
  public static int optAperture = 1;
  public static double optDataTerm = 1.0D;
  public static double optRegTerm = 1.0D;
  public static int optCost = 0;
  public static double tolerance = 4.7D;
  public static int smooth = 2;
  
  public JPanel getPanel(Settings settings)
  {
    GridPanel pn = new GridPanel("Polygon Simplication", 1);
    pn.place(1, 0, new JLabel("Tolerance"));
    pn.place(1, 1, this.spnTolerance);
    pn.place(1, 2, new JLabel("Smooth"));
    pn.place(1, 3, this.spnSmooth);
    
    GridPanel pnOpt = new GridPanel("Livewire - Shortest Path (DP)", 1);
    pnOpt.place(1, 0, new JLabel("Cost"));
    pnOpt.place(1, 1, this.cmbOptCost);
    pnOpt.place(1, 2, new JLabel("Aperture"));
    pnOpt.place(1, 3, this.spnOptAperture);
    pnOpt.place(2, 0, new JLabel("<html>&lambda; data</html>"));
    pnOpt.place(2, 1, this.spnOptDataTerm);
    pnOpt.place(2, 2, new JLabel("<html>&lambda; reg.</html>"));
    pnOpt.place(2, 3, this.spnOptRegTerm);
    
    GridPanel pnMag = new GridPanel("Magnetic - Local Attraction", 1);
    pnMag.place(1, 0, new JLabel("Cost"));
    pnMag.place(1, 1, this.cmbMagCost);
    pnMag.place(1, 2, new JLabel("Aperture"));
    pnMag.place(1, 3, this.spnMagAperture);
    pnMag.place(2, 0, new JLabel("<html>&lambda; data</html>"));
    pnMag.place(2, 1, this.spnMagDataTerm);
    pnMag.place(2, 2, new JLabel("<html>&lambda; reg.</html>"));
    pnMag.place(2, 3, this.spnMagRegTerm);
    
    settings.record("spnTolerance", this.spnTolerance, "4.7");
    settings.record("spnSmooth", this.spnSmooth, "2");
    
    settings.record("cmbOptCost", this.cmbOptCost, (String)this.cmbOptCost.getItemAt(0));
    settings.record("spnOptAperture", this.spnOptAperture, "1");
    settings.record("spnOptDataTerm", this.spnOptDataTerm, "1");
    settings.record("spnOptRegTerm", this.spnOptRegTerm, "1");
    
    settings.record("cmbMagCost", this.cmbMagCost, (String)this.cmbMagCost.getItemAt(0));
    settings.record("spnMagAperture", this.spnMagAperture, "1");
    settings.record("spnMagDataTerm", this.spnMagDataTerm, "1");
    settings.record("spnMagRegTerm", this.spnMagRegTerm, "1");
    
    JPanel panel = new JPanel();
    panel.setLayout(new BoxLayout(panel, 3));
    
    panel.add(pn);
    panel.add(pnMag);
    panel.add(pnOpt);
    
    update();
    this.spnTolerance.addChangeListener(this);
    this.spnSmooth.addChangeListener(this);
    
    this.cmbOptCost.addActionListener(this);
    this.spnOptAperture.addChangeListener(this);
    this.spnOptDataTerm.addChangeListener(this);
    this.spnOptRegTerm.addChangeListener(this);
    
    this.cmbMagCost.addActionListener(this);
    this.spnMagAperture.addChangeListener(this);
    this.spnMagDataTerm.addChangeListener(this);
    this.spnMagRegTerm.addChangeListener(this);
    return panel;
  }
  
  public void actionPerformed(ActionEvent e)
  {
    update();
  }
  
  public void stateChanged(ChangeEvent e)
  {
    update();
  }
  
  private void update()
  {
    tolerance = this.spnTolerance.get();
    smooth = this.spnSmooth.get();
    optCost = this.cmbOptCost.getSelectedIndex();
    optAperture = this.spnOptAperture.get();
    optDataTerm = this.spnOptDataTerm.get();
    optRegTerm = this.spnOptRegTerm.get();
    magCost = this.cmbMagCost.getSelectedIndex();
    magAperture = this.spnMagAperture.get();
    magDataTerm = this.spnMagDataTerm.get();
    magRegTerm = this.spnMagRegTerm.get();
  }
}
