package com.gregtechceu.gtceu.api.machine.feature;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public interface IDataStickConfigurable extends IMachineFeature, IDataStickInteractable {

    @Override
    default InteractionResult onDataStickShiftUse(Player player, ItemStack dataStick) {
        if (!self().isRemote()) {
            CompoundTag root = dataStick.getOrCreateTag();
            CompoundTag config = new CompoundTag();
            writeConfig(config);
            root.put(getConfigKey(), config);
            dataStick.setHoverName(getConfigName());
            player.sendSystemMessage(Component.translatable("gtceu.machine.me.import_copy_settings"));
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    default InteractionResult onDataStickUse(Player player, ItemStack dataStick) {
        String tagKey = getConfigKey();
        CompoundTag root = dataStick.getTag();
        if (root == null || !root.contains(tagKey)) {
            return InteractionResult.PASS;
        }
        if (!self().isRemote()) {
            readConfig(root.getCompound(tagKey));
            player.sendSystemMessage(Component.translatable("gtceu.machine.me.import_paste_settings"));
        }
        return InteractionResult.sidedSuccess(self().isRemote());
    }

    default String getConfigKey() {
        return self().getDefinition().getDescriptionId();
    }

    default Component getConfigName() {
        return Component.translatable(getConfigKey());
    }

    void writeConfig(CompoundTag tag);

    void readConfig(CompoundTag tag);
}
