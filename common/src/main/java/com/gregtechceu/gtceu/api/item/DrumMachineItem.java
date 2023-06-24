package com.gregtechceu.gtceu.api.item;


import com.gregtechceu.gtceu.api.block.IMachineBlock;
import dev.architectury.injectables.annotations.ExpectPlatform;

/**
 * @author KilaBash
 * @date 2023/3/28
 * @implNote DrumMachineItem
 */
public class DrumMachineItem extends MetaMachineItem {
    protected DrumMachineItem(IMachineBlock block, Properties properties) {
        super(block, properties);
    }

    @ExpectPlatform
    public static DrumMachineItem create(IMachineBlock block, Properties properties) {
        throw new AssertionError();
    }

}
