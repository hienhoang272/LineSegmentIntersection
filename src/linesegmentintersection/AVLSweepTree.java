/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package linesegmentintersection;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 *
 * @author HienHoang
 */
public class AVLSweepTree extends AVLTree<Segment> {

    public final static double EPSILON = 1e-8;

    public AVLSweepTree() {
        root = null;
        map = new HashMap<>();
    }

    public AVLSweepTree(AVLNode<Segment> root) {
        this.root = root;
        map = new HashMap<>();
        map.put(root.key, root);
    }

    public AVLSweepTree(Segment t) {
        root = new AVLNode<>(t, 1, null, null, null);
        map.put(t, root);
    }

    public void insert(Segment[] segments, Point eventPoint) {
        int startIdx = 0;
        AVLNode<Segment> lowerNode = lower(eventPoint);
        if (lowerNode == null) {
            if (root != null) {
                lowerNode = findMinNodeSubTree(root);
                lowerNode.rightChild = new AVLNode<>(
                        lowerNode.key, 1, null, null, lowerNode);
                lowerNode.leftChild = new AVLNode<>(
                        segments[0], 1, null, null, lowerNode);
                lowerNode.height = 2;
                lowerNode.key = lowerNode.leftChild.key;
                map.put(lowerNode.leftChild.key, lowerNode.leftChild);
                map.put(lowerNode.rightChild.key, lowerNode.rightChild);
                if (lowerNode.parent != null) {
                    updateHeight(lowerNode.parent);
                    makeBalance(lowerNode.parent);
                }
                lowerNode = lowerNode.leftChild;
            } else {
                root = new AVLNode<>(segments[0], 1, null, null, null);
                map.put(root.key, root);
                lowerNode = root;
            }
            startIdx = 1;
        }
        for (int i = startIdx; i < segments.length; ++i) {
            lowerNode.leftChild = new AVLNode<>(lowerNode.key, 1, null, null,
                    lowerNode);
            lowerNode.rightChild = new AVLNode<>(segments[i], 1, null, null,
                    lowerNode);
            lowerNode.height = 2;
            map.put(lowerNode.leftChild.key, lowerNode.leftChild);
            map.put(lowerNode.rightChild.key, lowerNode.rightChild);
            AVLNode<Segment> temp = lowerNode;
            while (temp.parent != null && temp.parent.rightChild == temp) {
                temp = temp.parent;
            }
            if (temp.parent != null) {
                temp.parent.key = segments[i];
            }
            AVLNode<Segment> nextLowerNode = lowerNode.rightChild;
            if (lowerNode.parent != null) {
                updateHeight(lowerNode.parent);
                makeBalance(lowerNode.parent);
            }
            lowerNode = nextLowerNode;
        }
    }

    @Override
    public void delete(Segment s) {
        AVLNode<Segment> node = map.get(s);
        if (node != null) {
            if (node == root) {
                root = null;
                map.remove(s);
            } else if (node.parent.leftChild == node) {
                if (node.parent == root) {
                    root = root.rightChild;
                    root.parent = null;
                    map.remove(s);
                } else {
                    if (node.parent.parent.leftChild == node.parent) {
                        node.parent.parent.leftChild = node.parent.rightChild;
                        node.parent.parent.leftChild.parent = node.parent.parent;
                    } else {
                        node.parent.parent.rightChild = node.parent.rightChild;
                        node.parent.parent.rightChild.parent = node.parent.parent;
                    }
                    map.remove(s);
                    updateHeight(node.parent.parent);
                    makeBalance(node.parent.parent);
                }
            } else if (node.parent == root) {
                root = root.leftChild;
                root.parent = null;
                map.remove(s);
            } else {
                if (node.parent.parent.leftChild == node.parent) {
                    node.parent.parent.leftChild = node.parent.leftChild;
                    node.parent.parent.leftChild.parent = node.parent.parent;
                    node.parent.parent.key = findMaxNodeSubTree(
                            node.parent.parent.leftChild).key;
                } else {
                    node.parent.parent.rightChild = node.parent.leftChild;
                    node.parent.parent.rightChild.parent = node.parent.parent;
                    AVLNode<Segment> temp = node.parent.parent;
                    while (temp.parent != null && temp.parent.rightChild == temp) {
                        temp = temp.parent;
                    }
                    if (temp.parent != null) {
                        temp.parent.key = findMaxNodeSubTree(
                                node.parent.leftChild).key;
                    }
                }
                map.remove(s);
                updateHeight(node.parent.parent);
                makeBalance(node.parent.parent);
            }
        }
    }

    public AVLNode<Segment> lowerLeaf(AVLNode<Segment> leaf) {
        if (leaf == null) {
            if (root != null) {
                return findMaxNodeSubTree(root);
            }
            return null;
        }
        while (leaf.parent != null && leaf.parent.leftChild == leaf) {
            leaf = leaf.parent;
        }
        if (leaf.parent == null) {
            return null;
        }
        return findMaxNodeSubTree(leaf.parent.leftChild);
    }

