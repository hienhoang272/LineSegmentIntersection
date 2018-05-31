/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package linesegmentintersection;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author HienHoang
 */
public class LineSegmentIntersection {
    
    private final static boolean DEBUG = false;
    
    final Comparator<Point> eventCmp = new Comparator<Point>() {
        @Override
        public int compare(Point o1, Point o2) {
            if (Math.abs(o1.y - o2.y) <= AVLSweepTree.EPSILON) {
                if (Math.abs(o1.x - o2.x) <= AVLSweepTree.EPSILON) {
                    return 0;
                }
                return Double.compare(o1.x, o2.x);
            }
            return Double.compare(o2.y, o1.y);
        }
    };
    
    Segment[] segments;
    TreeSet<Point> eventPoints;
    AVLSweepTree sweepIntersectionSegments;
    Map<Point, HashSet<Segment>> upperEndpointMap, lowerEndpointMap, containPointMap;
    Map<Point, List<Segment>> intersections;
    
    private void initValue() {
        for (int i = 0; i < segments.length; ++i) {
            Point p1 = segments[i].p1;
            Point p2 = segments[i].p2;
            HashSet<Segment> h1 = upperEndpointMap.get(p1);
            if (h1 == null) {
                h1 = new HashSet<>();
                upperEndpointMap.put(p1, h1);
            }
            h1.add(segments[i]);
            HashSet<Segment> h2 = lowerEndpointMap.get(p2);
            if (h2 == null) {
                h2 = new HashSet<>();
                lowerEndpointMap.put(p2, h2);
            }
            h2.add(segments[i]);
            eventPoints.add(p1);
            eventPoints.add(p2);
        }
    }
    
    public LineSegmentIntersection(Segment[] segments) {
        this.segments = segments;
        eventPoints = new TreeSet<>(eventCmp);
        upperEndpointMap = new TreeMap<>();
        lowerEndpointMap = new TreeMap<>();
        containPointMap = new TreeMap<>();
        sweepIntersectionSegments = new AVLSweepTree();
        intersections = new TreeMap<>();
        initValue();
    }

