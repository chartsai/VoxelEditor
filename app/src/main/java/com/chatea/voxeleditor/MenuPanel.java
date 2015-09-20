package com.chatea.voxeleditor;

import android.content.Context;

import com.chatea.voxeleditor.menu.GLMenu;
import com.chatea.voxeleditor.menu.GLPaletteBlockMenu;
import com.chatea.voxeleditor.menu.GLRenderableMenu;
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

    private Set<GLRenderableMenu> mMenus;

    private EditorCore mCore;

    /**
     * MenuPanel need context to get drawable.
     */
    private Context mContext;

    public MenuPanel(EditorCore core, Context context) {
        mCore = core;
        mContext = context;

        mMenus = new HashSet<>();

        setupModeMenus(context);

        setupPlaletteMenus();
    }

    private void setupModeMenus(Context context) {
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

        addVoxel.select();
    }

    private void setupPlaletteMenus() {
        float width = MENU_PANEL_WIDTH * 0.1f;
        float margin = width * 0.1f;
        float yOffset = 2 * margin;
        float innerWidth = width - 2 * margin;

        // row 1
        {
            GLPaletteBlockMenu whiteBlock;
            GLPaletteBlockMenu redBlock;
            GLPaletteBlockMenu greenBlock;
            GLPaletteBlockMenu blueBlock;
            GLPaletteBlockMenu yellowBlock;
            GLPaletteBlockMenu purpleBlock;

            whiteBlock = new GLPaletteBlockMenu(margin, yOffset + width - margin,
                    innerWidth, innerWidth, 1.0f, 1.0f, 1.0f);

            redBlock = new GLPaletteBlockMenu(width + margin, yOffset + width - margin,
                    innerWidth, innerWidth, 1.0f, 0.0f, 0.0f);

            greenBlock = new GLPaletteBlockMenu(2 * width + margin, yOffset + width - margin,
                    innerWidth, innerWidth, 0.0f, 1.0f, 0.0f);

            blueBlock = new GLPaletteBlockMenu(3 * width + margin, yOffset + width - margin,
                    innerWidth, innerWidth, 0.0f, 0.0f, 1.0f);

            yellowBlock = new GLPaletteBlockMenu(4 * width + margin, yOffset + width - margin,
                    innerWidth, innerWidth, 1.0f, 1.0f, 0.0f);

            purpleBlock = new GLPaletteBlockMenu(5 * width + margin, yOffset + width - margin,
                    innerWidth, innerWidth, 1.0f, 0.0f, 1.0f);

            mMenus.add(whiteBlock);
            mMenus.add(redBlock);
            mMenus.add(greenBlock);
            mMenus.add(blueBlock);
            mMenus.add(yellowBlock);
            mMenus.add(purpleBlock);
        }

        {
            // row 2
            GLPaletteBlockMenu grayBlock;
            GLPaletteBlockMenu ligthBlueBlock;
            GLPaletteBlockMenu paletteBlock8;
            GLPaletteBlockMenu paletteBlock9;
            GLPaletteBlockMenu pinkBlock;
            GLPaletteBlockMenu paletteBlock11;

            grayBlock = new GLPaletteBlockMenu(margin, yOffset + 2 * width - margin,
                    innerWidth, innerWidth, 0.5f, 0.5f, 0.5f);

            ligthBlueBlock = new GLPaletteBlockMenu(width + margin, yOffset + 2 * width - margin,
                    innerWidth, innerWidth, 0.0f, 1.0f, 1.0f);

            paletteBlock8 = new GLPaletteBlockMenu(2 * width + margin, yOffset + 2 * width - margin,
                    innerWidth, innerWidth, 1.0f, 0.5f, 0.0f);

            paletteBlock9 = new GLPaletteBlockMenu(3 * width + margin, yOffset + 2 * width - margin,
                    innerWidth, innerWidth, 0.5f, 0.0f, 1.0f);

            pinkBlock = new GLPaletteBlockMenu(4 * width + margin, yOffset + 2 * width - margin,
                    innerWidth, innerWidth, 1.0f, 0.25f, 0.5f);

            paletteBlock11 = new GLPaletteBlockMenu(5 * width + margin, yOffset + 2 * width - margin,
                    innerWidth, innerWidth, 0.5f, 0.25f, 0.25f);

            mMenus.add(grayBlock);
            mMenus.add(ligthBlueBlock);
            mMenus.add(paletteBlock8);
            mMenus.add(paletteBlock9);
            mMenus.add(pinkBlock);
            mMenus.add(paletteBlock11);
        }

        {
            // row 3
            GLPaletteBlockMenu blackBlock;
            GLPaletteBlockMenu paletteBlock13;
            GLPaletteBlockMenu paletteBlock14;
            GLPaletteBlockMenu paletteBlock15;
            GLPaletteBlockMenu paletteBlock16;
            GLPaletteBlockMenu paletteBlock17;

            blackBlock = new GLPaletteBlockMenu(margin, yOffset + 3 * width - margin,
                    innerWidth, innerWidth, 0.0f, 0.0f, 0.0f);

            paletteBlock13 = new GLPaletteBlockMenu(width + margin, yOffset + 3 * width - margin,
                    innerWidth, innerWidth, 0.5f, 1.0f, 1.0f);

            paletteBlock14 = new GLPaletteBlockMenu(2 * width + margin, yOffset + 3 * width - margin,
                    innerWidth, innerWidth, 0.75f, 0.75f, 1.0f);

            paletteBlock15 = new GLPaletteBlockMenu(3 * width + margin, yOffset + 3 * width - margin,
                    innerWidth, innerWidth, 0.0f, 0.5f, 0.75f);

            paletteBlock16 = new GLPaletteBlockMenu(4 * width + margin, yOffset + 3 * width - margin,
                    innerWidth, innerWidth, 0.75f, 0.75f, 0.75f);

            paletteBlock17 = new GLPaletteBlockMenu(5 * width + margin, yOffset + 3 * width - margin,
                    innerWidth, innerWidth, 0.25f, 0.25f, 0.25f);

            mMenus.add(blackBlock);
            mMenus.add(paletteBlock13);
            mMenus.add(paletteBlock14);
            mMenus.add(paletteBlock15);
            mMenus.add(paletteBlock16);
            mMenus.add(paletteBlock17);
        }
    }

    @Override
    public void draw(float[] vpMatrix) {
        for (GLRenderableMenu menu: mMenus) {
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
