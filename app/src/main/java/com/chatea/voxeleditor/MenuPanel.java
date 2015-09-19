package com.chatea.voxeleditor;

import android.content.Context;
import android.util.Log;

import java.util.HashSet;
import java.util.Set;

public class MenuPanel implements IRenderable {

    public static final float MENU_PANEL_WIDTH = 1080f;
    public static final float MENU_PANEL_HEIGHT = 1920f;

    /**
     * pick interval, unit is millisecond.
     */
    private static final long PICK_INTERVAL = 330;

    private Set<GLMenu> mMenus;

    private EditorCore mCore;

    /**
     * MenuPanel need context to get drawable.
     */
    private Context mContext;

    private long mLastPickTime = 0;

    public MenuPanel(EditorCore core, Context context) {
        mCore = core;
        mContext = context;

        mMenus = new HashSet<>();

        float projectWidth = MENU_PANEL_WIDTH;
        float width = projectWidth * 0.2f;
        float margin = width * 0.05f;
        float innerWidth = width - 2 * margin;
        float x = MENU_PANEL_WIDTH - width;
        float y = width;

        GLClickableMenu sampleMenu = new GLClickableMenu(context, R.drawable.test,
                x + margin, y - margin, innerWidth, innerWidth);

        mMenus.add(sampleMenu);
    }

    @Override
    public void draw(float[] vpMatrix) {
        for (GLMenu menu: mMenus) {
            menu.draw(vpMatrix, 1.0f);
        }
    }

    public void pick(float x, float y) {
        // TODO implement this.
    }


    private void selectMenu(GLMenu menu) {
        Log.d("TAG", "======= test menu is clicked");
    }
}
