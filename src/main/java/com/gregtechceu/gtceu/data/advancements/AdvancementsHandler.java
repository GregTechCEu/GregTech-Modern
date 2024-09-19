package com.gregtechceu.gtceu.data.advancements;

import com.tterrag.registrate.providers.RegistrateAdvancementProvider;

public class AdvancementsHandler {

    public static void init(RegistrateAdvancementProvider provider) {
        RootAdvancements.init(provider);
        SteamAdvancements.init(provider);
        LVAdvancements.init(provider);
        MiscAdvancements.init(provider);

    }
}
