/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package linesegmentintersection;

/**
 *
 * @author HienHoang
 * @param <T>
 */
public class AVLNode<T> {

    T key;
    int height;
    AVLNode<T> leftChild, rightChild, parent;

    public AVLNode(T key, int height, AVLNode<T> leftChild,
            AVLNode<T> rightChild, AVLNode<T> parent) {
        this.key = key;
        this.height = height;
        this.leftChild = leftChild;
        this.rightChild = rightChild;
        this.parent = parent;
    }

}
