package com.gregtechceu.gtceu.data.lang;

import com.gregtechceu.gtceu.api.registry.registrate.provider.GTLangProvider;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;

public class LangHandler {

    /**
     * MC language formatting is moving towards using {@link MutableComponent#withStyle(Style)} instead of § codes, so {@link MutableComponent#withStyle(Style)} should be used where possible.<br>
     * Unformatted text must be in light gray - §7
     * Items must be in gold - §6
     * Fluids must be in blue - §9
     * Directions must be in yellow - §e
     * Disabled/Inactive/Errors must be in red - §c
     * Enabled/Active must be in green - §a
     * Potion effects must be in yellow - §e
     * Time must be in red - §c
     * Percentages must be in green - §a
     * Keys must be in all caps
     * Key combos must follow the format KEY1 + KEY2 (for example, SHIFT + R-CLICK)
     */

    public static void init(GTLangProvider provider) {

        CommonLang.init(provider);
        AdvancementLang.init(provider);
        ArmorLang.init(provider);
        BlockLang.init(provider);
        CommandLang.init(provider);
        ConfigurationLang.init(provider);
        CoverLang.init(provider); // TODO
        GUILang.init(provider); // TODO
        HazardLang.init(provider);
        IntegrationLang.init(provider);
        ItemLang.init(provider);
        MultiblockLang.init(provider);
        MachineLang.init(provider); // TODO
        MaterialLang.init(provider); // TODO
        RecipeLang.init(provider); // TODO
        SubtitleLang.init(provider);
        ToolLang.init(provider); // TODO
    }
}
