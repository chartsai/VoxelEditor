package com.chatea.voxeleditor;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.opengl.GLU;
import android.opengl.Matrix;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.widget.Toast;

import com.chatea.voxeleditor.utils.GLCamera;
import com.chatea.voxeleditor.utils.GLViewPort;
import com.chatea.voxeleditor.widget.Cube;

public class EditorCore implements EditorRenderer.RenderDataMaintainer {

    private static final float MOVEMENT_FACTOR_THETA = 180.0f / 320;
    private static final float MOVEMENT_FACTOR_PHI = 90.0f / 320;

    private static final long SCALE_INTERVAL = 330;

    private Context mContext;
    private EditorGLSurfaceView mGLSurfaceView;
    private EditorRenderer mRenderer;

    // control logic
    public enum Mode {
        None, // init state
        AddBlock,
        BreakBlock,
        Move,
        Rotate
    }
    private ModeActions mCurrentActions;
    private ModeActions mNoActions;
    private ModeActions mAddActions;
    private ModeActions mDragActions;
    private ModeActions mMoveActions;
    private ModeActions mBreakActions;

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
    private float[] mMenuProjectionMatrix = new float[16];

    // Renderable objects
    private VoxelPanel mVoxelPanel;
    private MenuPanel mMenuPanel;

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

        setupActionModes();
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

    private void setupActionModes() {
        mNoActions = new ModeActions() {
            @Override
            public void handleMotionEvent(MotionEvent e) {
                // only triggerClick, because we still need interaction with Menu.
                if (e.getPointerCount() > 1) {
                    return;
                }
                float x = e.getX();
                float y = e.getY();
                switch (e.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        triggerClick(x, y);
                        refresh();
                        break;
                }
                mPreviousX = x;
                mPreviousY = y;
            }

            @Override
            public void handleClickCube(Cube cube) {
            }
        };

        mAddActions = new ModeActions() {
            @Override
            public void handleMotionEvent(MotionEvent e) {
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
                        triggerClick(x, y);
                        refresh();
                        break;
                    case MotionEvent.ACTION_MOVE:
                        float dx = x - mPreviousX;
                        float dy = y - mPreviousY;
                        triggerDrag(dx, dy);
                        refresh();
                        break;
                }
                mPreviousX = x;
                mPreviousY = y;
            }

            @Override
            public void handleClickCube(Cube cube) {
                // TODO add cube
                float[] pickedCenter = cube.getCenter();
                float pickedX = pickedCenter[0];
                float pickedY = pickedCenter[1];
                float pickedZ = pickedCenter[2];

                switch (cube.getLastPickedPlane()) {
                    case Cube.BACK: {
                        mVoxelPanel.addCube(pickedX - 1, pickedY, pickedZ);
                        return;
                    }
                    case Cube.FRONT: {
                        mVoxelPanel.addCube(pickedX + 1, pickedY, pickedZ);
                        return;
                    }
                    case Cube.LEFT: {
                        mVoxelPanel.addCube(pickedX, pickedY - 1, pickedZ);
                        return;
                    }
                    case Cube.RIGHT: {
                        mVoxelPanel.addCube(pickedX, pickedY + 1, pickedZ);
                        return;
                    }
                    case Cube.TOP: {
                        mVoxelPanel.addCube(pickedX, pickedY, pickedZ + 1);
                        return;
                    }
                    case Cube.BOTTOM: {
                        mVoxelPanel.addCube(pickedX, pickedY, pickedZ - 1);
                        return;
                    }
                    default:
                        Log.e("TAG", "some error happened when panel pick cube");
                }
            }
        };

        mDragActions = new ModeActions() {
            @Override
            public void handleMotionEvent(MotionEvent e) {
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
                        triggerClick(x, y);
                        refresh();
                        break;
                    case MotionEvent.ACTION_MOVE:
                        float dx = x - mPreviousX;
                        float dy = y - mPreviousY;
                        triggerDrag(dx, dy);
                        refresh();
                        break;
                }
                mPreviousX = x;
                mPreviousY = y;
            }

