package com.gregtechceu.gtceu.api.gui.compass;

import com.google.common.base.Suppliers;
import com.gregtechceu.gtceu.GTCEu;
import com.lowdragmc.lowdraglib.gui.compass.ICompassUIConfig;
import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.texture.ResourceBorderTexture;
import com.lowdragmc.lowdraglib.gui.texture.ShaderTexture;
import com.mojang.blaze3d.systems.RenderSystem;
import lombok.Getter;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.world.inventory.InventoryMenu;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.opengl.GL13;

import java.util.function.Supplier;

public class GTCompassUIConfig implements ICompassUIConfig {
    @Getter
    private final IGuiTexture listViewBackground = ResourceBorderTexture.BORDERED_BACKGROUND_INVERSE;
    @Getter
    private final IGuiTexture listItemBackground = ResourceBorderTexture.BUTTON_COMMON;
    @Getter
    private final IGuiTexture listItemSelectedBackground = ResourceBorderTexture.BUTTON_COMMON.copy().setColor(0xff337f7f);
    @Getter
    private final IGuiTexture nodeBackground = ResourceBorderTexture.BUTTON_COMMON;
    @Getter
    private final IGuiTexture nodeHoverBackground = ResourceBorderTexture.BUTTON_COMMON.copy().setColor(0xff337f7f);
    private final Supplier<IGuiTexture> sectionBackground = Suppliers.memoize(() -> ShaderTexture.createShader(GTCEu.id("compass_background")).setUniformCache(cache -> {
        // bind uvs
        var atlas = Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS);
        var base = atlas.apply(GTCEu.id("block/casings/voltage/lv/side"));
        var top = atlas.apply(GTCEu.id("block/machines/distillery/overlay_top_active"));
        var front = atlas.apply(GTCEu.id("block/machines/distillery/overlay_front_active"));
        var back = atlas.apply(GTCEu.id("block/overlay/machine/overlay_fluid_output"));
        var side = atlas.apply(GTCEu.id("block/machines/distillery/overlay_side_active"));

        cache.glUniform4F("baseTexture", base.getU0(), base.getV0(), base.getU1(), base.getV1());
        cache.glUniform4F("topTexture", top.getU0(), top.getV0(), top.getU1(), top.getV1());
        cache.glUniform4F("frontTexture", front.getU0(), front.getV0(), front.getU1(), front.getV1());
        cache.glUniform4F("backTexture", back.getU0(), back.getV0(), back.getU1(), back.getV1());
        cache.glUniform4F("sideTexture", side.getU0(), side.getV0(), side.getU1(), side.getV1());


        // bind BLOCK_ATLAS
        RenderSystem.activeTexture(GL13.GL_TEXTURE0);
        RenderSystem.bindTexture(Minecraft.getInstance().getTextureManager().getTexture(InventoryMenu.BLOCK_ATLAS).getId());
        cache.glUniform1I("BLOCK_ATLAS", 0);
    }));

    @NotNull
    @Override
    public IGuiTexture getSectionBackground() {
        return sectionBackground.get();
    }
}
