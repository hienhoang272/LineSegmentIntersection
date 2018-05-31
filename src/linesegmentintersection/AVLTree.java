/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package linesegmentintersection;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 *
 * @author HienHoang
 * @param <T>
 */
public class AVLTree<T> {

    AVLNode<T> root;
    HashMap<T, AVLNode<T>> map;

    public AVLTree(T t, HashMap<T, AVLNode<T>> map) {
        root = new AVLNode<>(t, 0, null, null, null);
        this.map = map;
    }

    public AVLTree(T t) {
        root = new AVLNode<>(t, 1, null, null, null);
        map = new HashMap<>();
        map.put(t, root);
    }

    public AVLTree() {
        map = new HashMap<>();
    }

    public void updateHeight(AVLNode<T> node) {
        int h = Math.max(getHeight(node.leftChild), getHeight(node.rightChild)) + 1;
        if (node.height != h) {
            node.height = h;
            if (node.parent != null) {
                updateHeight(node.parent);
            }
        }
    }

    public void insert(T t, T afterKey) {
        AVLNode<T> node = map.get(afterKey);
        if (node != null) {
            if (node.rightChild == null) {
                node.rightChild = new AVLNode<>(t, 1, null, null, node);
                updateHeight(node);
                makeBalance(node);
                map.put(t, node.rightChild);
            } else {
                insert(t, node.rightChild);
            }
        } else if (root == null) {
            root = new AVLNode<>(t, 1, null, null, null);
            map.put(t, root);
        } else {
            insert(t, root);
        }
    }

    private void insert(T t, AVLNode<T> node) {
        node = findMinNodeSubTree(node);
        node.leftChild = new AVLNode<>(t, 1, null, null, node);
        updateHeight(node);
        makeBalance(node);
        map.put(t, node.leftChild);
    }

    public void delete(T keyDelete) {
        AVLNode<T> node = map.get(keyDelete);
        if (node != null) {
            delete(node);
            map.remove(keyDelete);
        }
    }

    private void delete(AVLNode<T> node) {
        if (node.leftChild == null) {
            if (node.rightChild == null) {
                if (node == root) {
                    root = null;
                } else {
                    if (node.parent.leftChild == node) {
                        node.parent.leftChild = null;
                    } else {
                        node.parent.rightChild = null;
                    }
                    updateHeight(node.parent);
                    makeBalance(node.parent);
                }
            } else if (node == root) {
                root = node.rightChild;
                node.rightChild.parent = null;
            } else {
                if (node.parent.leftChild == node) {
                    node.parent.leftChild = node.rightChild;
                    node.rightChild.parent = node.parent;
                } else {
                    node.parent.rightChild = node.rightChild;
                    node.rightChild.parent = node.parent;
                }
                updateHeight(node.parent);
                makeBalance(node.parent);
            }
        } else if (node.rightChild == null) {
            if (node == root) {
                root = node.leftChild;
                node.leftChild.parent = null;
            } else {
                if (node.parent.leftChild == node) {
                    node.parent.leftChild = node.leftChild;
                    node.leftChild.parent = node.parent;
                } else {
                    node.parent.rightChild = node.leftChild;
                    node.leftChild.parent = node.parent;
                }
                updateHeight(node.parent);
                makeBalance(node.parent);
            }
        } else {
            AVLNode<T> successor = findMinNodeSubTree(node.rightChild);
            node.key = successor.key;
            map.put(node.key, node);
            delete(successor);
        }
    }

    public AVLNode<T> findMinNodeSubTree(AVLNode<T> node) {
        while (node.leftChild != null) {
            node = node.leftChild;
        }
        return node;
    }

    public AVLNode<T> findMaxNodeSubTree(AVLNode<T> node) {
        while (node.rightChild != null) {
            node = node.rightChild;
        }
        return node;
    }

    private AVLNode<T> leftRotate(AVLNode<T> node) {
        AVLNode<T> x = node;
        AVLNode<T> y = x.rightChild;
        AVLNode<T> t2 = y.leftChild;
        x.parent = y;
        y.leftChild = x;
        x.rightChild = t2;
        if (t2 != null) {
            t2.parent = x;
        }
        x.height = Math.max(getHeight(x.leftChild), getHeight(x.rightChild)) + 1;
        y.height = Math.max(getHeight(y.leftChild), getHeight(y.rightChild)) + 1;
        return y;
    }

    private AVLNode<T> rightRotate(AVLNode<T> node) {
        AVLNode<T> y = node;
        AVLNode<T> x = y.leftChild;
        AVLNode<T> t2 = x.rightChild;
        y.parent = x;
        x.rightChild = y;
        y.leftChild = t2;
        if (t2 != null) {
            t2.parent = y;
        }
        y.height = Math.max(getHeight(y.leftChild), getHeight(y.rightChild)) + 1;
        x.height = Math.max(getHeight(x.leftChild), getHeight(x.rightChild)) + 1;
        return x;
    }

    private int getHeight(AVLNode<T> node) {
        return node == null ? 0 : node.height;
    }

