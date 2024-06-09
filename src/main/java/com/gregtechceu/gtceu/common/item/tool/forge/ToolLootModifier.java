package com.gregtechceu.gtceu.common.item.tool.forge;

import com.gregtechceu.gtceu.common.item.tool.ToolEventHandlers;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;

import com.mojang.serialization.MapCodec;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.jetbrains.annotations.NotNull;

public class ToolLootModifier implements IGlobalLootModifier {

    public static final ToolLootModifier INSTANCE = new ToolLootModifier();
    public static final MapCodec<ToolLootModifier> CODEC = MapCodec.unit(INSTANCE);

    private ToolLootModifier() {/**/}

    @Override
    public @NotNull ObjectArrayList<ItemStack> apply(ObjectArrayList<ItemStack> objectArrayList, LootContext context) {
        if (context.getParamOrNull(LootContextParams.THIS_ENTITY) instanceof Player player &&
                context.hasParam(LootContextParams.TOOL) && context.hasParam(LootContextParams.BLOCK_STATE)) {
            Vec3 pos = context.getParam(LootContextParams.ORIGIN);
            BlockPos blockPos = new BlockPos(Mth.floor(pos.x), Mth.floor(pos.y), Mth.floor(pos.z));
            ItemStack tool = context.getParam(LootContextParams.TOOL);
            Registry<Enchantment> registry = context.getLevel().registryAccess().registryOrThrow(Registries.ENCHANTMENT);
            boolean isSilktouch = tool.getEnchantmentLevel(registry.getHolderOrThrow(Enchantments.SILK_TOUCH)) > 0;
            int fortuneLevel = tool.getEnchantmentLevel(registry.getHolderOrThrow(Enchantments.FORTUNE));
            return ToolEventHandlers.onHarvestDrops(player, tool, context.getLevel(), blockPos,
                    context.getParam(LootContextParams.BLOCK_STATE), isSilktouch, fortuneLevel, objectArrayList, 1);
        } else {
            return objectArrayList;
        }
    }

    @Override
    public MapCodec<ToolLootModifier> codec() {
        return CODEC;
    }
}