    //========= SWEEP LINE MOVE =============================//
    public void sweepLineAlgorithm() {
        double[] time = new double[10];
        long st, fi;
        int cnt = 0;
        while (!eventPoints.isEmpty()) {
            Point eventPoint = eventPoints.first();

//             debug
            if (DEBUG) {
                if (cnt == 34) {
                    System.out.println("");
                }
                System.out.format(
                        "\n============\n" + cnt + ". At event point: (%f, %f)\n",
                        eventPoint.x, eventPoint.y);
            }
            st = System.nanoTime();
            HashSet<Segment> uppers = upperEndpointMap.get(eventPoint);
            HashSet<Segment> lowers = lowerEndpointMap.get(eventPoint);
            HashSet<Segment> contains = containPointMap.get(eventPoint);
            fi = System.nanoTime();
            time[0] += ((double) (fi - st) / 1e9);
            
            uppers = uppers == null ? new HashSet<>() : uppers;
            lowers = lowers == null ? new HashSet<>() : lowers;
            contains = contains == null ? new HashSet<>() : contains;
            
            st = System.nanoTime();
            List<AVLNode<Segment>> prevSegments = sweepIntersectionSegments.
                    lowerList(eventPoint);
            List<AVLNode<Segment>> nextSegments = sweepIntersectionSegments.
                    upperList(eventPoint);
            fi = System.nanoTime();
            time[1] += ((double) (fi - st) / 1e9);
            
            prevSegments = prevSegments == null ? new ArrayList<>() : prevSegments;
            nextSegments = nextSegments == null ? new ArrayList<>() : nextSegments;
            
            st = System.nanoTime();
            List<AVLNode<Segment>> listOnEventPoint = sweepIntersectionSegments.
                    rangeLeaf(
                            prevSegments.isEmpty() ? null : prevSegments.
                                    get(prevSegments.size() - 1),
                            nextSegments.isEmpty() ? null : nextSegments.
                                    get(0),
                            false, false);
            HashSet<Segment> h = new HashSet<>();
            for (AVLNode<Segment> node : listOnEventPoint) {
                if (node.key.onSegment(eventPoint)) {
                    h.add(node.key);
                }
            }
            for (Segment s : uppers) {
                h.add(s);
            }
            for (Segment s : lowers) {
                h.add(s);
            }
            for (Segment s : contains) {
                h.add(s);
            }
            if (acceptIntersections(new ArrayList<>(h), eventPoint)) {
                for (Segment s : h) {
                    if (s.onSegment(eventPoint) && s.notEndpoint(eventPoint)) {
                        contains.add(s);
                    }
                }
                if (!contains.isEmpty()) {
                    containPointMap.put(eventPoint, contains);
                }
            }
            fi = System.nanoTime();
            time[2] += ((double) (fi - st) / 1e9);
            
            if (DEBUG) {
                System.out.println("Event point info: ");
                System.out.print("Upper: ");
                for (Segment s : uppers) {
                    System.out.print(s.id + " ");
                }
                System.out.print("\nLower: ");
                for (Segment s : lowers) {
                    System.out.print(s.id + " ");
                }
                System.out.print("\nContain: ");
                for (Segment s : contains) {
                    System.out.print(s.id + " ");
                }
            }
            st = System.nanoTime();
            if (uppers.size() + lowers.size() + contains.size() > 1) {
                List<Segment> ses = new LinkedList<>(lowers);
                ses.addAll(uppers);
                ses.addAll(contains);
                if (acceptIntersections(ses, eventPoint)) {
                    intersections.put(eventPoint, ses);
                }
            }
            fi = System.nanoTime();
            time[3] += ((double) (fi - st) / 1e9);
            
            st = System.nanoTime();
            for (Segment s : lowers) {
                sweepIntersectionSegments.delete(s);
            }
            for (Segment s : contains) {
                sweepIntersectionSegments.delete(s);
            }
            fi = System.nanoTime();
            time[4] += ((double) (fi - st) / 1e9);
            
            List<Segment> belows = new ArrayList<>(uppers);
            belows.addAll(contains);
            if (belows.isEmpty()) {
                st = System.nanoTime();
                if (!prevSegments.isEmpty() && !nextSegments.isEmpty()) {
                    int minYIdPrev = 0;
                    for (int i = 1; i < prevSegments.size(); ++i) {
                        if (prevSegments.get(minYIdPrev).key.p2.y
                                > prevSegments.get(i).key.p2.y) {
                            minYIdPrev = i;
                        }
                    }
                    int minYIdNext = 0;
                    for (int i = 1; i < nextSegments.size(); ++i) {
                        if (nextSegments.get(minYIdNext).key.p2.y > nextSegments.
                                get(i).key.p2.y) {
                            minYIdNext = i;
                        }
                    }
                    Point p = prevSegments.get(minYIdPrev).key.
                            intersectionPoint(nextSegments.get(minYIdNext).key);
                    if (p != null) {
                        if (eventCmp.compare(p, eventPoints.first()) > 0) {
                            eventPoints.add(p);
                        }
                        h = containPointMap.get(p);
                        if (h == null) {
                            h = new HashSet<>();
                            containPointMap.put(p, h);
                        }
                        for (AVLNode<Segment> node : prevSegments) {
                            if (node.key.onSegment(p) && node.key.notEndpoint(p)) {
                                h.add(node.key);
                            }
                        }
                        for (AVLNode<Segment> node : nextSegments) {
                            if (node.key.onSegment(p) && node.key.notEndpoint(p)) {
                                h.add(node.key);
                            }
                        }
                    }
                }
                fi = System.nanoTime();
                time[5] += ((double) (fi - st) / 1e9);
            } else {
                st = System.nanoTime();
                List<Segment> listSegments = new ArrayList<>();
                for (Segment s : belows) {
                    listSegments.add(s);
                }
                if (!listSegments.isEmpty()) {
                    Segment[] arrSegments = listSegments.toArray(
                            new Segment[listSegments.size()]);
                    updateContainArrSegments(arrSegments, prevSegments,
                            nextSegments);
                    sweepIntersectionSegments.insert(arrSegments, eventPoint);
                }
                fi = System.nanoTime();
                time[6] += ((double) (fi - st) / 1e9);
            }
            eventPoints.remove(eventPoint);

            // for debug
//            if (DEBUG) {
//                if (intersections.get(new Point(9, 0)) != null) {
//                    System.out.println("");
//                }
//            }
//            for (HashSet<Segment> hs : containPointMap.values()) {
//                if (hs.contains(segments[0])
//                        && hs.contains(segments[7])
//                        && hs.contains(segments[14])) {
//                    System.out.println("");
//                }
//            }
//            HashSet<Segment> hs = containPointMap.get(new Point(
//                    7.77777777777777777777777,
//                    4.5555555555555555555555));
//            if (hs != null && hs.contains(segments[18])) {
//                System.out.println("");
//            }
            if (DEBUG) {
                List<AVLNode<Segment>> inorderSegmentSweepline = sweepIntersectionSegments.
                        rangeLeaf(null, null);
                System.out.print("\nIn AVL Tree: ");
                for (AVLNode<Segment> node : inorderSegmentSweepline) {
                    System.out.print(node.key.id + " ");
                }
                System.out.println("");
                try {
                    appendFile(String.format("debug_tree_%d.dot", cnt),
                            sweepIntersectionSegments.toDotString(segments));
                } catch (IOException ex) {
                    Logger.getLogger(LineSegmentIntersection.class.getName()).
                            log(Level.SEVERE, null, ex);
                }
            }
            ++cnt;
        }
        if (DEBUG) {
            System.out.println("Counter: " + cnt);
            System.out.println("Time running: ");
            System.out.println("Get lowers/uppers/contains: " + time[0]);
            System.out.println("Get prev/next segments: " + time[1]);
            System.out.println("Find contains: " + time[2]);
            System.out.println("Found intersections: " + time[3]);
            System.out.println("Delete node: " + time[4]);
            System.out.println(
                    "Check segments intersections in event lower endpoint: " + time[5]);
            System.out.println(
                    "Check segments intersections in event upper and contain endpoint: " + time[6]);
        }
    }
    
