package com.gregtechceu.gtceu.api.item;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.block.MaterialBlock;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.DustProperty;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.PropertyKey;
import com.lowdragmc.lowdraglib.client.renderer.IBlockRendererProvider;
import com.lowdragmc.lowdraglib.client.renderer.IItemRendererProvider;
import com.lowdragmc.lowdraglib.client.renderer.IRenderer;
import dev.architectury.injectables.annotations.ExpectPlatform;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.color.item.ItemColor;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;

/**
 * @author KilaBash
 * @implNote MaterialBlockItem
 */
public class MaterialBlockItem extends BlockItem implements IItemRendererProvider {

    protected MaterialBlockItem(MaterialBlock block, Properties properties) {
        super(block, properties);
    }

    @ExpectPlatform
    public static MaterialBlockItem create(MaterialBlock block, Properties properties) {
        throw new AssertionError();
    }

    public void onRegister() {

    }

    @Override
    @Nonnull
    public MaterialBlock getBlock() {
        return (MaterialBlock)super.getBlock();
    }

    @Environment(EnvType.CLIENT)
    public static ItemColor tintColor() {
        return (itemStack, index) -> {
            if (itemStack.getItem() instanceof MaterialBlockItem materialBlockItem) {
                if (index == 1 && materialBlockItem.getBlock().material.getMaterialSecondaryRGB() != -1) {
                    return materialBlockItem.getBlock().material.getMaterialSecondaryARGB();
                } else {
                    return materialBlockItem.getBlock().material.getMaterialARGB();
                }
            }
            return -1;
        };
    }

    @Nullable
    @Override
    @Environment(EnvType.CLIENT)
    public IRenderer getRenderer(ItemStack stack) {
        if (getBlock() instanceof IBlockRendererProvider provider) {
            return provider.getRenderer(getBlock().defaultBlockState());
        }
        return null;
    }

    @Override
    public String getDescriptionId() {
        return getBlock().getDescriptionId();
    }

    @Override
    public String getDescriptionId(ItemStack stack) {
        return getDescriptionId();
    }

    @Override
    public Component getDescription() {
        return getBlock().getName();
    }

    @Override
    public Component getName(ItemStack stack) {
        return getDescription();
    }

    public int getItemBurnTime() {
        var material = getBlock().material;
        DustProperty property = material == null ? null : material.getProperty(PropertyKey.DUST);
        if (property != null) return (int) (property.getBurnTime() * getBlock().tagPrefix.getMaterialAmount(material) / GTValues.M);
        return -1;
    }
}
