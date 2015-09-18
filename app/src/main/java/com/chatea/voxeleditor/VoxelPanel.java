package com.chatea.voxeleditor;

import java.util.ArrayList;
import java.util.List;

public class VoxelPanel {

    public int mXSize;
    public int mYSize;
    public int mZSize;

    public List<GLCube> mCubes;

    public VoxelPanel(int xSize, int ySize, int zSize) {
        mXSize = xSize;
        mYSize = ySize;
        mZSize = zSize;

        mCubes = new ArrayList<>(xSize * ySize * zSize);

        GLCube cube = new GLCube();
        cube.setCenter(0, 0, 0);
        cube.setColor(1.0f, 1.0f, 1.0f, 1.0f);

        mCubes.add(cube);
    }

    public void draw(float[] mvpMatrix) {

        // TODO draw coordinate lines.

        for (GLCube cube: mCubes) {
            cube.draw(mvpMatrix);
        }
    }

    public void pick() {

    }
}
