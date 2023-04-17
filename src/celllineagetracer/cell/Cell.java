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

package celllineagetracer.cell;

import java.awt.Color;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;

import celllineagetracer.outline.Outline;
import celllineagetracer.outline.Outlines;

public class Cell {
	private int hue;
	private String defaultClass;
	private boolean fix;
	private boolean singleton;
	private Outlines outlines;
	private ArrayList<TrajectoryPoint> trajectory;

	public Cell(String defaultClass, int hue, boolean fix, boolean singleton) {
		this.defaultClass = defaultClass;
		this.hue = hue;
		this.fix = fix;
		this.singleton = singleton;
		this.outlines = new Outlines();
	}

	public void addOutline(int frame, Outline outline) {
		if (this.outlines != null) {
			this.outlines.put(new Integer(frame), outline);
		}
		computeTrajectory();
	}

	public void removeOutline(int frame) {
		if (this.outlines != null) {
			this.outlines.remove(new Integer(frame));
		}
		computeTrajectory();
	}

	public void removeAllOutlines() {
		if (this.outlines != null) {
			this.outlines.clear();
		}
		computeTrajectory();
	}

	public int getCountOutline() {
		if (this.outlines != null) {
			return this.outlines.size();
		}
		return 0;
	}

	public Set<Integer> getListOutlinesFrame() {
		if (this.outlines != null) {
			return this.outlines.keySet();
		}
		return new TreeSet<Integer>();
	}

	public Outline getOutline(Integer frame) {
		if (this.outlines != null) {
			return (Outline) this.outlines.get(frame);
		}
		return null;
	}

	public ArrayList<TrajectoryPoint> getTrajectory() {
		return this.trajectory;
	}

	public void computeTrajectory() {
		ArrayList<Integer> sorted = new ArrayList<Integer>(this.outlines.keySet());
		Collections.sort(sorted);
		this.trajectory = new ArrayList<TrajectoryPoint>();
		for (Integer i : sorted) {
			Outline outline = (Outline) this.outlines.get(i);
			Point2D.Double cog = outline.getPolyline().computeCoG();
			this.trajectory.add(new TrajectoryPoint(cog.x, cog.y, outline.getFrame()));
		}
	}

	public String getInformation() {
		if (this.outlines == null) {
			return "Error";
		}
		return this.outlines.size() + " outlines";
	}

	public String getDefaultClass() {
		return this.defaultClass;
	}

	public Color getColor() {
		return Color.getHSBColor(this.hue / 360.0F, 1.0F, 1.0F);
	}

	public void setHue(int hue) {
		this.hue = hue;
	}

	public void setDefaultClass(String defaultClass) {
		this.defaultClass = defaultClass;
	}

	public int getHue() {
		return this.hue;
	}

	public boolean isMobile() {
		return !this.fix;
	}

	public boolean isMultiplePart() {
		return !this.singleton;
	}
}