            @Override
            public void handleClickCube(Cube cube) {
                // do nothing.
            }
        };

        mMoveActions = new ModeActions() {
            @Override
            public void handleMotionEvent(MotionEvent e) {
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
                        triggerClick(x, y);
                        refresh();
                        break;
                    case MotionEvent.ACTION_MOVE:
                        float dx = x - mPreviousX;
                        float dy = y - mPreviousY;

                        // TODO move camera.
                        triggerMoveCamera(dx, dy);

                        refresh();
                        break;
                }
                mPreviousX = x;
                mPreviousY = y;
            }

            @Override
            public void handleClickCube(Cube cube) {
                // do nothing.
            }
        };

        mBreakActions = new ModeActions() {
            @Override
            public void handleMotionEvent(MotionEvent e) {
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
                        triggerClick(x, y);
                        refresh();
                        break;
                    case MotionEvent.ACTION_MOVE:
                        float dx = x - mPreviousX;
                        float dy = y - mPreviousY;
                        triggerDrag(dx, dy);
                        refresh();
                        break;
                }
                mPreviousX = x;
                mPreviousY = y;
            }

            @Override
            public void handleClickCube(Cube cube) {
                mVoxelPanel.removeCube(cube);
            }
        };

        mCurrentActions = mNoActions;
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
            // check menu first.
            boolean pickMenu = false;
            float menuX = mClickX * MenuPanel.MENU_PANEL_WIDTH / mViewPort.width;
            // the y is inverse in GLMenu and MotionEvent coordinate
            // y in GL = real pixel number * (GLUnit / real pixel GL used)
            float menuY = (mViewPort.height - mClickY)
                    * MenuPanel.MENU_PANEL_HEIGHT / mViewPort.height;

            Log.d("TAG", "mViewPort.height - mClickY=" + (mViewPort.height - mClickY)
                    + ", " + "mClickY=" + mClickY + ", menuY=" + menuY);

            pickMenu = mMenuPanel.pick(menuX, menuY);

            if (!pickMenu) {
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

                    float[] eyePoint = new float[]{mCamera.eyeX, mCamera.eyeY, mCamera.eyeZ};
                    // TODO refactor the behavior into this.
                    mVoxelPanel.pick(mCurrentActions, eyePoint, pickRay);
                }
            }
            mCheckPick = false;
        }
    }

    private void render() {
        mGLSurfaceView.requestRender();
    }

    public void processTouchEvent(MotionEvent e) {
        mCurrentActions.handleMotionEvent(e);
    }

    private void triggerMoveCamera(float dx, float dy) {
        // TODO
    }

    private void triggerClick(float x, float y) {
        // TODO
        mClickX = x;
        mClickY = y;

        mCheckPick = true;
    }

    private void triggerDrag(float dx, float dy) {
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

    private void triggerScale(float scaleFactor) {
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
        mVoxelPanel = new VoxelPanel(this, 10, 10, 10);
        mMenuPanel = new MenuPanel(this, mContext);
    }

    @Override
    public void setViewPort(GLViewPort viewPort) {
        this.mViewPort = viewPort;

        final float viewDepth = 30;

        float ratio = (float) viewPort.width / viewPort.height;
        Matrix.frustumM(mProjectionMatrix, 0, -ratio, ratio, -1, 1, 1, viewDepth);

        Matrix.orthoM(mMenuProjectionMatrix, 0,
                0, MenuPanel.MENU_PANEL_WIDTH,
                0, MenuPanel.MENU_PANEL_HEIGHT,
                -1, 1);

        refresh();
    }

    @Override
    public void drawPanel() {
        float[] vpMatrix = new float[16];
        Matrix.multiplyMM(vpMatrix, 0, mProjectionMatrix, 0, mViewMatrix, 0);
        mVoxelPanel.draw(vpMatrix);
    }

    @Override
    public void drawMenu() {
        // Menu used identical View Matrix so projectionMatrix is vpMatrix
        mMenuPanel.draw(mMenuProjectionMatrix);
    }

    public void showToast(String str) {
        Toast.makeText(mContext, str, Toast.LENGTH_SHORT).show();
    }

    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            triggerScale(detector.getScaleFactor());
            mLastScaleTime = System.currentTimeMillis();

            return true;
        }
    }

    public void setMode(Mode mode) {
        switch(mode) {
            case AddBlock:
                mCurrentActions = mAddActions;
                break;
            case Rotate:
                mCurrentActions = mDragActions;
                break;
            case Move:
                mCurrentActions = mMoveActions;
                break;
            case BreakBlock:
                mCurrentActions = mBreakActions;
                break;
            case None:
                mCurrentActions = mNoActions;
                break;
        }
    }
}
