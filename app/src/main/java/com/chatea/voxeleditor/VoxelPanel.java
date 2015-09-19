package com.chatea.voxeleditor;

import android.util.Log;

import com.chatea.voxeleditor.shader.GLCubeShader;
import com.chatea.voxeleditor.shader.GLLineShader;
import com.chatea.voxeleditor.widget.Cube;
import com.chatea.voxeleditor.widget.Line;

import java.util.HashSet;
import java.util.Set;

public class VoxelPanel implements IRenderable {

    /**
     * pick interval, unit is millisecond.
     */
    private static final long PICK_INTERVAL = 330;

    public int mXSize;
    public int mYSize;
    public int mZSize;

    private GLCubeShader mCubeShader;
    private Set<Cube> mCubes;

    private GLLineShader mLineShader;
    private Set<Line> mLines;

    private long mLastPickTime = 0;

    public VoxelPanel(int xSize, int ySize, int zSize) {
        mXSize = xSize;
        mYSize = ySize;
        mZSize = zSize;

        mCubes = new HashSet<>(xSize * ySize * zSize);

        // Panel should has a started Cube at the center.
        Cube cube = new Cube();
        cube.setCenter(0, 0, 0);
        cube.setColor(1.0f, 1.0f, 1.0f, 1.0f);
        mCubes.add(cube);

        mCubeShader = new GLCubeShader();
        mLineShader = new GLLineShader();
    }

    @Override
    public void draw(float[] vpMatrix) {

        // TODO draw coordinate lines.

        for (Cube cube: mCubes) {
            cube.draw(vpMatrix, mCubeShader);
        }

        for (Line line: mLines) {
            line.draw(vpMatrix, mLineShader);
        }
    }

    public void pick(float[] eye, float[] ray) {
        if (System.currentTimeMillis() - mLastPickTime < PICK_INTERVAL) {
            return;
        }

        Cube lastPickedCube = null;
        float lastDistanceSquare = Float.MAX_VALUE;

        for (Cube cube: mCubes) {
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
            case Cube.BACK: {
                addCube(pickedX - 1, pickedY, pickedZ);
                return;
            }
            case Cube.FRONT: {
                addCube(pickedX + 1, pickedY, pickedZ);
                return;
            }
            case Cube.LEFT: {
                addCube(pickedX, pickedY - 1, pickedZ);
                return;
            }
            case Cube.RIGHT: {
                addCube(pickedX, pickedY + 1, pickedZ);
                return;
            }
            case Cube.TOP: {
                addCube(pickedX, pickedY, pickedZ + 1);
                return;
            }
            case Cube.BOTTOM: {
                addCube(pickedX, pickedY, pickedZ - 1);
                return;
            }
            default:
                Log.e("TAG", "some error happened when panel pick cube");
        }
    }

    /**
     * The x, y, z *MUST* be integer.
     * @param x
     * @param y
     * @param z
     */
    private void addCube(float x, float y, float z) {
        Log.d("TAG", "add cube at (" + x + "," + y + "," + z + ")");
        Cube cube = new Cube();
        cube.setCenter((int) x, (int) y, (int) z);
        cube.setColor(1.0f, 1.0f, 1.0f, 1.0f);

        mCubes.add(cube);
    }
}
