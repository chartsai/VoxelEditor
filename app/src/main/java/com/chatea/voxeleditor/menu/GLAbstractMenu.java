package com.chatea.voxeleditor.menu;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import android.opengl.Matrix;

import com.chatea.voxeleditor.EditorRenderer;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

public abstract class GLAbstractMenu implements GLMenu {

    private static final String vertexShaderCode =
            "uniform mat4 uMVPMatrix;" +
            "attribute vec4 vPosition;" +
            "attribute vec2 vTexureCoord;" +
            "varying vec2 aTexureCoord;" +
            "void main() {" +
            "  gl_Position = uMVPMatrix * vPosition;" +
            "  aTexureCoord = vTexureCoord;" +
            "}";

    /**
     * Note:
     * We cannot declare attribute in fragment shader,
     * so declare vTextureCoord in vertex shader and
     * use a varying to pass to fragment shader.
     */
    private static final String fragmentShaderCode =
            "precision mediump float;" +
            "varying vec2 aTexureCoord;" +
            "uniform sampler2D sTexture;" +
            "uniform float uAlpha;" +
            "void main() {" +
            "  vec4 tex = texture2D(sTexture, aTexureCoord);" +
            "  gl_FragColor = tex * uAlpha;" +
            "}";



    private FloatBuffer vertexBuffer;
    private ShortBuffer indexBuffer;
    private FloatBuffer uvBuffer;
    private int mProgram;

    private float mX;
    private float mY;
    private float mWidth;
    private float mHeight;

    private int[] mTextureId;

    public GLAbstractMenu(Context context, int resourceId, float x, float y, float width, float height) {
        Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), resourceId);
        if (bitmap != null) {
            setup(bitmap, width, height);
            bitmap.recycle();
        }

        mX = x;
        mY = y;
        mWidth = width;
        mHeight = height;
    }

    private void setup(Bitmap bitmap, float width, float height) {
        setupShader();
        setupSquare(width, height);
        setupImage(bitmap);
    }

    private void setupShader() {
        int vertexShader = EditorRenderer.loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode);
        int fragmentShader = EditorRenderer.loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode);

        mProgram = GLES20.glCreateProgram();

        GLES20.glAttachShader(mProgram, vertexShader);
        GLES20.glAttachShader(mProgram, fragmentShader);

        GLES20.glLinkProgram(mProgram);
    }

    private void setupSquare(float width, float height) {
        // We have create the vertices of our view.
        float[] vertices = {
                0.0f, 0.0f, 0.0f, // top-left
                0.0f, -height, 0.0f, // bottom-left
                width, -height, 0.0f, // bottom-right
                width, 0.0f, 0.0f, // top-right
        };

        short[] indices = {0, 1, 2, 0, 2, 3};

        // The vertex buffer.
        ByteBuffer bb = ByteBuffer.allocateDirect(vertices.length * 4);
        bb.order(ByteOrder.nativeOrder());
        vertexBuffer = bb.asFloatBuffer();
        vertexBuffer.put(vertices);
        vertexBuffer.position(0);

        // initialize byte buffer for the draw list
        ByteBuffer dlb = ByteBuffer.allocateDirect(indices.length * 2);
        dlb.order(ByteOrder.nativeOrder());
        indexBuffer = dlb.asShortBuffer();
        indexBuffer.put(indices);
        indexBuffer.position(0);
    }

    private void setupImage(Bitmap bitmap) {
        // Create our UV coordinates.
        float[] uvs = {
                0.0f, 0.0f, // bottom-left
                0.0f, 1.0f, // top-left
                1.0f, 1.0f, // top-right
                1.0f, 0.0f // bottom-right
        };

        // The texture buffer
        ByteBuffer bb = ByteBuffer.allocateDirect(uvs.length * 4);
        bb.order(ByteOrder.nativeOrder());
        uvBuffer = bb.asFloatBuffer();
        uvBuffer.put(uvs);
        uvBuffer.position(0);

        mTextureId = new int[1];
        GLES20.glGenTextures(mTextureId.length, mTextureId, 0);

        // Bind texture to texturename
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureId[0]);

        // Set filtering
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);

        // Set wrapping mode
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);

        // Load the bitmap into the bound texture.
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);
    }

    public float getX() {
        return mX;
    }

    public float getY() {
        return mY;
    }

    public float getWidth() {
        return mWidth;
    }

    public float getHeight() {
        return mHeight;
    }

    public void draw(float[] vpMatrix, float alpha) {

        float[] mvpMatrix = new float[16];

        float[] moduleMatrix = new float[16];
        Matrix.setIdentityM(moduleMatrix, 0);
        Matrix.translateM(moduleMatrix, 0, mX, mY, 0);

        Matrix.multiplyMM(mvpMatrix, 0, vpMatrix, 0, moduleMatrix, 0);

        GLES20.glUseProgram(mProgram);

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureId[0]);

        // get handle to vertex shader's vPosition member
        int mPositionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition");

        // Enable generic vertex attribute array
        GLES20.glEnableVertexAttribArray(mPositionHandle);

        // Prepare the triangle coordinate data
        GLES20.glVertexAttribPointer(mPositionHandle, 3, GLES20.GL_FLOAT, false, 0, vertexBuffer);

        // Get handle to texture coordinates location
        int mTexCoordLoc = GLES20.glGetAttribLocation(mProgram, "vTexureCoord");

        // Enable generic vertex attribute array
        GLES20.glEnableVertexAttribArray(mTexCoordLoc);

        // Prepare the texturecoordinates
        GLES20.glVertexAttribPointer(mTexCoordLoc, 2, GLES20.GL_FLOAT, false, 0, uvBuffer);

        // Get handle to shape's transformation matrix
        int mtrxhandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix");

        // Apply the projection and view transformation
        GLES20.glUniformMatrix4fv(mtrxhandle, 1, false, mvpMatrix, 0);

        // Get handle to textures locations
        int mSamplerLoc = GLES20.glGetUniformLocation(mProgram, "sTexture");

        // Set the sampler texture unit to 0, where we have saved the texture.
        GLES20.glUniform1i(mSamplerLoc, 0);

        // get handle to fragment shader's vColor member
        int mAlphaHandle = GLES20.glGetUniformLocation(mProgram, "uAlpha");

        // Set color for drawing the triangle
        GLES20.glUniform1f(mAlphaHandle, alpha);

        // Draw the triangle
        GLES20.glDrawElements(GLES20.GL_TRIANGLES, 3 * 2, GLES20.GL_UNSIGNED_SHORT, indexBuffer);

        // Disable vertex array
        GLES20.glDisableVertexAttribArray(mPositionHandle);
        GLES20.glDisableVertexAttribArray(mTexCoordLoc);

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
        GLES20.glUseProgram(0);
    }
}
