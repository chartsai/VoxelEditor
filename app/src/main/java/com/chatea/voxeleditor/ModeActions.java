package com.chatea.voxeleditor;

import android.view.MotionEvent;

import com.chatea.voxeleditor.widget.Cube;

public interface ModeActions {
    void handleMotionEvent(MotionEvent e);
    void handleClickCube(Cube cube);
}
