package com.chatea.voxeleditor;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.view.MotionEvent;

public class EditorGLSurfaceView extends GLSurfaceView {

    private EditorCore mCore;

    public EditorGLSurfaceView(Context context) {
        super(context);
        init();
    }

    public EditorGLSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public void init() {
        setEGLContextClientVersion(2);
    }

    public void setEditorCore(EditorCore core) {
        mCore = core;
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        mCore.processTouchEvent(e);
        return true;
    }
}