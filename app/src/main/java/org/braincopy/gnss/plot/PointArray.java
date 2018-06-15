/*
 
Copyright (c) 2013-2014 Hiroaki Tateshita

Permission is hereby granted, free of charge, to any person obtaining a copy 
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell 
copies of the Software, and to permit persons to whom the Software is furnished
to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all 
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR 
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, 
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL 
THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER 
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, 
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS 
IN THE SOFTWARE.

 */

package org.braincopy.gnss.plot;

import android.util.Log;

/**
 * Que of points.
 * 
 * @author Hiroaki Tateshita
 * @version 0.6
 * 
 */
public class PointArray {
	/**
	 * 
	 */
	private Point[] pointArray;

	/**
	 * this is current size, not max size.
	 */
	private int size;

	/**
	 * 
	 */
	private int cursor;

	/**
	 * 
	 * @param _size_max
	 *            max size of array
	 */
	PointArray(int _size_max) {
		pointArray = new Point[_size_max];
		for (int i = 0; i < _size_max; i++) {
			pointArray[i] = new Point(-1000000, -1000000);
		}
		this.size = 0;
		cursor = 0;
	}

	/**
	 * 
	 * @param x
	 *            x of the point which will be added
	 * @param y
	 *            y of the point which will be added
	 */
	public final void addPoint(final double _x, final double _y) {
		if (this.size < pointArray.length - 1) {
			// pointArray[this.size] = new Point(_x, _y);
			pointArray[this.size].x = _x;
			pointArray[this.size].y = _y;
			this.size++;
			this.cursor++;
		} else if (this.size == pointArray.length - 1) {
			// pointArray[this.cursor] = new Point(_x, _y);
			pointArray[this.cursor].x = _x;
			pointArray[this.cursor].y = _y;
			if (this.cursor < pointArray.length - 1) {
				this.cursor++;
			} else if (this.cursor == pointArray.length - 1) {
				this.cursor = 0;
			} else {
				Log.e("hiro", "the cursor of pointArray is strange.");
			}
		} else {
			Log.e("hiro", "pointArray is wired");
		}
	}

	/**
	 * 
	 * @param index
	 *            from 0 to (size -1)
	 * @return x of the point of the index, if no value, Double.MAX_VALUE will
	 *         be returned
	 */
	public final double getX(final int index) {
		double result = Double.MAX_VALUE;
		if (index < this.size && index >= 0) {
			result = pointArray[index].x;
		}
		return result;
	}

	/**
	 * 
	 * @param index
	 *            from 0 to (size -1)
	 * @return y if no value, return Double.MAX_VALUE
	 */
	public final double getY(final int index) {
		double result = Double.MAX_VALUE;
		if (index < this.size && index >= 0) {
			result = pointArray[index].y;
		}
		return result;
	}

	/**
	 * 
	 * @return if no value, Double.MAX_VALUE will be returned
	 */
	public final double getLatestX() {
		double result = Double.MAX_VALUE;
		if (this.cursor == 0) {
			result = pointArray[this.size].x;
		} else if (this.cursor <= this.size) {
			result = pointArray[this.cursor - 1].x;
		}
		return result;
	}

	/**
	 * 
	 * @return if no value, Double.MAX_VALUE will be returned
	 */
	public final double getLatestY() {
		double result = Double.MAX_VALUE;
		if (this.cursor == 0) {
			result = pointArray[this.size].y;
		} else if (this.cursor <= this.size) {
			result = pointArray[this.cursor - 1].y;
		}
		return result;
	}

	/**
	 * 
	 * @author Hiroaki Tateshita
	 * 
	 */
	private class Point {
		/**
		 * 
		 */
		private double x;

		/**
		 * 
		 */
		private double y;

		/**
		 * 
		 * @param _x
		 * @param _y
		 */
		public Point(double _x, double _y) {
			this.x = _x;
			this.y = _y;
		}

		/**
		 * @return string
		 */
		public final String toString() {
			return "(" + this.x + ", " + this.y + ")";
		}
	}

	/**
	 * 
	 * @param deltaX
	 *            delta x
	 * @param deltaY
	 *            delta y
	 */
	public final void moveAll(final double deltaX, final double deltaY) {
		for (int i = 0; i < this.size; i++) {
			set_x(i, getX(i) + deltaX);
			set_y(i, getY(i) + deltaY);
		}
	}

	/**
	 * 
	 * @param i
	 *            index
	 * @param _x
	 *            x
	 */
	private void set_x(final int i, final double _x) {
		pointArray[i].x = _x;
	}

	private void set_y(int i, double _y) {
		pointArray[i].y = _y;

	}

	/**
	 * 
	 * @param zoomRate
	 * @param center_x
	 * @param center_y
	 */
	public final void zoomAll(double zoomRate, double center_x, double center_y) {
		for (int i = 0; i < this.size; i++) {
			set_x(i, getX(i) * zoomRate + (1 - zoomRate) * center_x);
			set_y(i, getY(i) * zoomRate + (1 - zoomRate) * center_y);
		}
	}

	/**
	 * 
	 * @param i
	 */
	public final void setSize(int i) {
		this.size = i;

	}

	/**
	 * 
	 * @return
	 */
	public final int getSize() {
		return this.size;
	}

}
