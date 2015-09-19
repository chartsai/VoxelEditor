package com.chatea.voxeleditor.widget;

import android.util.Log;

import com.chatea.voxeleditor.shader.GLLineShader;

public class Line {

    private float[] mStart = {0f, 0f, 0f};
    private float[] mEnd = {0f, 0f, 0f};

    private float mWidth = 1f;

    // Set color with red, green, blue and alpha (opacity) values
    private float mColor[] = { 1.0f, 1.0f, 1.0f, 1.0f };

    public Line() {
    }

    public Line(float startX, float startY, float startZ,
                float endX, float endY, float endZ) {
        mStart[0] = startX;
        mStart[1] = startY;
        mStart[2] = startZ;
        mEnd[0] = endX;
        mEnd[1] = endY;
        mEnd[2] = endZ;
    }

    public Line(float[] start, float[] end) {
        this(start[0], start[1], start[2], end[0], end[1], end[2]);
    }

    public void setStart(float x, float y, float z) {
        mStart[0] = x;
        mStart[1] = y;
        mStart[2] = z;
    }

    public void setEnd(float x, float y, float z) {
        mEnd[0] = x;
        mEnd[1] = y;
        mEnd[2] = z;
    }

    public void setColor(float r, float g, float b, float a) {
        mColor[0] = r;
        mColor[1] = g;
        mColor[2] = b;
        mColor[3] = a;
    }

    public void setWidth(float width) {
        mWidth = width;
    }

    public void draw(float[] vpMatrix, GLLineShader shader) {
        Log.d("TAG", "line is been draw");
        shader.draw(vpMatrix, mStart, mEnd, mColor, mWidth);
    }
}