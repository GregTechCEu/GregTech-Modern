package com.gregtechceu.gtceu.common.item.tool;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemInstance;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.loot.LootModifier;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.jetbrains.annotations.NotNull;

public final class ToolLootModifier extends LootModifier {

    public static final MapCodec<ToolLootModifier> CODEC = RecordCodecBuilder
            .mapCodec(inst -> codecStart(inst).apply(inst, ToolLootModifier::new));

    public ToolLootModifier(LootItemCondition[] conditions, int priority) {
        super(conditions, priority);
    }

    @Override
    protected @NotNull ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot,
                                                          LootContext context) {
        if (!(context.getOptionalParameter(LootContextParams.THIS_ENTITY) instanceof Player player)) {
            return generatedLoot;
        }
        if (!context.hasParameter(LootContextParams.TOOL) ||
                !context.hasParameter(LootContextParams.BLOCK_STATE)) {
            return generatedLoot;
        }
        Vec3 pos = context.getParameter(LootContextParams.ORIGIN);
        BlockPos blockPos = new BlockPos(Mth.floor(pos.x), Mth.floor(pos.y), Mth.floor(pos.z));
        ItemInstance toolInstance = context.getParameter(LootContextParams.TOOL);
        if (!(toolInstance instanceof ItemStack tool)) {
            return generatedLoot;
        }
        ServerLevel level = context.getLevel();
        HolderLookup.RegistryLookup<Enchantment> registry = level.registryAccess()
                .lookupOrThrow(Registries.ENCHANTMENT);
        boolean isSilkTouch = EnchantmentHelper.getItemEnchantmentLevel(
                registry.getOrThrow(Enchantments.SILK_TOUCH), tool) > 0;
        int fortuneLevel = EnchantmentHelper.getItemEnchantmentLevel(
                registry.getOrThrow(Enchantments.FORTUNE), tool);
        return new ObjectArrayList<>(ToolEventHandlers.onHarvestDrops(player, tool, level, blockPos,
                context.getParameter(LootContextParams.BLOCK_STATE), isSilkTouch, fortuneLevel,
                generatedLoot, 1.0f));
    }

    @Override
    public MapCodec<ToolLootModifier> codec() {
        return CODEC;
    }
}