    public void updateContainArrSegments(Segment[] segments,
            List<AVLNode<Segment>> prevSegments,
            List<AVLNode<Segment>> nextSegments) {
        class SegmentA {
            
            Segment s;
            double a;
            
            public SegmentA(Segment s, double a) {
                this.s = s;
                this.a = a;
            }
        }
        int maxYId = 0;
        for (int i = 1; i < segments.length; ++i) {
            if (segments[maxYId].p2.y < segments[i].p2.y) {
                maxYId = i;
            }
        }
        SegmentA[] segmentAs = new SegmentA[segments.length];
        for (int i = 0; i < segmentAs.length; ++i) {
            segmentAs[i] = new SegmentA(segments[i], segments[i].getA());
        }
        Arrays.sort(segmentAs, new Comparator<SegmentA>() {
            @Override
            public int compare(SegmentA o1, SegmentA o2) {
                if (Math.abs(o1.a - o2.a) <= AVLSweepTree.EPSILON) {
                    return 0;
                }
                return Double.compare(o1.a, o2.a);
            }
        });
        for (int i = 0; i < segments.length; ++i) {
            segments[i] = segmentAs[i].s;
        }
        List<Segment> leftMostSegments = new ArrayList<>();
        List<Segment> rightMostSegments = new ArrayList<>();
        leftMostSegments.add(segmentAs[0].s);
        rightMostSegments.add(segmentAs[segmentAs.length - 1].s);
        for (int i = 1; i < segmentAs.length; ++i) {
            if (segmentAs[i].a == segmentAs[0].a
                    || Math.abs(segmentAs[i].a - segmentAs[0].a) <= AVLSweepTree.EPSILON) {
                leftMostSegments.add(segmentAs[i].s);
            } else {
                break;
            }
        }
        for (int i = segmentAs.length - 2; i >= 0; --i) {
            if (segmentAs[i].a == segmentAs[segmentAs.length - 1].a
                    || Math.abs(
                            segmentAs[i].a - segmentAs[segmentAs.length - 1].a) <= AVLSweepTree.EPSILON) {
                rightMostSegments.add(segmentAs[i].s);
            } else {
                break;
            }
        }
        int minYIdLower = 0;
        for (int i = 1; i < prevSegments.size(); ++i) {
            if (eventCmp.compare(prevSegments.get(i).key.p2, prevSegments.get(
                    minYIdLower).key.p2) > 0) {
                minYIdLower = i;
            }
        }
        int minYIdUpper = 0;
        for (int i = 1; i < nextSegments.size(); ++i) {
            if (eventCmp.compare(nextSegments.get(i).key.p2, nextSegments.get(
                    minYIdUpper).key.p2) > 0) {
                minYIdUpper = i;
            }
        }
        int minYIdLeftMost = 0;
        for (int i = 1; i < leftMostSegments.size(); ++i) {
            if (eventCmp.compare(leftMostSegments.get(i).p2, leftMostSegments.
                    get(minYIdLeftMost).p2) > 0) {
                minYIdLeftMost = i;
            }
        }
        int minYIdRightMost = 0;
        for (int i = 1; i < rightMostSegments.size(); ++i) {
            if (eventCmp.compare(rightMostSegments.get(i).p2, rightMostSegments.
                    get(minYIdRightMost).p2) > 0) {
                minYIdRightMost = i;
            }
        }
        if (!prevSegments.isEmpty()) {
            Point p1 = leftMostSegments.get(minYIdLeftMost).intersectionPoint(
                    prevSegments.get(minYIdLower).key);
            if (p1 != null) {
                if (eventCmp.compare(p1, eventPoints.first()) > 0) {
                    eventPoints.add(p1);
                }
                HashSet<Segment> h = containPointMap.get(p1);
                if (h == null) {
                    h = new HashSet<>();
                    containPointMap.put(p1, h);
                }
                for (Segment s : leftMostSegments) {
                    if (s.onSegment(p1) && s.notEndpoint(p1)) {
                        h.add(s);
                    }
                }
                for (AVLNode<Segment> node : prevSegments) {
                    if (node.key.onSegment(p1) && node.key.notEndpoint(p1)) {
                        h.add(node.key);
                    }
                }
            }
        }
        if (!nextSegments.isEmpty()) {
            Point p2 = rightMostSegments.get(minYIdRightMost).intersectionPoint(
                    nextSegments.get(minYIdUpper).key);
            if (p2 != null) {
                if (eventCmp.compare(p2, eventPoints.first()) > 0) {
                    eventPoints.add(p2);
                }
                HashSet<Segment> h = containPointMap.get(p2);
                if (h == null) {
                    h = new HashSet<>();
                    containPointMap.put(p2, h);
                }
                for (Segment s : rightMostSegments) {
                    if (s.onSegment(p2) && s.notEndpoint(p2)) {
                        h.add(s);
                    }
                }
                for (AVLNode<Segment> node : nextSegments) {
                    if (node.key.onSegment(p2) && node.key.notEndpoint(p2)) {
                        h.add(node.key);
                    }
                }
            }
        }
    }
    
