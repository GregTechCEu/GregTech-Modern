package com.gregtechceu.gtceu.integration.ae2;

import com.gregtechceu.gtceu.api.data.tag.TagPrefix;

import appeng.core.definitions.AEBlocks;
import appeng.core.definitions.AEItems;

import static com.gregtechceu.gtceu.common.data.GTMaterials.CertusQuartz;

public class GTAE2Materials {

    public static void init() {
        TagPrefix.gem.setIgnored(CertusQuartz, AEItems.CERTUS_QUARTZ_CRYSTAL);
        TagPrefix.dust.setIgnored(CertusQuartz, AEItems.CERTUS_QUARTZ_DUST);
        TagPrefix.block.setIgnoredBlock(CertusQuartz, AEBlocks.QUARTZ_BLOCK::block);
    }
}