    public void makeBalance(AVLNode<T> node) {
        if (node == null) {
            return;
        }
        int balance = getHeight(node.leftChild) - getHeight(node.rightChild);
        AVLNode<T> parentNode = node.parent;
        if (balance > 1) {
            int childBalance = getHeight(node.leftChild.leftChild) - getHeight(
                    node.leftChild.rightChild);
            if (childBalance < 0) {
                node.leftChild = leftRotate(node.leftChild);
                node.leftChild.parent = node;
                updateHeight(node);
            }
            if (parentNode == null) {
                this.root = rightRotate(node);
                this.root.parent = null;
            } else if (parentNode.leftChild == node) {
                parentNode.leftChild = rightRotate(node);
                parentNode.leftChild.parent = parentNode;
                updateHeight(parentNode);
            } else {
                parentNode.rightChild = rightRotate(node);
                parentNode.rightChild.parent = parentNode;
                updateHeight(parentNode);
            }
        } else if (balance < -1) {
            int childBalance = getHeight(node.rightChild.leftChild) - getHeight(
                    node.rightChild.rightChild);
            if (childBalance > 0) {
                node.rightChild = rightRotate(node.rightChild);
                node.rightChild.parent = node;
                updateHeight(node);
            }
            if (parentNode == null) {
                this.root = leftRotate(node);
                this.root.parent = null;
            } else if (parentNode.leftChild == node) {
                parentNode.leftChild = leftRotate(node);
                parentNode.leftChild.parent = parentNode;
                updateHeight(parentNode);
            } else {
                parentNode.rightChild = leftRotate(node);
                parentNode.rightChild.parent = parentNode;
                updateHeight(parentNode);
            }
        }
        makeBalance(parentNode);
    }

    public T lower(T t) {
        AVLNode<T> node = map.get(t);
        if (node == null) {
            if (root != null) {
                return findMaxNodeSubTree(root).key;
            } else {
                return null;
            }
        }
        if (node.leftChild != null) {
            return findMaxNodeSubTree(node.leftChild).key;
        } else {
            while (node.parent != null) {
                if (node.parent.rightChild == node) {
                    return node.parent.key;
                }
                node = node.parent;
            }
            return null;
        }
    }

    public T upper(T t) {
        AVLNode<T> node = map.get(t);
        if (node == null) {
            if (root != null) {
                return findMinNodeSubTree(root).key;
            } else {
                return null;
            }
        }
        if (node.rightChild != null) {
            return findMinNodeSubTree(node.rightChild).key;
        } else {
            while (node.parent != null) {
                if (node.parent.leftChild == node) {
                    return node.parent.key;
                }
                node = node.parent;
            }
            return null;
        }
    }

    public String toDotString(T[] ts) {
        Map<T, Integer> mapIdx = new HashMap<>();
        int cnt = 0;
        for (T t : ts) {
            mapIdx.put(t, cnt++);
        }
        StringBuilder sb = new StringBuilder();
        sb.append("graph{\n    node[shape=record];\n");
        List<List<AVLNode<T>>> list = new ArrayList<>();
        List<AVLNode<T>> temp = new ArrayList<>();
        if (root != null) {
            temp.add(root);
        }
        HashMap<AVLNode<T>, Integer> map2 = new HashMap<>();
        cnt = 0;
        list.add(temp);
        for (int i = 0; i < list.size(); ++i) {
            List<AVLNode<T>> temp2 = new ArrayList<>();
            temp = list.get(i);
            for (int j = 0; j < temp.size(); ++j) {
                map2.put(temp.get(j), cnt);
                sb.append("node").append(cnt).append(
                        "[label=\"{").append(mapIdx.get(temp.get(j).key)).
                        append("|{<left>|<right>}}\"];\n");
                if (temp.get(j).leftChild != null) {
                    temp2.add(temp.get(j).leftChild);
                }
                if (temp.get(j).rightChild != null) {
                    temp2.add(temp.get(j).rightChild);
                }
                ++cnt;
            }
            if (!temp2.isEmpty()) {
                list.add(temp2);
            }
        }
        for (int i = 0; i < list.size(); ++i) {
            temp = list.get(i);
            for (int j = 0; j < list.get(i).size(); ++j) {
                if (temp.get(j).leftChild != null) {
                    sb.append("node").append(map2.get(temp.get(j))).
                            append(":left -- node").append(map2.get(temp.
                            get(j).leftChild)).append(";\n");
                }
                if (temp.get(j).rightChild != null) {
                    sb.append("node").append(map2.get(temp.get(j))).
                            append(":right -- node").append(map2.get(temp.get(
                            j).rightChild)).append(";\n");
                }
            }
        }
        sb.append("}");
        return sb.toString();
    }

    public String toInorderString() {
        return toInorderString(root);
    }

    public List<T> toInorderKey() {
        return toInorderKey(root);
    }

    private List<T> toInorderKey(AVLNode<T> node) {
        List<T> list = new LinkedList<>();
        if (node == null) {
            return list;
        }
        list.addAll(toInorderKey(node.leftChild));
        list.add(node.key);
        list.addAll(toInorderKey(node.rightChild));
        return list;
    }

    private String toInorderString(AVLNode<T> node) {
        if (node == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        sb.append(toInorderString(node.leftChild));
        sb.append(node.key).append("\n");
        sb.append(toInorderString(node.rightChild));
        return sb.toString();
    }

    public boolean checkVaild() {
        if (root == null) {
            return true;
        }
        if (root.parent != null) {
            return false;
        }
        if (!checkVaild(root)) {
            return false;
        }
        for (AVLNode<T> node : map.values()) {
            if (node.leftChild != null && node.leftChild.parent != node) {
                return false;
            }
            if (node.rightChild != null && node.rightChild.parent != node) {
                return false;
            }
        }
        return true;
    }

    public boolean checkVaild(AVLNode<T> node) {
        if (node.leftChild != null) {
            if (node.leftChild.parent != node) {
                return false;
            }
            if (!checkVaild(node.leftChild)) {
                return false;
            }
        }
        if (node.rightChild != null) {
            if (node.rightChild.parent != node) {
                return false;
            }
            if (!checkVaild(node.rightChild)) {
                return false;
            }
        }
        return true;
    }

    public static void main(String[] args) {

    }
}
