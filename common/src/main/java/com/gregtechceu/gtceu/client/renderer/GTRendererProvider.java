package com.gregtechceu.gtceu.client.renderer;

import com.lowdragmc.lowdraglib.client.renderer.ATESRRendererProvider;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.level.block.entity.BlockEntity;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 * @author KilaBash
 * @date 2022/11/3
 * @implNote TCRendererProvider
 */
@Environment(EnvType.CLIENT)
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class GTRendererProvider extends ATESRRendererProvider<BlockEntity> {
    private static GTRendererProvider INSTANCE;

    private GTRendererProvider(BlockEntityRendererProvider.Context context) {
//        ModelBellows.INSTANCE = new ModelBellows(context);
//        ModelHungryChest.INSTANCE = new ModelHungryChest(context);
    }

    public static GTRendererProvider getOrCreate(BlockEntityRendererProvider.Context context) {
        if (INSTANCE == null) {
            INSTANCE = new GTRendererProvider(context);
        }
        return INSTANCE;
    }

    @Nullable
    public static GTRendererProvider getInstance() {
        return INSTANCE;
    }
}
