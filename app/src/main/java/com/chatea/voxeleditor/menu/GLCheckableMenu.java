package com.chatea.voxeleditor.menu;

import android.content.Context;

public class GLCheckableMenu extends GLAbstractMenu {

    private boolean mChecked = false;
    private OnCheckCallback mCallback = null;

    public GLCheckableMenu(Context context, int resourceId, float x, float y, float width, float height) {
        super(context, resourceId, x, y, width, height);
    }

    public void setCheckCallback(OnCheckCallback callback) {
        mCallback = callback;
    }

    @Override
    public boolean tryToPick(float x, float y) {
        // AABB detection.
        if (getX() <= x && x <= getX() + getWidth()
                && getY() <= y && y <= getY() + getHeight()) {
            mChecked = !mChecked;
            if (mCallback != null) {
                mCallback.onCheck(this, mChecked);
            }
            return true;
        }
        return false;
    }

    public interface OnCheckCallback {
        void onCheck(GLCheckableMenu menu, boolean checked);
    }
}
