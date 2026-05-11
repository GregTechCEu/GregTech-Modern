package com.gregtechceu.gtceu.api.mui;

import com.gregtechceu.gtceu.common.mui.GTGuiTheme;

import brachy.modularui.screen.ModularPanel;
import brachy.modularui.screen.ModularScreen;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

import static com.gregtechceu.gtceu.GTCEu.MOD_ID;

@OnlyIn(Dist.CLIENT)
public class GTGuiScreen extends ModularScreen {

    public GTGuiScreen(@NotNull ModularPanel<?> mainPanel) {
        this(mainPanel, GTGuiTheme.STANDARD);
    }

    public GTGuiScreen(@NotNull ModularPanel<?> mainPanel, GTGuiTheme theme) {
        this(MOD_ID, mainPanel, theme);
    }

    public GTGuiScreen(@NotNull String owner, @NotNull ModularPanel<?> mainPanel, GTGuiTheme theme) {
        this(owner, mainPanel, theme.getId());
    }

    public GTGuiScreen(String owner, ModularPanel<?> mainPanel, String themeId) {
        super(owner, mainPanel);
        useTheme(themeId);
    }
}
