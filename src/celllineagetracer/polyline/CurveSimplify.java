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

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.BitSet;

public class CurveSimplify {
	private Point2D.Double[] sampleArray;

	public CurveSimplify(Point2D.Double[] sampleArray) {
		this.sampleArray = sampleArray;
	}

	public Polyline simplify(Point2D.Double[] points, double tolerance, boolean highestQuality) {
		if (points == null) {
			return new Polyline();
		}
		if (points.length < 2) {
			return new Polyline();
		}
		if (points.length == 2) {
			Polyline a = new Polyline();
			a.add(new Node(points[0].x, points[0].y));
			a.add(new Node(points[1].x, points[1].y));
			return a;
		}
		double sqTolerance = tolerance * tolerance;
		if (!highestQuality) {
			points = simplifyRadialDistance(points, sqTolerance);
		}
		return simplifyDouglasPeucker(points, sqTolerance);
	}

	private Point2D.Double[] simplifyRadialDistance(Point2D.Double[] points, double sqTolerance) {
		Point2D.Double point = null;
		Point2D.Double prevPoint = points[0];

		ArrayList<Point2D.Double> newPoints = new ArrayList<Point2D.Double>();
		newPoints.add(prevPoint);
		for (int i = 1; i < points.length; i++) {
			point = points[i];
			if (getSquareDistance(point, prevPoint) > sqTolerance) {
				newPoints.add(point);
				prevPoint = point;
			}
		}
		if (prevPoint != point) {
			newPoints.add(point);
		}
		return (Point2D.Double[]) newPoints.toArray(this.sampleArray);
	}

	private static class Range {
		int first;
		int last;

		private Range(int first, int last) {
			this.first = first;
			this.last = last;
		}
	}

	private Polyline simplifyDouglasPeucker(Point2D.Double[] points, double sqTolerance) {
		BitSet bitSet = new BitSet(points.length);
		bitSet.set(0);
		bitSet.set(points.length - 1);

		ArrayList<Range> stack = new ArrayList<Range>();
		stack.add(new Range(0, points.length - 1));
		while (!stack.isEmpty()) {
			Range range = (Range) stack.remove(stack.size() - 1);

			int index = -1;
			double maxSqDist = 0.0D;
			for (int i = range.first + 1; i < range.last; i++) {
				double sqDist = getSquareSegmentDistance(points[i], points[range.first], points[range.last]);
				if (sqDist > maxSqDist) {
					index = i;
					maxSqDist = sqDist;
				}
			}
			if (maxSqDist > sqTolerance) {
				bitSet.set(index);
				stack.add(new Range(range.first, index));
				stack.add(new Range(index, range.last));
			}
		}
		Polyline newPoints = new Polyline();
		for (int index = bitSet.nextSetBit(0); index >= 0; index = bitSet.nextSetBit(index + 1)) {
			newPoints.add(new Node(points[index].x, points[index].y));
		}
		return newPoints;
	}

	public double getSquareDistance(Point2D.Double p1, Point2D.Double p2) {
		double dx = p1.x - p2.x;
		double dy = p1.y - p2.y;
		return dx * dx + dy * dy;
	}

	public double getSquareSegmentDistance(Point2D.Double p0, Point2D.Double p1, Point2D.Double p2) {
		double x1 = p1.getX();
		double y1 = p1.getY();
		double x2 = p2.getX();
		double y2 = p2.getY();
		double x0 = p0.getX();
		double y0 = p0.getY();

		double dx = x2 - x1;
		double dy = y2 - y1;
		if ((dx != 0.0D) || (dy != 0.0D)) {
			double t = ((x0 - x1) * dx + (y0 - y1) * dy) / (dx * dx + dy * dy);
			if (t > 1.0D) {
				x1 = x2;
				y1 = y2;
			}
			else if (t > 0.0D) {
				x1 += dx * t;
				y1 += dy * t;
			}
		}
		dx = x0 - x1;
		dy = y0 - y1;

		return dx * dx + dy * dy;
	}
}
