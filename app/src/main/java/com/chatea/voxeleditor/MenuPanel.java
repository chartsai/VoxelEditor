package com.chatea.voxeleditor;

import android.content.Context;

import com.chatea.voxeleditor.menu.GLAbstractMenu;
import com.chatea.voxeleditor.menu.GLMenu;
import com.chatea.voxeleditor.menu.GLSelectableMenu;

import java.util.HashSet;
import java.util.Set;

public class MenuPanel implements IRenderable {

    public static final float MENU_PANEL_WIDTH = 1080f;
    public static final float MENU_PANEL_HEIGHT = 1920f;

    /**
     * pick interval, unit is millisecond.
     */
    private static final long PICK_INTERVAL = 330;

    private Set<GLAbstractMenu> mMenus;

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

        float width = MENU_PANEL_WIDTH * 0.2f;
        float margin = width * 0.05f;
        float innerWidth = width - 2 * margin;

        final GLSelectableMenu addVoxel = new GLSelectableMenu(context, R.drawable.add_voxel,
                MENU_PANEL_WIDTH - 2 * width + margin, 2 * width - margin, innerWidth, innerWidth);
        final GLSelectableMenu dragPanel = new GLSelectableMenu(context, R.drawable.drag_panel,
                MENU_PANEL_WIDTH - width + margin, 2 * width - margin, innerWidth, innerWidth);
        final GLSelectableMenu movePanel = new GLSelectableMenu(context, R.drawable.move_panel,
                MENU_PANEL_WIDTH - 2 * width + margin, width - margin, innerWidth, innerWidth);
        final GLSelectableMenu breakVoxel = new GLSelectableMenu(context, R.drawable.break_voxel,
                MENU_PANEL_WIDTH - width + margin, width - margin, innerWidth, innerWidth);

        addVoxel.setOnSelectCallback(new GLSelectableMenu.OnSelectCallback() {
            @Override
            public void onSelected(GLSelectableMenu menu) {
                mCore.setMode(EditorCore.Mode.AddBlock);

                dragPanel.setSelection(false);
                movePanel.setSelection(false);
                breakVoxel.setSelection(false);
            }
        });

        dragPanel.setOnSelectCallback(new GLSelectableMenu.OnSelectCallback() {
            @Override
            public void onSelected(GLSelectableMenu menu) {
                mCore.setMode(EditorCore.Mode.Rotate);

                addVoxel.setSelection(false);
                movePanel.setSelection(false);
                breakVoxel.setSelection(false);
            }
        });

        movePanel.setOnSelectCallback(new GLSelectableMenu.OnSelectCallback() {
            @Override
            public void onSelected(GLSelectableMenu menu) {
                mCore.setMode(EditorCore.Mode.Move);

                addVoxel.setSelection(false);
                dragPanel.setSelection(false);
                breakVoxel.setSelection(false);
            }
        });

        breakVoxel.setOnSelectCallback(new GLSelectableMenu.OnSelectCallback() {
            @Override
            public void onSelected(GLSelectableMenu menu) {
                mCore.setMode(EditorCore.Mode.BreakBlock);

                addVoxel.setSelection(false);
                dragPanel.setSelection(false);
                movePanel.setSelection(false);
            }
        });

        mMenus.add(addVoxel);
        mMenus.add(dragPanel);
        mMenus.add(movePanel);
        mMenus.add(breakVoxel);
    }

    @Override
    public void draw(float[] vpMatrix) {
        for (GLAbstractMenu menu: mMenus) {
            menu.draw(vpMatrix, 1.0f);
        }
    }

    public boolean pick(float x, float y) {
        for (GLMenu menu: mMenus) {
            if (menu.tryToPick(x, y)) {
                // the pick event is been handled.
                return true;
            }
        }
        return false;
    }
}
