package com.chatea.voxeleditor;

import android.opengl.GLES20;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

public class GLCubeShader {

    public static final float EDGE_LENGTH = 1.0f;

    private FloatBuffer vertexBuffer;
    private ShortBuffer drawListBuffer;

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

    private final int mProgram;

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

    public GLCubeShader() {
        // initialize vertex byte buffer for shape coordinates
        // # of coordinate values * 4 bytes per float)
        ByteBuffer bb = ByteBuffer.allocateDirect(mCoords.length * 4);
        bb.order(ByteOrder.nativeOrder());
        vertexBuffer = bb.asFloatBuffer();
        vertexBuffer.put(mCoords);
        vertexBuffer.position(0);

        // initialize byte buffer for the draw list
        // (# of coordinate values * 2 bytes per short)
        ByteBuffer dlb = ByteBuffer.allocateDirect(drawOrder.length * 2);
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

    public void draw(float[] mvpMatrix, float[] color) {
        GLES20.glUseProgram(mProgram);

        int mPositionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition");
        GLES20.glEnableVertexAttribArray(mPositionHandle);
        GLES20.glVertexAttribPointer(mPositionHandle, COORDS_PER_VERTEX,
                GLES20.GL_FLOAT, false, 3 * 4, vertexBuffer);

        int mMVPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix");
        GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mvpMatrix, 0);

        int mColorHandle = GLES20.glGetUniformLocation(mProgram, "vColor");
        GLES20.glUniform4fv(mColorHandle, 1, color, 0);

        GLES20.glDrawElements(GLES20.GL_TRIANGLES, 3 * 2 * 6, GLES20.GL_UNSIGNED_SHORT, drawListBuffer);

        GLES20.glDisableVertexAttribArray(mPositionHandle);
    }
}