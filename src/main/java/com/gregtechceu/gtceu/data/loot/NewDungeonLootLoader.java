package com.gregtechceu.gtceu.data.loot;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.common.loot.condition.GTConfigValueCondition;
import net.minecraft.data.PackOutput;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraftforge.common.data.GlobalLootModifierProvider;
import org.apache.commons.lang3.ArrayUtils;

public class NewDungeonLootLoader extends GlobalLootModifierProvider {

    public NewDungeonLootLoader(PackOutput output) {
        super(output, GTCEu.MOD_ID);
    }

    @Override
    protected void start() {
        final LootItemCondition[] NORMAL_CONDITION = { new GTConfigValueCondition("worldgen.addLoot") };
        final LootItemCondition[] INCREASED_LOOT_CONDITION = ArrayUtils.add(NORMAL_CONDITION, new GTConfigValueCondition("worldgen.increaseDungeonLoot"));
    }
}
