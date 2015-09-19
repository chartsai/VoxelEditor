package com.chatea.voxeleditor;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.opengl.GLU;
import android.opengl.Matrix;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;

import com.chatea.voxeleditor.utils.GLCamera;
import com.chatea.voxeleditor.utils.GLViewPort;

import java.util.HashSet;
import java.util.Set;

public class EditorCore implements EditorRenderer.RenderController {

    private static final float MOVEMENT_FACTOR_THETA = 180.0f / 320;
    private static final float MOVEMENT_FACTOR_PHI = 90.0f / 320;

    private static final long SCALE_INTERVAL = 330;

    private Context mContext;
    private EditorGLSurfaceView mGLSurfaceView;
    private EditorRenderer mRenderer;

    // camera related
    /**
     * Will be set from Render.
     */
    private GLViewPort mViewPort;
    private GLCamera mCamera = new GLCamera();
    private float mViewDistance = 5.0f;
    private float mTheta = 90;
    private float mPhi = 0;

    private float[] mViewMatrix = new float[16];
    private float[] mProjectionMatrix = new float[16];

    // Renderable objects
    private Set<IRenderable> mRenderableObjects = new HashSet<>();
    private VoxelPanel mVoxelPanel;

    // Touch events.
    private boolean mCheckPick;
    private float mClickX;
    private float mClickY;
    private float mPreviousX;
    private float mPreviousY;
    private ScaleGestureDetector mScaleDetector;
    private long mLastScaleTime;

    public EditorCore(Context context, EditorGLSurfaceView glSurfaceView) {
        mContext = context;

        setupViews(glSurfaceView);

        setupGestureDetector();
    }

    private void setupViews(EditorGLSurfaceView glSurfaceView) {
        mGLSurfaceView = glSurfaceView;
        mGLSurfaceView.setEditorCore(this);

        mRenderer = new EditorRenderer(this);
        mGLSurfaceView.setRenderer(mRenderer);
        mGLSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
    }

    private void setupGestureDetector() {
        mScaleDetector = new ScaleGestureDetector(mContext, new ScaleListener());
    }

    private void refresh() {
        update();

        // TODO consider handle FPS here
        render();
    }

    private void update() {
        updateEyePosition();

        Matrix.setLookAtM(mViewMatrix, 0,
                mCamera.eyeX, mCamera.eyeY, mCamera.eyeZ,
                mCamera.centerX, mCamera.centerY, mCamera.centerZ,
                mCamera.upX, mCamera.upY, mCamera.upZ);

        if (mCheckPick) {
            // do ray-picking.
            float[] touchedPoint = new float[4];
            float[] pickRay = new float[3];

            float glWindowX = mClickX;
            float glWindowY = mViewPort.height - mClickY;

            // winZ = 0 is the nearest plan, winZ = 1 is the farest plan.
            GLU.gluUnProject(glWindowX, glWindowY, 1,
                    mViewMatrix, 0, mProjectionMatrix, 0, mViewPort.toIntArray(), 0, touchedPoint, 0);

            if (touchedPoint[3] != 0) {

                pickRay[0] = touchedPoint[0] / touchedPoint[3] - mCamera.eyeX;
                pickRay[1] = touchedPoint[1] / touchedPoint[3] - mCamera.eyeY;
                pickRay[2] = touchedPoint[2] / touchedPoint[3] - mCamera.eyeZ;

                float[] eyePoint = new float[] {mCamera.eyeX, mCamera.eyeY, mCamera.eyeZ};
                mVoxelPanel.pick(eyePoint, pickRay);
            }
            mCheckPick = false;
        }
    }

    private void render() {
        mGLSurfaceView.requestRender();
    }

    public void sendTouchEvent(MotionEvent e) {

        if (e.getPointerCount() > 1) {
            mScaleDetector.onTouchEvent(e);
            refresh();
            return;
        }

        if (System.currentTimeMillis() - mLastScaleTime < SCALE_INTERVAL) {
            // guard condition
            return;
        }

        float x = e.getX();
        float y = e.getY();

        switch (e.getAction()) {
            case MotionEvent.ACTION_DOWN:

                handleClick(x, y);

                refresh();
                break;
            case MotionEvent.ACTION_MOVE:
                float dx = x - mPreviousX;
                float dy = y - mPreviousY;

                handleDrag(dx, dy);

                refresh();
                break;
        }

        mPreviousX = x;
        mPreviousY = y;
    }

    public void handleClick(float x, float y) {
        // TODO
        mClickX = x;
        mClickY = y;

        mCheckPick = true;
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
    }

    public void handleScale(float scaleFactor) {
        mViewDistance *= (1.0f / scaleFactor);
    }

    private void updateEyePosition() {
        float theta = mTheta % 360;
        float phi = mPhi % 360;

        double radianceTheta = theta * Math.PI / 180;
        double radiancePhi = phi * Math.PI / 180;

        mCamera.eyeX = (float) (mViewDistance * Math.sin(radianceTheta) * Math.cos(radiancePhi));
        mCamera.eyeY = (float) (mViewDistance * Math.sin(radianceTheta) * Math.sin(radiancePhi));
        mCamera.eyeZ = (float) (mViewDistance * Math.cos(radianceTheta));

        mCamera.centerX = 0f;
        mCamera.centerY = 0f;
        mCamera.centerZ = 0f;

        mCamera.upX = 0f;
        mCamera.upY = 0f;
        mCamera.upZ = mTheta % 360 < 180 ? 1.0f : -1.0f;
    }

    @Override
    public void createRenderObject() {
        mVoxelPanel = new VoxelPanel(10, 10, 10);

        mRenderableObjects.add(mVoxelPanel);
    }

    @Override
    public void setViewPort(GLViewPort viewPort) {
        this.mViewPort = viewPort;

        final float viewDepth = 30;

        float ratio = (float) viewPort.width / viewPort.height;
        Matrix.frustumM(mProjectionMatrix, 0, -ratio, ratio, -1, 1, 1, viewDepth);

        refresh();
    }

    @Override
    public float[] getProjectionMatrix() {
        return mProjectionMatrix;
    }

    @Override
    public float[] getViewMatrix() {
        return mViewMatrix;
    }

    @Override
    public Set<IRenderable> getRenderableObjects() {
        return mRenderableObjects;
    }

    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            handleScale(detector.getScaleFactor());
            mLastScaleTime = System.currentTimeMillis();

            return true;
        }
    }
}
