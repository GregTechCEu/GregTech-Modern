package com.gregtechceu.gtceu.api.capability;

import com.gregtechceu.gtceu.api.item.tool.GTToolType;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;

import javax.annotation.Nonnull;

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
    InteractionResult onToolClick(@Nonnull GTToolType toolType, ItemStack itemStack, UseOnContext context);

}
