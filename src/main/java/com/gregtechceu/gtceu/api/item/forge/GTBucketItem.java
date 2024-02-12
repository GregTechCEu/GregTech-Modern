package com.gregtechceu.gtceu.api.item.forge;

import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.HazardProperty;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.PropertyKey;
import com.gregtechceu.gtceu.api.fluids.GTFluid;
import com.gregtechceu.gtceu.common.data.GTDamageTypes;
import com.gregtechceu.gtceu.utils.GTUtil;
import com.lowdragmc.lowdraglib.side.fluid.FluidHelper;
import com.lowdragmc.lowdraglib.side.fluid.FluidStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.fluids.capability.wrappers.FluidBucketWrapper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Supplier;

import static com.gregtechceu.gtceu.api.data.chemical.material.properties.PropertyKey.HAZARD;

/**
 * @author KilaBash
 * @date 2023/3/28
 * @implNote GTBucketItem
 */
public class GTBucketItem extends BucketItem {

    final Material material;
    final String langKey;

    public GTBucketItem(Supplier<? extends Fluid> fluid, Properties properties, Material material, String langKey) {
        super(fluid, properties);
        this.material = material;
        this.langKey = langKey;
    }

    public static int color(ItemStack itemStack, int index) {
        if (itemStack.getItem() instanceof GTBucketItem item) {
            if (index == 1) {
                return FluidHelper.getColor(FluidStack.create(item.getFluid(), FluidHelper.getBucket()));
            }
        }
        return -1;
    }

    public ICapabilityProvider initCapabilities(@Nonnull ItemStack stack, @Nullable CompoundTag nbt) {
        return this.getClass() == GTBucketItem.class ? new FluidBucketWrapper(stack) : super.initCapabilities(stack, nbt);
    }

    @Override
    public String getDescriptionId() {
        return "item.gtceu.bucket";
    }

    @Override
    public Component getDescription() {
        Component materialName = material.getLocalizedName();
        return Component.translatable("item.gtceu.bucket", Component.translatable(this.langKey, materialName));
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltipComponents, TooltipFlag isAdvanced) {
        super.appendHoverText(stack, level, tooltipComponents, isAdvanced);
        if (material.hasProperty(HAZARD)){
            tooltipComponents.add(Component.translatable("gtceu.hazard.description"));
            if (GTUtil.isShiftDown())
                tooltipComponents.add(Component.translatable("gtceu.hazard." + material.getProperty(HAZARD).getHazardType().name().toLowerCase()));
        }
    }

    @Override
    public Component getName(ItemStack stack) {
        return this.getDescription();
    }

    @Override
    public int getBurnTime(ItemStack itemStack, @Nullable RecipeType<?> recipeType) {
        var property = material.getProperty(PropertyKey.FLUID);
        if (property != null) {
            var fluid = material.getFluid();
            if (fluid instanceof GTFluid gtFluid) {
                return gtFluid.getBurnTime();
            }
        }
        return -1;
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        super.inventoryTick(stack, level, entity, slotId, isSelected);
        if(entity instanceof LivingEntity livingEntity && livingEntity.tickCount % 20 == 0) {
            if (!material.hasProperty(HAZARD)) return;
            //TODO protective equipment


            HazardProperty poisonProperty = material.getProperty(HAZARD);

            if (poisonProperty.getDamage() != null && livingEntity.tickCount % (20 * poisonProperty.getDamage().delay()) == 0)
                livingEntity.hurt(GTDamageTypes.CHEMICAL.source(level), poisonProperty.getDamage().damage());

            if (poisonProperty.getEffect() != null)
                poisonProperty.getEffect().apply(livingEntity);

        }
    }
}
