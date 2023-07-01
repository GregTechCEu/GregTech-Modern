package com.gregtechceu.gtceu.api.item;

import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.api.item.tool.GTToolType;
import com.gregtechceu.gtceu.api.item.tool.MaterialToolTier;
import com.gregtechceu.gtceu.api.item.tool.ToolHelper;
import com.gregtechceu.gtceu.client.renderer.item.ToolItemRenderer;
import com.lowdragmc.lowdraglib.client.renderer.IItemRendererProvider;
import com.lowdragmc.lowdraglib.client.renderer.IRenderer;
import dev.architectury.injectables.annotations.ExpectPlatform;
import lombok.Getter;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.color.item.ItemColor;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.DiggerItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;

/**
 * @author KilaBash
 * @date 2023/2/23
 * @implNote GTToolItem
 */
@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class GTToolItem extends DiggerItem implements IItemRendererProvider, IItemUseFirst {

    @Getter
    protected final GTToolType toolType;

    @ExpectPlatform
    public static GTToolItem create(GTToolType toolType, MaterialToolTier tier, Properties properties) {
        throw new AssertionError();
    }

    protected GTToolItem(GTToolType toolType, MaterialToolTier tier, Properties properties) {
        super(toolType.attackDamageModifier, toolType.attackSpeedModifier, tier, toolType.harvestTag, properties);
        this.toolType = toolType;
        ToolItemRenderer.getOrCreate(toolType);
    }

    @Override
    public MaterialToolTier getTier() {
        return (MaterialToolTier) super.getTier();
    }

    @Override
    public boolean hasCraftingRemainingItem() {
        return super.hasCraftingRemainingItem();
    }

    @Environment(EnvType.CLIENT)
    public static ItemColor tintColor() {
        return (itemStack, index) ->{
            if (itemStack.getItem() instanceof GTToolItem item) {
                return switch (index) {
                    case 0 -> {
                        if (item.toolType == GTToolType.CROWBAR) {
                            if (itemStack.hasTag() && itemStack.getTag().contains("tint_color", Tag.TAG_INT)) {
                                yield itemStack.getTag().getInt("tint_color");
                            }
                        }
                        yield -1;
                    }
                    case 1 -> item.getTier().material.getMaterialARGB();
                    default -> -1;
                };
            }
            return -1;
        };
    }

    @Nullable
    @Override
    public IRenderer getRenderer(ItemStack stack) {
        return ToolItemRenderer.getOrCreate(toolType);
    }

    @Override
    public InteractionResult onItemUseFirst(ItemStack itemStack, UseOnContext context) {
        var toolable = GTCapabilityHelper.getToolable(context.getLevel(), context.getClickedPos(), context.getClickedFace());
        if (toolable != null && ToolHelper.canUse(itemStack)) {
            var result = toolable.onToolClick(getToolType(), itemStack, context);
            if (result == InteractionResult.CONSUME && context.getPlayer() instanceof ServerPlayer serverPlayer) {
                ToolHelper.playToolSound(toolType, serverPlayer);

                if (!serverPlayer.isCreative()) {
                    ToolHelper.damageItem(itemStack, context.getLevel().getRandom(), serverPlayer);
                }
            }
            return result;
        }
        return InteractionResult.PASS;
    }

    @Override
    public String getDescriptionId() {
        return toolType.getUnlocalizedName();
    }

    @Override
    public Component getDescription() {
        //MutableComponent mat = Component.translatable(getTier().material.getUnlocalizedName());
        //GTCEu.LOGGER.info(mat.getString());
        //GTCEu.LOGGER.info(Component.translatable(toolType.getUnlocalizedName(), mat).getString());
        return Component.translatable(toolType.getUnlocalizedName(), Component.translatable(getTier().material.getUnlocalizedName()));
    }

    @Override
    public Component getName(ItemStack stack) {
        return this.getDescription();
    }
}
