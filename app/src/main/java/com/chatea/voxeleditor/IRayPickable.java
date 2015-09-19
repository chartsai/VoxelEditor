package com.chatea.voxeleditor;

public interface IRayPickable {
    /**
     * check if the object is picked.
     * if pickPoint != null and result is true, the pickPoint will
     * save the picked point.
     *
     */
    boolean isPicked(RayData ray, RayPickResult result);

    class RayData {
        float eyeX;
        float eyeY;
        float eyeZ;

        float rayX;
        float rayY;
        float rayZ;
    }

    class RayPickResult {
        float pickX;
        float pickY;
        float pickZ;

        IRayPickable pickedObject;
    }
}
