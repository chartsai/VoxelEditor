package com.chatea.voxeleditor;

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

    private EditorCore mCore;

    private GLCubeShader mCubeShader;
    private Set<Cube> mCubes;

    private GLLineShader mLineShader;
    private Set<Line> mLines;

    private long mLastPickTime = 0;

    public VoxelPanel(EditorCore core, int xSize, int ySize, int zSize) {
        mCore = core;

        mXSize = xSize;
        mYSize = ySize;
        mZSize = zSize;

        mCubes = new HashSet<>(xSize * ySize * zSize);

        // Panel should has a started Cube at the center.
        Cube cube = new Cube();
        cube.setCenter(0, 0, 0);
        cube.setColor(1.0f, 1.0f, 1.0f, 1.0f);
        mCubes.add(cube);

        setLines();

        mCubeShader = new GLCubeShader();
        mLineShader = new GLLineShader();
    }

    private void setLines() {
        mLines = new HashSet<>();
        // TODO draw lines in the world.
    }

    @Override
    public void draw(float[] vpMatrix) {
        for (Cube cube: mCubes) {
            cube.draw(vpMatrix, mCubeShader);
        }

        for (Line line: mLines) {
            line.draw(vpMatrix, mLineShader);
        }
    }

    public void pick(ModeActions actions, float[] eye, float[] ray) {
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
            return;
        }

        mLastPickTime = System.currentTimeMillis();

        actions.handleClickCube(lastPickedCube);
    }

    /**
     * The x, y, z *MUST* be integer.
     * @param x
     * @param y
     * @param z
     */
    public void addCube(float x, float y, float z, float[] color) {
        Cube cube = new Cube();
        cube.setCenter((int) x, (int) y, (int) z);
        cube.setColor(color[0], color[1], color[2], color[3]);

        mCubes.add(cube);
    }

    public void removeCube(Cube cube) {
        if (mCubes.size() == 1) {
            mCore.showToast("You must have at least one voxel!");
            return;
        }
        mCubes.remove(cube);
    }
}
