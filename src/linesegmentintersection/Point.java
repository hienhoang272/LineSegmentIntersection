/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package linesegmentintersection;

/**
 *
 * @author HienHoang
 */
public class Point implements Comparable<Point> {

    double x, y;

    public Point(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public Point() {
    }

    @Override
    public String toString() {
        return "Point{" + "x=" + x + ", y=" + y + '}';
    }

    @Override
    public int compareTo(Point o) {
        if (Math.abs(x - o.x) <= AVLSweepTree.EPSILON) {
            if (Math.abs(y - o.y) <= AVLSweepTree.EPSILON) {
                return 0;
            }
            return Double.compare(y, o.y);
        }
        return Double.compare(x, o.x);
    }

}
