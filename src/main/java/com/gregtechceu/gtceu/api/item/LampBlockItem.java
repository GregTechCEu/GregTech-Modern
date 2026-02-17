package com.gregtechceu.gtceu.api.item;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.client.renderer.block.LampItemRenderer;
import com.gregtechceu.gtceu.client.util.ModelUtils;
import com.gregtechceu.gtceu.common.block.LampBlock;

import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import net.minecraftforge.client.model.BakedModelWrapper;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

import javax.annotation.ParametersAreNonnullByDefault;

import static com.gregtechceu.gtceu.common.block.LampBlock.isBloomEnabled;
import static com.gregtechceu.gtceu.common.block.LampBlock.isInverted;
import static com.gregtechceu.gtceu.common.block.LampBlock.isLightEnabled;

@ParametersAreNonnullByDefault
public class LampBlockItem extends BlockItem {

    public LampBlockItem(LampBlock block, Properties properties) {
        super(block, properties);

        if (GTCEu.isClientSide()) {
            ClientCallWrapper.registerEventListener(this);
        }
    }

    @NotNull
    @Override
    public LampBlock getBlock() {
        return (LampBlock) super.getBlock();
    }

    @Nullable
    @Override
    protected BlockState getPlacementState(BlockPlaceContext context) {
        BlockState state = super.getPlacementState(context);
        return getStateFromStack(context.getItemInHand(), state);
    }

    public BlockState getStateFromStack(ItemStack stack, @Nullable BlockState baseState) {
        if (!stack.hasTag() || !stack.is(this)) {
            return baseState;
        }
        var tag = stack.getTag();
        return (baseState != null ? baseState : getBlock().defaultBlockState())
                .setValue(LampBlock.INVERTED, isInverted(tag))
                .setValue(LampBlock.BLOOM, isBloomEnabled(tag))
                .setValue(LampBlock.LIGHT, isLightEnabled(tag));
    }

    public void fillItemCategory(CreativeModeTab category, NonNullList<ItemStack> items) {
        for (int i = 0; i < 8; ++i) {
            items.add(this.getBlock().getStackFromIndex(i));
        }
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {

            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                return LampItemRenderer.getInstance();
            }
        });
    }

    private static class ClientCallWrapper {

        private static void registerEventListener(LampBlockItem item) {
            ModelUtils.registerBakeEventListener(false, event -> {
                ResourceLocation model = BuiltInRegistries.ITEM.getKey(item).withPrefix("item/");
                BakedModel original = event.getModels().get(model);
                if (original == null) {
                    model = new ModelResourceLocation(model, "inventory");
                    original = event.getModels().get(model);
                }
                event.getModels().put(model, new BakedModelWrapper<>(original) {

                    @Override
                    public boolean isCustomRenderer() {
                        return true;
                    }
                });
            });
        }
    }
}
