package com.chatea.voxeleditor.shader;

import android.opengl.GLES20;

import com.chatea.voxeleditor.EditorRenderer;

/**
 * FIXME some issues that the line cannot be drawn well.
 */
public class GLLineShader {

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

    public GLLineShader() {
        int vertexShader = EditorRenderer.loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode);
        int fragmentShader = EditorRenderer.loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode);

        mProgram = GLES20.glCreateProgram();

        GLES20.glAttachShader(mProgram, vertexShader);
        GLES20.glAttachShader(mProgram, fragmentShader);

        GLES20.glLinkProgram(mProgram);
    }

    public void draw(float[] mvpMatrix, float[] start, float[] end, float[] color, float width) {
        GLES20.glUseProgram(mProgram);

        int mPositionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition");
        GLES20.glEnableVertexAttribArray(mPositionHandle);
        float[] vertices = {
                start[0], start[1], start[2],
                end[0], end[1], end[2]
        };
        GLES20.glVertexAttrib3fv(mPositionHandle, vertices, 0);

        int mMVPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix");
        GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mvpMatrix, 0);

        int mColorHandle = GLES20.glGetUniformLocation(mProgram, "vColor");
        GLES20.glUniform4fv(mColorHandle, 1, color, 0);

        GLES20.glLineWidth(width);

        GLES20.glDrawArrays(GLES20.GL_LINES, 0, 2);

        // reset
        GLES20.glDisableVertexAttribArray(mPositionHandle);
        GLES20.glLineWidth(1);
        GLES20.glUseProgram(0);
    }
}