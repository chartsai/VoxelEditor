package com.chatea.voxeleditor;

import android.opengl.GLES20;
import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

public class GLCube implements IPickable {

    public static final float EDGE_LENGTH = 1.0f;

    public static final int NONE = -1;
    public static final int FRONT = 0;
    public static final int TOP = 1;
    public static final int LEFT = 2;
    public static final int BOTTOM = 3;
    public static final int RIGHT = 4;
    public static final int BACK = 5;

    private FloatBuffer vertexBuffer;
    private ShortBuffer drawListBuffer;

    private float[] mCenter = {0f, 0f, 0f};

    // number of coordinates per vertex in this array
    private static final int COORDS_PER_VERTEX = 3;
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

    private short drawOrder[] = {
            0, 1, 2, 0, 2, 3,  // order to draw front
            4, 0, 3, 4, 3, 7,  // order to draw top
            4, 5, 1, 4, 1, 0,  // order to draw left
            1, 5, 6, 1, 6, 2,  // order to draw bottom
            3, 2, 6, 3, 6, 7,  // order to draw right
            7, 6, 5, 7, 5, 4   // order to draw back
    };

    // Set color with red, green, blue and alpha (opacity) values
    private float mColor[] = { 0.1f, 0.6f, 0.5f, 1.0f };

    private final int mProgram;

    private int mLastPickedPlane = NONE;

    private final String vertexShaderCode =
            "uniform mat4 uMVPMatrix;" +
            "attribute vec4 vPosition;" +
            "void main() {" +
            "  gl_Position = uMVPMatrix * vPosition;" +
            "}";

    private final String fragmentShaderCode =
            "precision mediump float;" +
            "uniform vec4 vColor;" +
            "void main() {" +
            "  gl_FragColor = vColor;" +
            "}";

    public GLCube() {
        setCenter(0, 0, 0);

        // initialize byte buffer for the draw list
        ByteBuffer dlb = ByteBuffer.allocateDirect(
                // (# of coordinate values * 2 bytes per short)
                drawOrder.length * 2);
        dlb.order(ByteOrder.nativeOrder());
        drawListBuffer = dlb.asShortBuffer();
        drawListBuffer.put(drawOrder);
        drawListBuffer.position(0);

        int vertexShader = EditorRenderer.loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode);
        int fragmentShader = EditorRenderer.loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode);

        mProgram = GLES20.glCreateProgram();

        GLES20.glAttachShader(mProgram, vertexShader);
        GLES20.glAttachShader(mProgram, fragmentShader);

        GLES20.glLinkProgram(mProgram);
    }

    public void setCenter(float x, float y, float z) {
        for (int i = 0; i < mCoords.length; i += 3) {
            mCoords[i + 0] = mCoords[i + 0] - mCenter[0] + x;
            mCoords[i + 1] = mCoords[i + 1] - mCenter[1] + y;
            mCoords[i + 2] = mCoords[i + 2] - mCenter[2] + z;
        }

        Log.d("TAG", "mCoords[0, 1, 2] = (" + mCoords[0] + "," + mCoords[1] + "," + mCoords[2] + ")");

        // initialize vertex byte buffer for shape coordinates
        ByteBuffer bb = ByteBuffer.allocateDirect(
                // (# of coordinate values * 4 bytes per float)
                mCoords.length * 4);
        bb.order(ByteOrder.nativeOrder());
        vertexBuffer = bb.asFloatBuffer();
        vertexBuffer.put(mCoords);
        vertexBuffer.position(0);

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

    public void draw(float[] mvpMatrix) {
        GLES20.glUseProgram(mProgram);

        int mPositionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition");
        GLES20.glEnableVertexAttribArray(mPositionHandle);
        GLES20.glVertexAttribPointer(mPositionHandle, COORDS_PER_VERTEX,
                GLES20.GL_FLOAT, false, 3 * 4, vertexBuffer);

        Log.d("TAG", "vertexBuffer[0, 1, 2] = (" + vertexBuffer.get(0) + "," + vertexBuffer.get(1)
                + "," + vertexBuffer.get(2) + ")");

        int mMVPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix");
        GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mvpMatrix, 0);

        int mColorHandle = GLES20.glGetUniformLocation(mProgram, "vColor");
        GLES20.glUniform4fv(mColorHandle, 1, mColor, 0);

        GLES20.glDrawElements(GLES20.GL_TRIANGLES, 3 * 2 * 6, GLES20.GL_UNSIGNED_SHORT, drawListBuffer);

        GLES20.glDisableVertexAttribArray(mPositionHandle);
    }

    private float[] getPointCoordinate(int index) {
        float[] coordinates = new float[3];
        coordinates[0] = mCoords[index * 3];
        coordinates[1] = mCoords[index * 3 + 1];
        coordinates[2] = mCoords[index * 3 + 2];
        return coordinates;
    }

    @Override
    public boolean isPicked(float[] eye, float[] pickRay, float[] pickedPoint) {
        float[] nearestPoint = null;
        float currentDistSquare = -1;
        int pickedPlane = -1;
        for (int i = 0; i < drawOrder.length; i += 3) { // check the square one by one.
            float[] a = getPointCoordinate(drawOrder[i]);
            float[] b = getPointCoordinate(drawOrder[i + 1]);
            float[] c = getPointCoordinate(drawOrder[i + 2]);
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

    public int getLastPickedPlane() {
        return mLastPickedPlane;
    }
}