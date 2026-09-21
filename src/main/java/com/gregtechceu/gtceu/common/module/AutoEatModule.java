package com.gregtechceu.gtceu.common.module;

import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.api.capability.IElectricItem;
import com.gregtechceu.gtceu.api.item.armor.ArmorUtils;
import com.gregtechceu.gtceu.api.item.module.ItemModule;
import com.gregtechceu.gtceu.api.item.module.ModuleContext;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class AutoEatModule extends ItemModule {

    public AutoEatModule(ResourceLocation id) {
        super(id);
    }

    @Override
    public Component getInfo() {
        return Component.translatable(getDescriptionLanguageKey());
    }

    @Override
    public void onArmorTick(ModuleContext moduleContext, LivingEntity entity) {
        super.onArmorTick(moduleContext, entity);
        IElectricItem electricItem = GTCapabilityHelper.getElectricItem(moduleContext.getAppliedTo());
        if (electricItem == null) return;
        supplyFood(electricItem, (Player) entity, 512);
    }

    public static void supplyFood(@NotNull IElectricItem item, Player player, long energyPerUse) {
        if (item.canUse(energyPerUse / 10) && player.getFoodData().needsFood()) {
            int slotId = -1;
            IItemHandler playerInv = player.getCapability(Capabilities.ItemHandler.ENTITY);
            if (playerInv instanceof IItemHandlerModifiable items) {
                for (int i = 0; i < items.getSlots(); i++) {
                    ItemStack current = items.getStackInSlot(i);
                    if (current.getFoodProperties(player) != null) {
                        slotId = i;
                        break;
                    }
                }

                if (slotId > -1) {
                    ItemStack stack = items.getStackInSlot(slotId);
                    InteractionResultHolder<ItemStack> result = ArmorUtils.eat(player, stack);
                    stack = result.getObject();
                    if (stack.isEmpty())
                        items.setStackInSlot(slotId, ItemStack.EMPTY);

                    if (result.getResult() == InteractionResult.SUCCESS)
                        item.discharge(energyPerUse / 10, item.getTier(), true, false, false);

                }
            }
        }
    }
}
