package com.chatea.voxeleditor;

public class Vector3 {
    float x;
    float y;
    float z;

    public Vector3() {
        this(0, 0, 0);
    }

    public Vector3(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public float[] toFloatArray() {
        return new float[] {this.x, this.y, this.z};
    }
}
