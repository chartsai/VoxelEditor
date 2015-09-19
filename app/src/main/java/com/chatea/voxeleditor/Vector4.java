package com.chatea.voxeleditor;

public class Vector4 {
    float x;
    float y;
    float z;
    float w;

    public Vector4() {
        this(0, 0, 0, 0);
    }

    public Vector4(float x, float y, float z, float w) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.w = w;
    }

    public float[] toFloatArray() {
        return new float[] {this.x, this.y, this.z, this.w};
    }
}
