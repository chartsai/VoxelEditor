package com.chatea.voxeleditor;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.GLU;
import android.opengl.Matrix;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class EditorRenderer implements GLSurfaceView.Renderer {
    private static final float MOVEMENT_FACTOR_THETA = 180.0f / 320;
    private static final float MOVEMENT_FACTOR_PHI = 90.0f / 320;

    private VoxelPanel mPanel;
    private GLCube mCube;

    private int[] mViewPort = new int[4];
    private float[] mEyePoint = new float[3];
    private float mViewDistance = 5.0f;

    private float mTheta = 90;
    private float mPhi = 0;

    // mMVPMatrix is an abbreviation for "Model View Projection Matrix"
    private final float[] mMVPMatrix = new float[16];
    private final float[] mProjectionMatrix = new float[16];
    private final float[] mViewMatrix = new float[16];

    private float mClickX;
    private float mClickY;

    public EditorRenderer() {
    }

    /**
     * Tool used loader
     * @param type GLES20.GL_VERTEX_SHADER, GLES20.GL_FRAGMENT_SHADER
     * @param shaderCode
     * @return
     */
    public static int loadShader(int type, String shaderCode){
        int shader = GLES20.glCreateShader(type);

        // add the source code to the shader and compile it
        GLES20.glShaderSource(shader, shaderCode);
        GLES20.glCompileShader(shader);

        return shader;
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        GLES20.glClearColor(0.0f, 0.3f, 0.3f, 1.0f);

        // setup back cull function.
        GLES20.glEnable(GLES20.GL_CULL_FACE);
        GLES20.glFrontFace(GLES20.GL_CCW);
        GLES20.glCullFace(GLES20.GL_BACK);

        // setup depth test function. We need this since we use shader.
        GLES20.glEnable(GLES20.GL_DEPTH_TEST);
        GLES20.glClearDepthf(1.0f);
        GLES20.glDepthFunc(GLES20.GL_LEQUAL);
        GLES20.glDepthMask(true);

        mCube = new GLCube();
        mPanel = new VoxelPanel(10, 10, 10);
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        GLES20.glViewport(0, 0, width, height);
        mViewPort = new int[] {0, 0, width, height};

        float ratio = (float) width / height;

        Matrix.frustumM(mProjectionMatrix, 0, -ratio, ratio, -1, 1, 1, 9);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT);

        updateEyePosition();

        Matrix.setLookAtM(mViewMatrix, 0,
                mEyePoint[0], mEyePoint[1], mEyePoint[2],
                0f, 0f, 0f,
                0f, 0f, mTheta % 360 < 180 ? 1.0f : -1.0f);

        Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mViewMatrix, 0);

        // do ray-picking.
        float[] touchedPoint = new float[4];
        float[] pickRay = new float[3];

        float glWindowX = mClickX;
        float glWindowY = mViewPort[3] - mClickY;

        // winZ = 0 is the nearest plan, winZ = 1 is the farest plan.
        GLU.gluUnProject(glWindowX, glWindowY, 1, mViewMatrix, 0, mProjectionMatrix, 0, mViewPort, 0, touchedPoint, 0);

        if (touchedPoint[3] != 0) {
            for (int i = 0; i < 3; i++) {
                pickRay[i] = touchedPoint[i] / touchedPoint[3] - mEyePoint[i];
            }

            // TODO do picked.
//            mPanel.isPicked(mEyePoint, pickRay, null);
        }

        mPanel.draw(mMVPMatrix);
    }

    public void handleClick(float x, float y) {
        // TODO
        mClickX = x;
        mClickY = y;
    }

    public void handleDrag(float dx, float dy) {
        mTheta -= MOVEMENT_FACTOR_PHI * dy;

        while (mTheta < 0) {
            mTheta += 360;
        }
        mTheta = mTheta % 360;

        mPhi -= MOVEMENT_FACTOR_THETA * dx * (mTheta < 180? 1: -1);
        while (mPhi < 0) {
            mPhi += 360;
        }
        mPhi = mPhi % 360;

        updateEyePosition();
    }

    public void handleScale(float scaleFactor) {
        mViewDistance *= (1.0f / scaleFactor);
        updateEyePosition();
    }

    private void updateEyePosition() {
        float theta = mTheta % 360;
        float phi = mPhi % 360;

        double radianceTheta = theta * Math.PI / 180;
        double radiancePhi = phi * Math.PI / 180;
        mEyePoint[0] = (float) (mViewDistance * Math.sin(radianceTheta) * Math.cos(radiancePhi));
        mEyePoint[1] = (float) (mViewDistance * Math.sin(radianceTheta) * Math.sin(radiancePhi));
        mEyePoint[2] = (float) (mViewDistance * Math.cos(radianceTheta));
    }
}