    public static boolean acceptIntersections(List<Segment> segments,
            Point intersectionPoint) {
        TreeSet<Double> numADiff = new TreeSet<>(
                new Comparator<Double>() {
            @Override
            public int compare(Double o1, Double o2) {
                if (Math.abs(o1 - o2) <= AVLSweepTree.EPSILON) {
                    return 0;
                }
                return o1.compareTo(o2);
            }
        });
        for (Segment s : segments) {
            numADiff.add(s.getA());
        }
        if (numADiff.size() > 1) {
            return true;
        }
        int cnt = 0;
        for (Segment s : segments) {
            if (s.p1.compareTo(intersectionPoint) == 0) {
                ++cnt;
            } else if (!(s.p2.compareTo(intersectionPoint) == 0)) {
                return false;
            }
        }
        return !(cnt == 0 || cnt == segments.size());
    }
    
    public static Map<Point, List<Segment>> bruteForceAlgorithm(
            Segment[] segments) {
        Map<Point, List<Segment>> intersections = new TreeMap<>();
        for (int i = 0; i < segments.length; ++i) {
            for (int j = i + 1; j < segments.length; ++j) {
                Point p = segments[i].intersectionPoint(segments[j]);
                if (p != null) {
                    List<Segment> l = intersections.get(p);
                    if (l == null) {
                        l = new LinkedList<>();
                        intersections.put(p, l);
                    }
                    l.add(segments[i]);
                    l.add(segments[j]);
                }
            }
        }
        return intersections;
    }
    
