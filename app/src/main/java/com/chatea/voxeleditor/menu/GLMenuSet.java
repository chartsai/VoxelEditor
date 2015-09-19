package com.chatea.voxeleditor.menu;

import java.util.HashSet;
import java.util.Set;

public class GLMenuSet implements GLMenu {

    private Set<GLMenu> mMenus;

    private OnChildPickedCallback mCallback = null;

    public GLMenuSet() {
        mMenus = new HashSet<>();
    }

    public GLMenuSet(int capicity) {
        mMenus = new HashSet<>(capicity);
    }

    public void addChild(GLMenu menu) {
        mMenus.add(menu);
    }

    public void removechild(GLMenu menu) {
        mMenus.remove(menu);
    }

    public void setClickCallback(OnChildPickedCallback callback) {
        mCallback = callback;
    }

    @Override
    public boolean tryToPick(float x, float y) {
        for (GLMenu menu: mMenus) {
            boolean childPicked = menu.tryToPick(x, y);
            if (childPicked) {
                if (mCallback != null) {
                    mCallback.onChildPicked(this, menu);
                }

                // No need to determine other children.
                return true;
            }
        }
        return false;
    }

    public interface OnChildPickedCallback {
        void onChildPicked(GLMenuSet menu, GLMenu pickedChild);
    }
}
