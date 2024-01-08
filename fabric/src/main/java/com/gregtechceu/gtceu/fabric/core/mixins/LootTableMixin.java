package com.gregtechceu.gtceu.fabric.core.mixins;

import com.gregtechceu.gtceu.common.item.tool.ToolEventHandlers;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LootTable.class)
public class LootTableMixin {

    @Inject(method = "getRandomItems(Lnet/minecraft/world/level/storage/loot/LootContext;)Lit/unimi/dsi/fastutil/objects/ObjectArrayList;", at = @At("RETURN"), cancellable = true)
    private void gtceu$modifyBlockLoot(LootContext context, CallbackInfoReturnable<ObjectArrayList<ItemStack>> cir) {
        if (context.getParamOrNull(LootContextParams.THIS_ENTITY) instanceof Player player && context.hasParam(LootContextParams.TOOL) && context.hasParam(LootContextParams.BLOCK_STATE)) {
            Vec3 pos = context.getParam(LootContextParams.ORIGIN);
            BlockPos blockPos = new BlockPos(Mth.floor(pos.x), Mth.floor(pos.y), Mth.floor(pos.z));
            ItemStack tool = context.getParam(LootContextParams.TOOL);
            boolean isSilktouch = EnchantmentHelper.hasSilkTouch(tool);
            int fortuneLevel = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.BLOCK_FORTUNE, tool);
            cir.setReturnValue(ToolEventHandlers.onHarvestDrops(player, tool, context.getLevel(), blockPos, context.getParam(LootContextParams.BLOCK_STATE), isSilktouch, fortuneLevel, cir.getReturnValue(), 1));
        }
    }
}
