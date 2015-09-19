package com.chatea.voxeleditor.menu;

import android.content.Context;

public class GLClickableMenu extends GLAbstractMenu {

    private OnClickCallback mCallback = null;

    public GLClickableMenu(Context context, int resourceId, float x, float y, float width, float height) {
        super(context, resourceId, x, y, width, height);
    }

    public void setClickCallback(OnClickCallback callback) {
        mCallback = callback;
    }

    @Override
    public boolean tryToPick(float x, float y) {
        // AABB detection.
        if (getX() <= x && x <= getX() + getWidth()
                && getY() - getHeight() <= y && y <= getY()) {
            if (mCallback != null) {
                mCallback.onClick(this);
            }
            return true;
        }
        return false;
    }

    public interface OnClickCallback {
        void onClick(GLClickableMenu menu);
    }
}