    public static boolean compareResult(Map<Point, List<Segment>>... maps) {
        for (int i = 0; i < maps.length; ++i) {
            if (maps[i].size() != maps[0].size()) {
                if (maps[i].size() > maps[0].size()) {
                    for (Point p : maps[i].keySet()) {
                        if (!maps[0].containsKey(p)) {
                            System.out.println("Map 0 not has point " + p);
                        }
                    }
                } else {
                    for (Point p : maps[0].keySet()) {
                        if (!maps[i].containsKey(p)) {
                            System.out.println(
                                    "Map " + i + " not has point " + p);
                        }
                    }
                }
                return false;
            }
        }
        for (Point p : maps[0].keySet()) {
            for (int i = 1; i < maps.length; ++i) {
                if (!maps[i].containsKey(p)) {
                    return false;
                } else {
                    HashSet<Segment> h1 = new HashSet<>(maps[0].get(p));
                    HashSet<Segment> h2 = new HashSet<>(maps[i].get(p));
                    if (h1.size() != h2.size()) {
                        System.out.println("Size different in " + p);
                        return false;
                    }
                    for (Segment s : h1) {
                        if (!h2.contains(s)) {
                            System.out.println(
                                    "In point: " + p + ", not on segment " + s.id);
                            return false;
                        }
                    }
                }
            }
        }
        
        return true;
    }
    
