package com.chatea.voxeleditor;

import android.opengl.Matrix;
import android.util.Log;

/**
 * TODO enum the face, change get triangle coordinate function.
 * make this class describe all triangle, and make GLCubeShader only draw triangle.
 */
public class Cube implements IPickable {

    public static final float EDGE_LENGTH = 1.0f;

    public static final int NONE = -1;
    /**
     * X+
     */
    public static final int FRONT = 0;
    /**
     * Z+
     */
    public static final int TOP = 1;
    /**
     * Y-
     */
    public static final int LEFT = 2;
    /**
     * Z-
     */
    public static final int BOTTOM = 3;
    /**
     * Y+
     */
    public static final int RIGHT = 4;
    /**
     * X-
     */
    public static final int BACK = 5;

    private float[] mCenter = {0f, 0f, 0f};

    // number of coordinates per vertex in this array
    private static final float HALF_EDGE = EDGE_LENGTH / 2;

    private float mCoords[] = {
            HALF_EDGE, -HALF_EDGE, HALF_EDGE,   // front top left
            HALF_EDGE, -HALF_EDGE, -HALF_EDGE,  // front bottom left
            HALF_EDGE, HALF_EDGE, -HALF_EDGE,   // front bottom right
            HALF_EDGE, HALF_EDGE, HALF_EDGE,    // front top right
            -HALF_EDGE, -HALF_EDGE, HALF_EDGE,  // back top left
            -HALF_EDGE, -HALF_EDGE, -HALF_EDGE, // back bottom left
            -HALF_EDGE, HALF_EDGE, -HALF_EDGE,  // back bottom right
            -HALF_EDGE, HALF_EDGE, HALF_EDGE,   // back top right
    };

    private short mDrawOrder[] = {
            0, 1, 2, 0, 2, 3,  // order to draw front
            4, 0, 3, 4, 3, 7,  // order to draw top
            4, 5, 1, 4, 1, 0,  // order to draw left
            1, 5, 6, 1, 6, 2,  // order to draw bottom
            3, 2, 6, 3, 6, 7,  // order to draw right
            7, 6, 5, 7, 5, 4   // order to draw back
    };

    // Set color with red, green, blue and alpha (opacity) values
    private float mColor[] = { 0.1f, 0.6f, 0.5f, 1.0f };

    private int mLastPickedPlane = NONE;

    public Cube() {
        setCenter(0, 0, 0);
    }

    public void setCenter(float x, float y, float z) {
        for (int i = 0; i < mCoords.length; i += 3) {
            mCoords[i + 0] = mCoords[i + 0] - mCenter[0] + x;
            mCoords[i + 1] = mCoords[i + 1] - mCenter[1] + y;
            mCoords[i + 2] = mCoords[i + 2] - mCenter[2] + z;
        }
        mCenter[0] = x;
        mCenter[1] = y;
        mCenter[2] = z;
    }

    public void setColor(float r, float g, float b, float a) {
        mColor[0] = r;
        mColor[1] = g;
        mColor[2] = b;
        mColor[3] = a;
    }

    public void draw(float[] vpMatrix, GLCubeShader shader) {
        float[] mvpMatrix = new float[16];

        float[] moduleMatrix = new float[16];
        Matrix.setIdentityM(moduleMatrix, 0);
        Matrix.translateM(moduleMatrix, 0, mCenter[0], mCenter[1], mCenter[2]);
        Matrix.multiplyMM(mvpMatrix, 0, vpMatrix, 0, moduleMatrix, 0);

        shader.draw(mvpMatrix, mColor);
    }

    private float[] getTriangleCoordinate(int number) {
        float[] coordinates = new float[3];
        coordinates[0] = mCoords[number * 3];
        coordinates[1] = mCoords[number * 3 + 1];
        coordinates[2] = mCoords[number * 3 + 2];
        return coordinates;
    }

    @Override
    public boolean isPicked(float[] eye, float[] pickRay, float[] pickedPoint) {
        float[] nearestPoint = null;
        float currentDistSquare = -1;
        int pickedPlane = -1;
        for (int i = 0; i < mDrawOrder.length; i += 3) { // check the triangle one by one.
            float[] a = getTriangleCoordinate(mDrawOrder[i]);
            float[] b = getTriangleCoordinate(mDrawOrder[i + 1]);
            float[] c = getTriangleCoordinate(mDrawOrder[i + 2]);
            float[] crossPoint = Utils.getLineTriangleCrossPoint(eye, pickRay, a, b, c);

            if (crossPoint != null) {
                float newDistSquare = Utils.getDistanceSquare(eye, crossPoint);
                if (nearestPoint == null) {
                    nearestPoint = crossPoint;
                    currentDistSquare = newDistSquare;
                    pickedPlane = i / 3;
                } else if (newDistSquare < currentDistSquare) {
                    nearestPoint = crossPoint;
                    currentDistSquare = newDistSquare;
                    pickedPlane = i / 3;
                } else {
                    // keep old point and dist.
                }
            }
        }

        switch (pickedPlane) {
            case 0:
            case 1:
                mLastPickedPlane = FRONT;
                break;
            case 2:
            case 3:
                mLastPickedPlane = TOP;
                break;
            case 4:
            case 5:
                mLastPickedPlane = LEFT;
                break;
            case 6:
            case 7:
                mLastPickedPlane = BOTTOM;
                break;
            case 8:
            case 9:
                mLastPickedPlane = RIGHT;
                break;
            case 10:
            case 11:
                mLastPickedPlane = BACK;
                break;
            case -1:
            default:
                mLastPickedPlane = NONE;
                break;
        }
        Log.d("TAG", "pick " + pickedPlane);

        if (nearestPoint != null) {
            if (pickedPoint != null) {
                for (int i = 0; i < pickedPoint.length; i++) {
                    pickedPoint[i] = nearestPoint[i];
                }
            }
            return true;
        }
        return false;
    }

    public float[] getCenter() {
        return mCenter;
    }

    public int getLastPickedPlane() {
        return mLastPickedPlane;
    }
}