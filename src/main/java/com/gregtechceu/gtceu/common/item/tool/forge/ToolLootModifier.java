package com.gregtechceu.gtceu.common.item.tool.forge;

import com.gregtechceu.gtceu.common.item.tool.ToolEventHandlers;
import com.mojang.serialization.Codec;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import org.jetbrains.annotations.NotNull;

public class ToolLootModifier implements IGlobalLootModifier {
    public static final ToolLootModifier INSTANCE = new ToolLootModifier();
    public static final Codec<ToolLootModifier> CODEC = Codec.unit(() -> INSTANCE);

    private ToolLootModifier() {/**/}

    @Override
    public @NotNull ObjectArrayList<ItemStack> apply(ObjectArrayList<ItemStack> objectArrayList, LootContext context) {
        if (context.getParamOrNull(LootContextParams.THIS_ENTITY) instanceof Player player && context.hasParam(LootContextParams.TOOL) && context.hasParam(LootContextParams.BLOCK_STATE)) {
            Vec3 pos = context.getParam(LootContextParams.ORIGIN);
            BlockPos blockPos = new BlockPos(Mth.floor(pos.x), Mth.floor(pos.y), Mth.floor(pos.z));
            ItemStack tool = context.getParam(LootContextParams.TOOL);
            boolean isSilktouch = EnchantmentHelper.hasSilkTouch(tool);
            int fortuneLevel = tool.getEnchantmentLevel(Enchantments.BLOCK_FORTUNE);
            return ToolEventHandlers.onHarvestDrops(player, tool, context.getLevel(), blockPos, context.getParam(LootContextParams.BLOCK_STATE), isSilktouch, fortuneLevel, objectArrayList, 1);
        } else {
            return objectArrayList;
        }
    }

    @Override
    public Codec<ToolLootModifier> codec() {
        return CODEC;
    }
}
