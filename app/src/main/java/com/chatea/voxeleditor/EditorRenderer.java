package com.chatea.voxeleditor;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;

import com.chatea.voxeleditor.utils.GLViewPort;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class EditorRenderer implements GLSurfaceView.Renderer {

    private RenderDataMaintainer mDataMaintainer;

    public EditorRenderer(RenderDataMaintainer controller) {
        mDataMaintainer = controller;
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

        GLES20.glEnable(GLES20.GL_BLEND);
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);

        /**
         * Important!!!
         * onSurfaceCreated is run in GL Thread, so any object with shader should
         * load shader in functions of GLSurfaceView.Renderer.
         *
         * Thus, add a callback function to create renderable objects in EditorCor.
         */
        mDataMaintainer.createRenderObject();
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        GLES20.glViewport(0, 0, width, height);
        GLViewPort viewPort = new GLViewPort(0, 0, width, height);

        mDataMaintainer.setViewPort(viewPort);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT);

        mDataMaintainer.drawPanel();

        // disable depth test when draw the menu. (menu should always be the top)
        GLES20.glDisable(GLES20.GL_DEPTH_TEST);
        mDataMaintainer.drawMenu();
        GLES20.glEnable(GLES20.GL_DEPTH_TEST);
    }

    interface RenderDataMaintainer {
        void createRenderObject();

        void setViewPort(GLViewPort viewPort);

        void drawPanel();
        void drawMenu();
    }
}