    public AVLNode<Segment> upperLeaf(AVLNode<Segment> leaf) {
        if (leaf == null) {
            if (root != null) {
                return findMinNodeSubTree(root);
            }
            return null;
        }
        while (leaf.parent != null && leaf.parent.rightChild == leaf) {
            leaf = leaf.parent;
        }
        if (leaf.parent == null) {
            return null;
        }
        return findMinNodeSubTree(leaf.parent.rightChild);
    }

    public AVLNode<Segment> lower(Point eventPoint) {
        if (root == null) {
            return null;
        }
        AVLNode<Segment> node = root;
        while (true) {
            if (node.leftChild != null && node.rightChild != null) {
                Point p = node.key.intersectionPoint(eventPoint);
                if (p == null) {
                    System.out.println("");
                }
                if (eventPoint.x <= p.x + EPSILON) {
                    node = node.leftChild;
                } else {
                    node = node.rightChild;
                }
            } else {
                Point p = node.key.intersectionPoint(eventPoint);
                if (eventPoint.x <= p.x + EPSILON) {
                    node = lowerLeaf(node);
                }
                return node;
            }
        }
    }

    public List<AVLNode<Segment>> rangeLeaf(AVLNode<Segment> start,
            AVLNode<Segment> end) {
        return rangeLeaf(start, end, true, true);
    }

    public List<AVLNode<Segment>> rangeLeaf(
            AVLNode<Segment> start, AVLNode<Segment> end,
            boolean hasStart, boolean hasEnd) {
        List<AVLNode<Segment>> list = new ArrayList<>();
        if (start == end) {
            if (start != null) {
                if (hasStart && hasEnd) {
                    list.add(start);
                }
                return list;
            }
        }
        if (hasStart && start != null) {
            list.add(start);
        }
        while (true) {
            start = upperLeaf(start);
            if (start == end || start == null) {
                break;
            }
            list.add(start);
        }
        if (hasEnd && end != null) {
            list.add(end);
        }
        return list;
    }

    public List<AVLNode<Segment>> lowerList(Point eventPoint) {
        AVLNode<Segment> firstLower = lower(eventPoint);
        if (firstLower == null) {
            return null;
        }
        Point p = firstLower.key.intersectionPoint(eventPoint);
        AVLNode<Segment> secondLower = lower(p);
        List<AVLNode<Segment>> rst;
        if (secondLower == null) {
            rst = rangeLeaf(null, firstLower);
        } else {
            rst = rangeLeaf(upperLeaf(secondLower), firstLower);
        }
        if (rst.size() <= 1) {
            return rst;
        }
        NodeA[] segmentAs = new NodeA[rst.size()];
        for (int i = 0; i < segmentAs.length; ++i) {
            segmentAs[i] = new NodeA(rst.get(i), rst.get(i).key.getA());
        }
        Arrays.sort(segmentAs);
        rst = new ArrayList<>();
        for (int i = segmentAs.length - 1; i >= 0; --i) {
            if (Math.abs(segmentAs[i].a - segmentAs[segmentAs.length - 1].a) <= EPSILON) {
                rst.add(segmentAs[i].node);
            } else {
                break;
            }
        }
        return rst;
    }

    private class NodeA implements Comparable<NodeA> {

        AVLNode<Segment> node;
        double a;

        public NodeA(AVLNode<Segment> node, double a) {
            this.node = node;
            this.a = a;
        }

        @Override
        public int compareTo(NodeA o) {
            if (Math.abs(a - o.a) <= EPSILON) {
                return 0;
            }
            return Double.compare(a, o.a);
        }
    }

    public AVLNode<Segment> upper(Point eventPoint) {
        if (root == null) {
            return null;
        }
        AVLNode<Segment> node = root;
        while (true) {
            if (node.leftChild != null && node.rightChild != null) {
                Point p = node.key.intersectionPoint(eventPoint);
                if (eventPoint.x + EPSILON >= p.x) {
                    node = node.rightChild;
                } else {
                    node = node.leftChild;
                }
            } else {
                Point p = node.key.intersectionPoint(eventPoint);
                if (eventPoint.x + EPSILON >= p.x) {
                    node = upperLeaf(node);
                }
                return node;
            }
        }
    }

    public List<AVLNode<Segment>> upperList(Point eventPoint) {
        AVLNode<Segment> firstUpper = upper(eventPoint);
        if (firstUpper == null) {
            return null;
        }
        Point p = firstUpper.key.intersectionPoint(eventPoint);
        AVLNode<Segment> secondUpper = upper(p);
        List<AVLNode<Segment>> rst;
        if (secondUpper == null) {
            rst = rangeLeaf(firstUpper, null);
        } else {
            rst = rangeLeaf(firstUpper, lowerLeaf(secondUpper));
        }
        if (rst.size() <= 1) {
            return rst;
        }
        NodeA[] segmentAs = new NodeA[rst.size()];
        for (int i = 0; i < segmentAs.length; ++i) {
            segmentAs[i] = new NodeA(rst.get(i), rst.get(i).key.getA());
        }
        Arrays.sort(segmentAs);
        rst = new ArrayList<>();
        for (int i = 0; i < segmentAs.length; ++i) {
            if (Math.abs(segmentAs[i].a - segmentAs[0].a) <= EPSILON) {
                rst.add(segmentAs[i].node);
            } else {
                break;
            }
        }
        return rst;
    }

}
