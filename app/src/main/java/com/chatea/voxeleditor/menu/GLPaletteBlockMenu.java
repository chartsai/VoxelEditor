package com.chatea.voxeleditor.menu;

import android.opengl.GLES20;
import android.opengl.Matrix;

import com.chatea.voxeleditor.EditorRenderer;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

public class GLPaletteBlockMenu implements GLRenderableMenu {

    private static final String vertexShaderCode =
            "uniform mat4 uMVPMatrix;" +
            "attribute vec4 vPosition;" +
            "void main() {" +
            "  gl_Position = uMVPMatrix * vPosition;" +
            "}";

    /**
     * Note:
     * We cannot declare attribute in fragment shader,
     * so declare vTextureCoord in vertex shader and
     * use a varying to pass to fragment shader.
     */
    private static final String fragmentShaderCode =
            "precision mediump float;" +
            "uniform vec4 vColor;" +
            "void main() {" +
            "  gl_FragColor = vColor;" +
            "}";

    private static final int COORDS_PER_VERTEX = 3;

    private FloatBuffer vertexBuffer;
    private ShortBuffer indexBuffer;
    private int mProgram;

    private float mX;
    private float mY;
    private float mWidth;
    private float mHeight;
    private float mR;
    private float mG;
    private float mB;

    private boolean mSelected = false;
    private OnSelectCallback mCallback = null;

    public GLPaletteBlockMenu(float x, float y, float width, float height, float r, float g, float b) {
        mX = x;
        mY = y;
        mWidth = width;
        mHeight = height;

        mR = r;
        mG = g;
        mB = b;

        setup(width, height);
    }

    private void setup(float width, float height) {
        setupShader();
        setupSquare(width, height);
    }

    private void setupShader() {
        int vertexShader = EditorRenderer.loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode);
        int fragmentShader = EditorRenderer.loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode);

        mProgram = GLES20.glCreateProgram();

        GLES20.glAttachShader(mProgram, vertexShader);
        GLES20.glAttachShader(mProgram, fragmentShader);

        GLES20.glLinkProgram(mProgram);
    }

    private void setupSquare(float width, float height) {
        // We have create the vertices of our view.
        float[] vertices = {
                0.0f, 0.0f, 0.0f, // top-left
                0.0f, -height, 0.0f, // bottom-left
                width, -height, 0.0f, // bottom-right
                width, 0.0f, 0.0f, // top-right
        };

        short[] indices = {0, 1, 2, 0, 2, 3};

        // The vertex buffer.
        ByteBuffer bb = ByteBuffer.allocateDirect(vertices.length * 4);
        bb.order(ByteOrder.nativeOrder());
        vertexBuffer = bb.asFloatBuffer();
        vertexBuffer.put(vertices);
        vertexBuffer.position(0);

        // initialize byte buffer for the draw list
        ByteBuffer dlb = ByteBuffer.allocateDirect(indices.length * 2);
        dlb.order(ByteOrder.nativeOrder());
        indexBuffer = dlb.asShortBuffer();
        indexBuffer.put(indices);
        indexBuffer.position(0);
    }

    public float getX() {
        return mX;
    }

    public float getY() {
        return mY;
    }

    public float getWidth() {
        return mWidth;
    }

    public float getHeight() {
        return mHeight;
    }

    public float[] getColor() {
        return new float[] {mR, mG, mB, 1.0f};
    }

    @Override
    public void draw(float[] vpMatrix, float alpha) {
        GLES20.glUseProgram(mProgram);

        float[] mvpMatrix = new float[16];
        float[] moduleMatrix = new float[16];
        Matrix.setIdentityM(moduleMatrix, 0);
        Matrix.translateM(moduleMatrix, 0, mX, mY, 0);
        Matrix.multiplyMM(mvpMatrix, 0, vpMatrix, 0, moduleMatrix, 0);

        int mPositionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition");
        GLES20.glEnableVertexAttribArray(mPositionHandle);
        GLES20.glVertexAttribPointer(mPositionHandle, COORDS_PER_VERTEX,
                GLES20.GL_FLOAT, false, 0, vertexBuffer);

        int mMVPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix");
        GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mvpMatrix, 0);

        float[] color = {mR, mG, mB, alpha};
        int mColorHandle = GLES20.glGetUniformLocation(mProgram, "vColor");
        GLES20.glUniform4fv(mColorHandle, 1, color, 0);

        GLES20.glDrawElements(GLES20.GL_TRIANGLES, 3 * 2, GLES20.GL_UNSIGNED_SHORT, indexBuffer);

        GLES20.glDisableVertexAttribArray(mPositionHandle);
    }

    @Override
    public boolean tryToPick(float x, float y) {
        // AABB detection.
        if (getX() <= x && x <= getX() + getWidth()
                && getY() - getHeight() <= y && y <= getY()) {
            if (!mSelected) {
                // only do select when it has not been selected.
                select();
            }
            return true;
        }
        return false;
    }

    public void setOnSelectCallback(OnSelectCallback callback) {
        mCallback = callback;
    }

    public void select() {
        mSelected = true;

        if (mCallback != null) {
            mCallback.onSelected(this);
        }
    }

    public void setSelection(boolean selected) {
        mSelected = selected;
    }

    public interface OnSelectCallback {
        void onSelected(GLPaletteBlockMenu menu);
    }
}
