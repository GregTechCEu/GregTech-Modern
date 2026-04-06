package com.gregtechceu.gtceu.common.datafixer.fixes;

import net.minecraft.util.datafix.fixes.ItemStackComponentRemainderFix;

import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class GTToolComponentFix extends ItemStackComponentRemainderFix {

    public GTToolComponentFix(Schema outputSchema) {
        super(outputSchema, "GTToolComponentFix", "gtceu:gt_tool");
    }

    @Override
    protected <T> @NotNull Dynamic<T> fixComponent(Dynamic<T> tag) {
        tag.remove("tool_speed").remove("attack_damage").remove("harvest_level");

        Optional<Dynamic<T>> lastCraftingUse = tag.get("last_crafting_use").result();
        if (lastCraftingUse.isEmpty()) {
            tag = tag.set("last_crafting_use", tag.createInt(0));
        }

        return tag;
    }
}
