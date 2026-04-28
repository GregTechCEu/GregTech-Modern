package com.gregtechceu.gtceu.client.renderer;

public final class LightTexture {

    public static final int FULL_BRIGHT = pack(15, 15);

    private LightTexture() {}

    public static int pack(int blockLight, int skyLight) {
        return blockLight << 4 | skyLight << 20;
    }

    public static int block(int packedLight) {
        return packedLight >> 4 & 0xF;
    }

    public static int sky(int packedLight) {
        return packedLight >> 20 & 0xF;
    }
}
