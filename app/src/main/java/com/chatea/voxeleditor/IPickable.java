package com.chatea.voxeleditor;

import androidx.annotation.Nullable;

public interface IPickable {
    /**
     * check if the object is picked.
     * if pickPoint != null and result is true, the pickPoint will
     * save the picked point.
     *
     * @param eye
     * @param pickRay
     * @param pickedPoint
     * @return
     */
    boolean isPicked(float[] eye, float[] pickRay, @Nullable float[] pickedPoint);
}
