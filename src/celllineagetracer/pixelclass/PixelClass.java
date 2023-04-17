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

package celllineagetracer.pixelclass;

import java.awt.Color;

public class PixelClass {
	public int hue;
	public int sizeInner = 0;
	public int sizeOuter = 0;
	public int valueRegion = 0;
	public int valueInner = 0;
	public int valueOuter = 0;

	public PixelClass(int hue, int valueRegion) {
		this.hue = hue;
		this.valueRegion = valueRegion;
	}

	public PixelClass setInnerContour(int sizeInner, int valueInner) {
		this.sizeInner = sizeInner;
		this.valueInner = valueInner;
		return this;
	}

	public PixelClass setOuterContour(int sizeOuter, int valueOuter) {
		this.sizeOuter = sizeOuter;
		this.valueOuter = valueOuter;
		return this;
	}

	public Color getColor() {
		return Color.getHSBColor(this.hue / 360.0F, 1.0F, 1.0F);
	}
}
