package com.gregtechceu.gtceu.api.capability;

import com.gregtechceu.gtceu.api.item.tool.GTToolType;
import com.mojang.datafixers.util.Pair;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import java.util.Set;

/**
 * @author KilaBash
 * @date 2023/2/25
 * @implNote IToolable
 */
public interface IToolable {

    /**
     * Called when a player clicks this meta tile entity with a tool
     *
     * @return SUCCESS / CONSUME (will damage tool) / FAIL if something happened, so tools will get damaged and animations will be played
     */
    Pair<@Nullable GTToolType, InteractionResult> onToolClick(@Nonnull Set<GTToolType> toolTypes, ItemStack itemStack, UseOnContext context);

}
