package com.gregtechceu.gtceu.core.mixins.client;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

@Mixin(Screen.class)
public interface ScreenAccessor {

    @Accessor
    Font getFont();

    @Mutable
    @Accessor
    void setChildren(List<? extends GuiEventListener> children);
}
