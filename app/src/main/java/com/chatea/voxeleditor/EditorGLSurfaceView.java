package com.chatea.voxeleditor;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;

public class EditorGLSurfaceView extends GLSurfaceView {

    private static final long SCALE_INTERVAL = 330;

    private EditorRenderer mRenderer;

    private float mPreviousX;
    private float mPreviousY;

    private ScaleGestureDetector mScaleDetector;

    private long mLastScaleTime;

    public EditorGLSurfaceView(Context context) {
        super(context);
        init(context);
    }

    public EditorGLSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public void init(Context context) {
        setEGLContextClientVersion(2);

        mRenderer = new EditorRenderer();
        setRenderer(mRenderer);

        setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);

        mScaleDetector = new ScaleGestureDetector(context, new ScaleListener());
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {

        if (e.getPointerCount() > 1) {
            mScaleDetector.onTouchEvent(e);
            return true;
        }

        if (System.currentTimeMillis() - mLastScaleTime < SCALE_INTERVAL) {
            // guard condition
            return true;
        }

        float x = e.getX();
        float y = e.getY();

        switch (e.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mRenderer.handleClick(x, y);
                requestRender();
                break;
            case MotionEvent.ACTION_MOVE:
                float dx = x - mPreviousX;
                float dy = y - mPreviousY;

                mRenderer.handleDrag(dx, dy);

                requestRender();
                break;
        }

        mPreviousX = x;
        mPreviousY = y;

        return true;
    }

    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            mRenderer.handleScale(detector.getScaleFactor());

            mLastScaleTime = System.currentTimeMillis();

            requestRender();
            return true;
        }
    }
}