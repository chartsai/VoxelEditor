package com.chatea.voxeleditor;

import android.content.Context;

public class GLClickableMenu extends GLMenu {

    private ClickCallback mCallback = null;

    public GLClickableMenu(Context context, int resourceId, float x, float y, float width, float height) {
        super(context, resourceId, x, y, width, height);
    }

    public void setClickCallback(ClickCallback callback) {
        mCallback = callback;
    }

    public boolean isClick(float x, float y) {
        // AABB detection.
        if (getX() <= x && x <= getX() + getWidth()
                && getY() <= y && y <= getY() + getHeight()) {
            return true;
        }
        return false;
    }

    public void click() {
        if (mCallback != null) {
            mCallback.onClick(this);
        }
    }

    interface ClickCallback {
        void onClick(GLClickableMenu menu);
    }
}
