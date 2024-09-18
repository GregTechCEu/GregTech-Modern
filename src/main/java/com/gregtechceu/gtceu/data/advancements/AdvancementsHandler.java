package com.gregtechceu.gtceu.data.advancements;

import com.tterrag.registrate.providers.RegistrateAdvancementProvider;

public class AdvancementsHandler {

    public static void init(RegistrateAdvancementProvider provider) {
        MiscAdvancements.init(provider);
        LVAdvancements.init(provider);
        SteamAdvancements.init(provider);
    }
}
