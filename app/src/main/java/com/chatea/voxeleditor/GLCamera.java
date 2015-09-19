package com.chatea.voxeleditor;

public class GLCamera {
    public float eyeX;
    public float eyeY;
    public float eyeZ;

    public float centerX;
    public float centerY;
    public float centerZ;

    public float upX;
    public float upY;
    public float upZ;

    public GLCamera() {
        this(0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f);
    }

    public GLCamera(float eyeX, float eyeY, float eyeZ,
             float centerX, float centerY, float centerZ,
             float upX, float upY, float upZ) {
        this.eyeX = eyeX;
        this.eyeY = eyeY;
        this.eyeZ = eyeZ;

        this.centerX = centerX;
        this.centerY = centerY;
        this.centerZ = centerZ;

        this.upX = upX;
        this.upY = upY;
        this.upZ = upZ;
    }
}
