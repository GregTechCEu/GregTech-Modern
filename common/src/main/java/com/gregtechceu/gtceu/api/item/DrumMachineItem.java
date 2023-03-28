package com.gregtechceu.gtceu.api.item;


import com.gregtechceu.gtceu.api.block.MetaMachineBlock;
import dev.architectury.injectables.annotations.ExpectPlatform;

/**
 * @author KilaBash
 * @date 2023/3/28
 * @implNote DrumMachineItem
 */
public class DrumMachineItem extends MetaMachineItem {
    protected DrumMachineItem(MetaMachineBlock block, Properties properties) {
        super(block, properties);
    }

    @ExpectPlatform
    public static DrumMachineItem create(MetaMachineBlock block, Properties properties) {
        throw new AssertionError();
    }

}
