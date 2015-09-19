package com.chatea.voxeleditor.menu;


import android.content.Context;

public class GLSelectableMenu extends GLAbstractMenu {

    private boolean mSelected = false;
    private OnSelectCallback mCallback = null;

    public GLSelectableMenu(Context context, int resourceId,
                          float x, float y, float width, float height) {
        super(context, resourceId, x, y, width, height);
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

    @Override
    public void draw(float[] vpMatrix, float alpha) {
        super.draw(vpMatrix, alpha * (mSelected? 1.0f: 0.8f));
    }

    @Override
    public boolean tryToPick(float x, float y) {
        // AABB detection.
        if (getX() <= x && x <= getX() + getWidth()
                && getY() <= y && y <= getY() + getHeight()) {
            if (!mSelected) {
                // only do select when it has not been selected.
                select();
            }
            return true;
        }
        return false;
    }

    public interface OnSelectCallback {
        void onSelected(GLSelectableMenu menu);
    }
}