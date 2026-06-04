package com.gregtechceu.gtceu.common.pipelike.duct;

import com.gregtechceu.gtceu.api.block.PipeBlock;
import com.gregtechceu.gtceu.api.pipenet.LevelPipeNet;

import com.gregtechceu.gtceu.common.pipelike.GTPipeNetworks;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockGetter;

import org.jetbrains.annotations.Nullable;

import java.util.List;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class DuctPipeBlock extends PipeBlock<DuctPipeVariant, DuctPipeProperties> {

    private static final String DATA_ID = "gtceu_duct_pipe_net";

    private final DuctPipeProperties properties;

    public DuctPipeBlock(Properties properties, DuctPipeVariant type) {
        super(properties, type, GTPipeNetworks.DUCT);
        this.properties = new DuctPipeProperties(type.getRateMultiplier());
    }

    @Override
    public LevelPipeNet<DuctPipeProperties, DuctPipeNet> getWorldPipeNet(ServerLevel world) {
        return world.getDataStorage().computeIfAbsent(tag -> new LevelPipeNet<>(world, DuctPipeNet::new),
                () -> new LevelPipeNet<>(world, DuctPipeNet::new), DATA_ID);
    }

    @Override
    public DuctPipeProperties createRawData() {
        return properties;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable BlockGetter level, List<Component> tooltip,
                                TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        tooltip.add(Component.translatable("gtceu.duct_pipe.transfer_rate",
                this.pipeType.modifyProperties(this.properties).getTransferRate()));
    }
}
