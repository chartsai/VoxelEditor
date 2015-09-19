package com.chatea.voxeleditor.utils;

public class GLViewPort {
    public int x;
    public int y;
    public int width;
    public int height;

    public GLViewPort(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public int[] toIntArray() {
        return new int[] {this.x, this.y, this.width, this.height};
    }
}
