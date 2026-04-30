package com.gregtechceu.gtceu.common.item;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.client.renderer.block.LampItemRenderer;
import com.gregtechceu.gtceu.client.util.ModelEventHelper;
import com.gregtechceu.gtceu.common.block.LampBlock;

import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import net.minecraftforge.client.model.BakedModelWrapper;

import it.unimi.dsi.fastutil.objects.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.Set;
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

        private static boolean registeredListener = false;
        private static final Set<Item> trackedItems = new ReferenceLinkedOpenHashSet<>();
        private static final Set<ResourceLocation> trackedItemIds = new ObjectLinkedOpenHashSet<>();

        private static void registerEventListener(LampBlockItem toWrap) {
            trackedItems.add(toWrap);

            if (registeredListener) return;
            registeredListener = true;
            ModelEventHelper.registerBakeEventListener(false, (modelLocation, model, unbakedModel, modelBakery) -> {
                if (trackedItemIds.isEmpty()) {
                    for (Item item : trackedItems) {
                        trackedItemIds.add(BuiltInRegistries.ITEM.getKey(item));
                    }
                }

                // handle both cases 1.20 can have passed here. 1.21 *only* has the ModelResourceLocation case.
                ResourceLocation possibleItemId;
                if (modelLocation instanceof ModelResourceLocation modelResLoc &&
                        Objects.equals(modelResLoc.getVariant(), "inventory")) {
                    // unwrap ModelResourceLocations
                    // 1.21 needs different code here as ModelResourceLocation is a wrapper record instead of a subclass
                    possibleItemId = modelResLoc.withPrefix("");
                } else if (modelLocation.getPath().startsWith("item/")) {
                    // remove the "item/" prefix from the model path
                    possibleItemId = modelLocation.withPath(path -> path.substring("item/".length()));
                } else {
                    return model;
                }

                if (trackedItemIds.contains(possibleItemId)) {
                    // if the current model is a lamp item, replace it with one that has isCustomRenderer()==true
                    // so the custom renderer in `LampBlockItem#initializeClient` works
                    return new BakedModelWrapper<>(model) {

                        @Override
                        public boolean isCustomRenderer() {
                            return true;
                        }
                    };
                } else {
                    return model;
                }
            });
        }
    }
}
