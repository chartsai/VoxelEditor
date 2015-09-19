package com.chatea.voxeleditor.menu;

public interface GLMenu {
    /**
     * try to do pick
     * @param x
     * @param y
     * @return true if picked, false else.
     */
    boolean tryToPick(float x, float y);
}
