package com.chatea.voxeleditor.menu;

import android.content.Context;

public class GLSwitchableMenu extends GLAbstractMenu {

    // default is off
    private boolean mOn = false;

    private OnSwitchCallback mCallback = null;

    public GLSwitchableMenu(Context context, int resourceId, float x, float y, float width, float height) {
        super(context, resourceId, x, y, width, height);
    }

    public void setSwitchCallback(OnSwitchCallback callback) {
        mCallback = callback;
    }

    public void toggle() {
        boolean oldStatus = mOn;
        boolean newStatus = !oldStatus;

        mOn = newStatus;

        triggerCallbacks(newStatus, oldStatus);
    }

    public void switchOn() {
        boolean oldStatus = mOn;
        boolean newStatus = true;

        mOn = newStatus;

        triggerCallbacks(newStatus, oldStatus);
    }

    public void switchOff() {
        boolean oldStatus = mOn;
        boolean newStatus = false;

        mOn = newStatus;

        triggerCallbacks(newStatus, oldStatus);
    }

    private void triggerCallbacks(boolean newStatus, boolean oldStatus) {
        mCallback.onSwitch(this, newStatus, oldStatus);
    }

    @Override
    public boolean tryToPick(float x, float y) {
        // AABB detection.
        if (getX() <= x && x <= getX() + getWidth()
                && getY() <= y && y <= getY() + getHeight()) {
            toggle();
            return true;
        }
        return false;
    }

    public interface OnSwitchCallback {
        void onSwitch(GLSwitchableMenu menu, boolean newStatus, boolean oldStatus);
    }
}