    public static void segmentsToFile(Segment[] segments, String path) {
        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(path));
            for (Segment s : segments) {
                bw.write(String.format("%s\t%s\t%s\t%s\n",
                        s.p1.x, s.p1.y,
                        s.p2.x, s.p2.y));
            }
            bw.close();
            
        } catch (IOException ex) {
            Logger.getLogger(LineSegmentIntersection.class
                    .getName()).
                    log(Level.SEVERE, null, ex);
        }
    }
    
    public static void intersectionsToFile(
            Map<Point, List<Segment>> intersections, String path) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(path))) {
            bw.write("List intersections point ==> list segments contain it\n");
            for (Point p : intersections.keySet()) {
                bw.write(String.format("[%s] ==> ", p));
                HashSet<Segment> segments = new HashSet<>(intersections.get(p));
                for (Segment s : segments) {
                    bw.write(String.format("%s ", s));
                }
                bw.write("\n");
            }
            bw.close();
            
        } catch (IOException ex) {
            Logger.getLogger(LineSegmentIntersection.class
                    .getName()).
                    log(Level.SEVERE, null, ex);
        }
    }
    
    public static void segmentsToArrPython(Segment[] segments, String path) {
        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(path));
            for (int i = 0; i < segments.length; ++i) {
                Segment s = segments[i];
                if (i == segments.length - 1) {
                    bw.write(String.format("[%s,\t%s,\t%s,\t%s]\n",
                            s.p1.x, s.p1.y,
                            s.p2.x, s.p2.y));
                } else {
                    bw.write(String.format("[%s,\t%s,\t%s,\t%s],\n",
                            s.p1.x, s.p1.y,
                            s.p2.x, s.p2.y));
                }
            }
            bw.close();
            
        } catch (IOException ex) {
            Logger.getLogger(LineSegmentIntersection.class
                    .getName()).
                    log(Level.SEVERE, null, ex);
        }
    }
    
    public static Segment[] readFile(String path) {
        try {
            BufferedReader br = new BufferedReader(new FileReader(path));
            String line;
            List<Segment> segments = new ArrayList<>();
            int i = 0;
            while ((line = br.readLine()) != null) {
                String[] ps = line.split("\t");
                double d1 = Double.parseDouble(ps[0]);
                double d2 = Double.parseDouble(ps[1]);
                double d3 = Double.parseDouble(ps[2]);
                double d4 = Double.parseDouble(ps[3]);
                segments.add(new Segment(new Point(d1, d2), new Point(d3, d4),
                        i++));
            }
            br.close();
            return segments.toArray(new Segment[segments.size()]);
            
        } catch (FileNotFoundException ex) {
            Logger.getLogger(LineSegmentIntersection.class
                    .getName()).
                    log(Level.SEVERE, null, ex);
            
        } catch (IOException ex) {
            Logger.getLogger(LineSegmentIntersection.class
                    .getName()).
                    log(Level.SEVERE, null, ex);
        }
        return null;
    }
    
    public static void batchTest() throws IOException {
//        int size = 1000;
        Random rd = new Random();
        long rdNumber;
        long start, end;
        if (!DEBUG) {
            rdNumber = System.nanoTime();
        } else {
            rdNumber = 136127503377388L;
        }
//        System.out.println(rdNumber);
        Segment[] segments;
//        segments = new Segment[size];
//        for (int i = 0; i < segments.length; ++i) {
//            segments[i] = new Segment(2000, rd, i);
//        }
//        segments = genTest(50, 20, 5, (int) 1e6, rd);
        if (DEBUG) {
            segments = readFile("data_sample" + rdNumber + ".txt");
        } else {
            for (int test = 0;; ++test) {
                System.out.println(test);
                segments = findBugs();
                if (segments != null) {
                    break;
                }
            }
            segmentsToFile(segments, "data_sample" + rdNumber + ".txt");
            segmentsToArrPython(segments,
                    "data_sample_arrpython" + rdNumber + ".txt");
        }
        Map<Point, List<Segment>> intersectionSweepLine, intersectionBruteForce;
        
        LineSegmentIntersection lsi = new LineSegmentIntersection(segments);
        start = System.nanoTime();
        lsi.sweepLineAlgorithm();
        end = System.nanoTime();
        intersectionSweepLine = lsi.intersections;
        System.out.println("SweepLine size: " + intersectionSweepLine.size());
        System.out.println("Time: " + (double) (end - start) / 1e9);
        
        start = System.nanoTime();
        intersectionBruteForce = bruteForceAlgorithm(segments);
        end = System.nanoTime();
        System.out.println("__________________");
        System.out.println("BruteForce size: " + intersectionBruteForce.size());
        System.out.println("Time: " + (double) (end - start) / 1e9);
        
        System.out.println("BruteForce: ");
        for (Point parent : intersectionBruteForce.keySet()) {
            List<Segment> l = intersectionBruteForce.get(parent);
            System.out.print(parent + " --> ");
            for (Segment s : l) {
                System.out.print(s.id + " ");
            }
            System.out.println("");
        }
        System.out.println("SweepLine: ");
        for (Point parent : intersectionSweepLine.keySet()) {
            List<Segment> l = intersectionSweepLine.get(parent);
            System.out.print(parent + " --> ");
            for (Segment s : l) {
                System.out.print(s.id + " ");
            }
            System.out.println("");
        }
        System.out.println("Correct: " + compareResult(intersectionSweepLine,
                intersectionBruteForce));
    }
    
    public static void appendFile(String path, String content) throws IOException {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(path, true))) {
            bw.write(content);
        }
    }
    
    public static Segment[] findBugs() {
        Random rd = new Random();
        Segment[] segments;
        segments = genTest(20, 300, 5, rd);
        Map<Point, List<Segment>> intersectionSweepLine, intersectionBruteForce;
        intersectionBruteForce = bruteForceAlgorithm(segments);
        LineSegmentIntersection lsi = new LineSegmentIntersection(segments);
        lsi.sweepLineAlgorithm();
        intersectionSweepLine = lsi.intersections;
        boolean rst = compareResult(intersectionBruteForce,
                intersectionSweepLine);
        System.out.println("Correct: " + rst);
        if (rst) {
            return null;
        }
        System.out.println("BruteForce size: " + intersectionBruteForce.size());
        System.out.println("SweepLine size: " + intersectionSweepLine.size());
        System.out.println("BruteForce: ");
        for (Point parent : intersectionBruteForce.keySet()) {
            List<Segment> l = intersectionBruteForce.get(parent);
            System.out.print(parent + " --> ");
            for (Segment s : l) {
                System.out.print(s.id + " ");
            }
            System.out.println("");
        }
        System.out.println("SweepLine: ");
        for (Point parent : intersectionSweepLine.keySet()) {
            List<Segment> l = intersectionSweepLine.get(parent);
            System.out.print(parent + " --> ");
            for (Segment s : l) {
                System.out.print(s.id + " ");
            }
            System.out.println("");
        }
        return segments;
    }
    
    public static Segment[] genTest(int numSegments, int bound, int maxLength,
            Random rd) {
        Segment[] segments = new Segment[numSegments];
//        int cnt = 0;
        TreeSet<Segment> set = new TreeSet<>(new Comparator<Segment>() {
            @Override
            public int compare(Segment o1, Segment o2) {
                if (o1.p1.compareTo(o2.p1) == 0) {
                    return o1.p2.compareTo(o2.p2);
                }
                return o1.p1.compareTo(o2.p1);
            }
        });
        for (int i = 0; i < segments.length; ++i) {
            segments[i] = new Segment(bound, rd, i, maxLength);
            if (!set.contains(segments[i])) {
                set.add(segments[i]);
            } else {
                --i;
            }
        }
        return segments;
    }
    
    public static void main(String[] args) throws IOException {
        Random rd = new Random();
        Segment[] segments;
        int numSegments = 5, bound = 5, maxLength = 5;
        long times = System.nanoTime();
        
        if (args.length == 3) {
            numSegments = Integer.parseInt(args[0]);
            bound = Integer.parseInt(args[1]);
            maxLength = Integer.parseInt(args[2]);
        }
        segments = genTest(numSegments, bound, maxLength, rd);
        String filename = String.format("test_%d_%d_%d_%d.txt", numSegments,
                bound, maxLength, times);
        segmentsToFile(segments, filename);
        segmentsToArrPython(segments, filename + ".python.txt");
        System.out.println("Saved test to file: " + filename);
        
        Map<Point, List<Segment>> intersectionSweepline, intersectionBruteforce;
        System.out.println(
                "Random done.\nNumber of segments: " + segments.length);
        
        LineSegmentIntersection lsi = new LineSegmentIntersection(segments);
        long time;
        time = System.nanoTime();
        lsi.sweepLineAlgorithm();
        intersectionSweepline = lsi.intersections;
        time = System.nanoTime() - time;
        System.out.println(
                "Sweepline found: " + intersectionSweepline.size() + " intersections");
        System.out.println("Sweepline time: " + (double) time / 1e9);
        
        time = System.nanoTime();
        intersectionBruteforce = bruteForceAlgorithm(segments);
        time = System.nanoTime() - time;
        System.out.println(
                "Bruteforce found: " + intersectionBruteforce.size() + " intersections");
        System.out.println("Bruteforce time: " + (double) time / 1e9);
        
        System.out.println("Correct: " + compareResult(intersectionSweepline,
                intersectionBruteforce));
        filename = String.format("result_%d_%d_%d_%d.txt", numSegments, bound,
                maxLength, times);
        intersectionsToFile(intersectionSweepline, filename);
        System.out.println("Saved result to: " + filename);

//        batchTest();
    }
}
