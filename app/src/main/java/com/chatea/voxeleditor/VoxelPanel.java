package com.chatea.voxeleditor;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class VoxelPanel implements IRenderable {

    /**
     * pick interval, unit is millisecond.
     */
    private static final long PICK_INTERVAL = 330;

    public int mXSize;
    public int mYSize;
    public int mZSize;

    public List<GLCube> mCubes;

    private long mLastPickTime = 0;

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

    @Override
    public void draw(float[] mvpMatrix) {

        // TODO draw coordinate lines.

        for (GLCube cube: mCubes) {
            cube.draw(mvpMatrix);
        }
    }

    public void pick(float[] eye, float[] ray) {
        if (System.currentTimeMillis() - mLastPickTime < PICK_INTERVAL) {
            return;
        }

        GLCube lastPickedCube = null;
        float lastDistanceSquare = Float.MAX_VALUE;

        for (GLCube cube: mCubes) {
            float[] pickedPoint = new float[3];
            if (cube.isPicked(eye, ray, pickedPoint)) {

                float distanceSquare = Utils.getDistanceSquare(eye, pickedPoint);

                if (lastPickedCube == null || distanceSquare < lastDistanceSquare) {
                    lastPickedCube = cube;
                    lastDistanceSquare = distanceSquare;
                }
            }
        }

        if (lastPickedCube == null) {
            Log.d("TAG", "No picked square");
            return;
        }

        mLastPickTime = System.currentTimeMillis();

        float[] pickedCenter = lastPickedCube.getCenter();
        float pickedX = pickedCenter[0];
        float pickedY = pickedCenter[1];
        float pickedZ = pickedCenter[2];

        switch (lastPickedCube.getLastPickedPlane()) {
            case GLCube.BACK: {
                GLCube cube = new GLCube();
                cube.setCenter(pickedX - 1, pickedY, pickedZ);
                mCubes.add(cube);
                return;
            }
            case GLCube.FRONT: {
                GLCube cube = new GLCube();
                cube.setCenter(pickedX + 1, pickedY, pickedZ);
                mCubes.add(cube);
                return;
            }
            case GLCube.LEFT: {
                GLCube cube = new GLCube();
                cube.setCenter(pickedX, pickedY - 1, pickedZ);
                mCubes.add(cube);
                return;
            }
            case GLCube.RIGHT: {
                GLCube cube = new GLCube();
                cube.setCenter(pickedX, pickedY + 1, pickedZ);
                mCubes.add(cube);
                return;
            }
            case GLCube.TOP: {
                GLCube cube = new GLCube();
                cube.setCenter(pickedX, pickedY, pickedZ + 1);
                mCubes.add(cube);
                return;
            }
            case GLCube.BOTTOM: {
                GLCube cube = new GLCube();
                cube.setCenter(pickedX, pickedY, pickedZ - 1);
                mCubes.add(cube);
                return;
            }
            default:
                Log.e("TAG", "some error happened when panel pick cube");
        }
    }
}
