package com.crotaplague.torture.Files.ServerStorage;

import java.util.List;

public class Pair<E, C> {
    E left;
    C right;
    public E getLeft(){return left;}
    public C getRight(){return right;}
    public void setRight(C br){this.right = br;}
    public void setLeft(E br){this.left = br;}
    public Pair(E left, C right){
        this.left = left;
        this.right = right;
    }
    public Pair(E left){
        this.left = left;
        this.right = null;
    }
}
