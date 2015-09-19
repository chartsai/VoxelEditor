package com.chatea.voxeleditor;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;

import java.util.Set;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class EditorRenderer implements GLSurfaceView.Renderer {

    private RenderController mController;

    public EditorRenderer(RenderController controller) {
        mController = controller;
    }

    /**
     * Tool used loader.<br>
     * The shader should be load in functions of GLSurfaceView.Renderer.<br>
     * (It's because the shader only can be created with GLContext thread)
     *
     * @param type GLES20.GL_VERTEX_SHADER, GLES20.GL_FRAGMENT_SHADER
     * @param shaderCode
     * @return
     */
    public static int loadShader(int type, String shaderCode){
        int shader = GLES20.glCreateShader(type);

        // add the source code to the shader and compile it
        GLES20.glShaderSource(shader, shaderCode);
        GLES20.glCompileShader(shader);

        return shader;
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        GLES20.glClearColor(0.0f, 0.0f, 0.3f, 1.0f);

        // setup back cull function.
        GLES20.glEnable(GLES20.GL_CULL_FACE);
        GLES20.glDisable(GLES20.GL_CULL_FACE);
        GLES20.glFrontFace(GLES20.GL_CCW);
        GLES20.glCullFace(GLES20.GL_BACK);

        // setup depth test function. We need this since we use shader.
        GLES20.glEnable(GLES20.GL_DEPTH_TEST);
        GLES20.glClearDepthf(1.0f);
        GLES20.glDepthFunc(GLES20.GL_LEQUAL);
        GLES20.glDepthMask(true);

        createShaders();

        /**
         * Important!!!
         * onSurfaceCreated is run in GL Thread, so any object with shader should
         * load shader in functions of GLSurfaceView.Renderer.
         *
         * Thus, add a callback function to create renderable objects in EditorCor.
         */
        mController.createRenderObject();
    }

    private void createShaders() {

    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        GLES20.glViewport(0, 0, width, height);
        GLViewPort viewPort = new GLViewPort(0, 0, width, height);

        mController.setViewPort(viewPort);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT);

        float[] vpMatrix = new float[16];
        float[] projectionMatrix = mController.getProjectionMatrix();
        float[] viewMatrix = mController.getViewMatrix();

        Matrix.multiplyMM(vpMatrix, 0, projectionMatrix, 0, viewMatrix, 0);

        for (IRenderable renderable: mController.getRenderableObjects()) {
            renderable.draw(vpMatrix);
        }
    }

    interface RenderController {
        void createRenderObject();

        void setViewPort(GLViewPort viewPort);

        float[] getProjectionMatrix();
        float[] getViewMatrix();

        Set<IRenderable> getRenderableObjects();
    }
}
