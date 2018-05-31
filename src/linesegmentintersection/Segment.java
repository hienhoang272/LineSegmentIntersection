/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package linesegmentintersection;

import java.util.Random;

/**
 *
 * @author HienHoang
 */
public class Segment {

    Point p1, p2;
    double xMax, xMin, yMax, yMin;
    int id;

    public Segment(int bound, Random rd, int id) {
        Point p1, p2;
        do {
            p1 = new Point((int) (rd.nextDouble() * bound),
                    (int) (rd.nextDouble() * bound));
            p2 = new Point((int) (rd.nextDouble() * bound),
                    (int) (rd.nextDouble() * bound));
        } while (p1.compareTo(p2) == 0);
        this.id = id;
        swap(p1, p2);
        setMinMax();
    }

    public Segment(Point p1, Point p2, int id) {
        this.id = id;
        swap(p1, p2);
        setMinMax();
    }

    public Segment(int bound, Random rd, int id, int maxLength) {
        Point p1, p2;
        do {
            p1 = new Point((int) (rd.nextDouble() * bound),
                    (int) (rd.nextDouble() * bound));
            double lx = rd.nextDouble() * maxLength * 2;
            double ly = rd.nextDouble() * maxLength * 2;
            p2 = new Point((int) (p1.x - maxLength + lx),
                    (int) (p1.y - maxLength + ly));
        } while (p1.compareTo(p2) == 0);
        this.id = id;
        swap(p1, p2);
        setMinMax();
    }

    private void swap(Point p1, Point p2) {
        if (p1.y == p2.y) {
            if (p1.x < p2.x) {
                this.p1 = p1;
                this.p2 = p2;
            } else {
                this.p2 = p1;
                this.p1 = p2;
            }
        } else if (p1.y > p2.y) {
            this.p1 = p1;
            this.p2 = p2;
        } else {
            this.p1 = p2;
            this.p2 = p1;
        }
    }

    private void setMinMax() {
        if (p1.x > p2.x) {
            xMax = p1.x;
            xMin = p2.x;
        } else {
            xMax = p2.x;
            xMin = p1.x;
        }
        if (p1.y > p2.y) {
            yMax = p1.y;
            yMin = p2.y;
        } else {
            yMax = p2.y;
            yMin = p1.y;
        }
    }

    public boolean onSegment(Point p) {
        if (Math.abs(
                (p.x - p1.x) * (p2.y - p1.y) - (p2.x - p1.x) * (p.y - p1.y)) <= AVLSweepTree.EPSILON) {
            return p.x <= xMax && p.x >= xMin && p.y <= yMax && p.y >= yMin;
        }
        return false;
    }

    public Point intersectionPoint(Point sweepLineThroughPoint) {
        if (yMax < sweepLineThroughPoint.y - AVLSweepTree.EPSILON || yMin - AVLSweepTree.EPSILON > sweepLineThroughPoint.y) {
            return null;
        }
        if (p1.y == p2.y) {
            return sweepLineThroughPoint;
        }
        return new Point((sweepLineThroughPoint.y - p1.y) * (p2.x - p1.x)
                / (p2.y - p1.y) + p1.x, sweepLineThroughPoint.y);
    }

    public Point intersectionPoint(Segment other) {
        double a1 = this.p2.y - this.p1.y;
        double b1 = this.p1.x - this.p2.x;
        double c1 = a1 * this.p1.x + b1 * this.p1.y;

        double a2 = other.p2.y - other.p1.y;
        double b2 = other.p1.x - other.p2.x;
        double c2 = a2 * other.p1.x + b2 * other.p1.y;

        double d = a1 * b2 - a2 * b1;
        if (Math.abs(d) <= AVLSweepTree.EPSILON) {
            if (p1.compareTo(other.p1) == 0) {
                double x = (p2.x - p1.x) * (other.p2.x - p1.x);
                if (x < 0) {
                    return p1;
                } else if (x == 0) {
                    double y = (p2.y - p1.y) * (other.p2.y - p1.y);
                    if (y < 0) {
                        return p1;
                    }
                }
            }
            if (p1.compareTo(other.p2) == 0) {
                double x = (p2.x - p1.x) * (other.p1.x - p1.x);
                if (x < 0) {
                    return p1;
                } else if (x == 0) {
                    double y = (p2.y - p1.y) * (other.p1.y - p1.y);
                    if (y < 0) {
                        return p1;
                    }
                }
            }
            if (p2.compareTo(other.p1) == 0) {
                double x = (p1.x - p2.x) * (other.p2.x - p2.x);
                if (x < 0) {
                    return p2;
                } else if (x == 0) {
                    double y = (p1.y - p2.y) * (other.p2.y - p2.y);
                    if (y < 0) {
                        return p2;
                    }
                }
            }
            if (p2.compareTo(other.p2) == 0) {
                double x = (p1.x - p2.x) * (other.p1.x - p2.x);
                if (x < 0) {
                    return p2;
                } else if (x == 0) {
                    double y = (p1.y - p2.y) * (other.p1.y - p2.y);
                    if (y < 0) {
                        return p2;
                    }
                }
            }
            return null;
        }

        double x = (b2 * c1 - b1 * c2) / d;
        double y = (a1 * c2 - a2 * c1) / d;
        if (x <= xMax && x >= xMin && y <= yMax && y >= yMin
                && x <= other.xMax && x >= other.xMin && y <= other.yMax && y >= other.yMin) {
            return new Point(x, y);
        }
        return null;
    }

    public boolean notEndpoint(Point p) {
        return p.compareTo(p1) != 0 && p.compareTo(p2) != 0;
    }

    public double getA() {
        return (p2.x - p1.x) / (p1.y - p2.y);
    }

    @Override
    public String toString() {
        return "Segment[" + id + "]:{" + "p1=" + p1 + ", p2=" + p2 + '}';
    }

    public static void main(String[] args) {
        System.out.println(1.0 / 0 == 2.0 / 0);
    }
}
