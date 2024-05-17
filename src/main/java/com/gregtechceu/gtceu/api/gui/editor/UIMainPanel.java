package com.gregtechceu.gtceu.api.gui.editor;

import com.gregtechceu.gtceu.api.gui.GuiTextures;

import com.lowdragmc.lowdraglib.gui.editor.ui.Editor;
import com.lowdragmc.lowdraglib.gui.editor.ui.MainPanel;
import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.texture.TextTexture;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;

import net.minecraft.client.gui.GuiGraphics;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

/**
 * @author KilaBash
 * @date 2023/7/5
 * @implNote UIMainPanel
 */
public class UIMainPanel extends MainPanel implements IGuiTexture {

    final String description;

    public UIMainPanel(Editor editor, WidgetGroup root, String description) {
        super(editor, root);
        this.setBackground(this);
        this.description = description;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void draw(GuiGraphics graphics, int mouseX, int mouseY, float x, float y, int width, int height) {
        if (description != null) {
            new TextTexture(description).scale(2.0f).draw(graphics, mouseX, mouseY, x, y,
                    width - editor.getConfigPanel().getSize().getWidth(), height);
        }
        var border = 4;
        var background = GuiTextures.BACKGROUND;
        var position = root.getPosition();
        var size = root.getSize();
        var w = Math.max(size.width + border * 2, 172);
        var h = Math.max(size.height + border * 2, 86);
        background.draw(graphics, mouseX, mouseY,
                position.x - (w - size.width) / 2f,
                position.y - (h - size.height) / 2f,
                w, h);
    }
}
