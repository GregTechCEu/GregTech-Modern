package com.gregtechceu.gtceu.common.cosmetics;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.cosmetics.event.RegisterGTCapesEvent;

import net.minecraft.resources.ResourceLocation;

import java.util.UUID;

public class GTCapes {

    public static final ResourceLocation GREGTECH_CAPE = GTCEu.id("gregtech");
    public static final ResourceLocation GREEN_CAPE = GTCEu.id("green");
    public static final ResourceLocation YELLOW_CAPE = GTCEu.id("yellow");
    public static final ResourceLocation RED_CAPE = GTCEu.id("red");
    public static final ResourceLocation DEBUG_CAPE = GTCEu.id("debug");

    public static final ResourceLocation RAINBOW_CAPE = GTCEu.id("rainbow");
    public static final ResourceLocation ACE_CAPE = GTCEu.id("ace");
    public static final ResourceLocation AGENDER_CAPE = GTCEu.id("agender");
    public static final ResourceLocation AROMANTIC_CAPE = GTCEu.id("aromantic");
    public static final ResourceLocation BI_CAPE = GTCEu.id("bi");
    public static final ResourceLocation GENDERFLUID_CAPE = GTCEu.id("genderfluid");
    public static final ResourceLocation GENDERQUEER_CAPE = GTCEu.id("genderqueer");
    public static final ResourceLocation INTERSEX_CAPE = GTCEu.id("intersex");
    public static final ResourceLocation LESBIAN_CAPE = GTCEu.id("lesbian");
    public static final ResourceLocation NONBINARY_CAPE = GTCEu.id("nonbinary");
    public static final ResourceLocation PAN_CAPE = GTCEu.id("pan");
    public static final ResourceLocation TRANS_CAPE = GTCEu.id("trans");

    public static void registerGTCapes(RegisterGTCapesEvent event) {
        // TODO add the advancements to unlock these
        // Reference
        // https://github.com/GregTechCEu/GregTech/blob/master/src/main/java/gregtech/api/util/CapesRegistry.java#L151-L156
        // for correct advancement IDs
        event.registerCape(GREGTECH_CAPE, GTCEu.id("textures/capes/gregtech_cape.png"));
        event.registerCape(GREEN_CAPE, GTCEu.id("textures/capes/green_cape.png"));
        event.registerCape(YELLOW_CAPE, GTCEu.id("textures/capes/yellow_cape.png"));
        event.registerCape(RED_CAPE, GTCEu.id("textures/capes/red_cape.png"));
        // don't link this to anything (except easter egg? april fools? TBD)
        event.registerCape(DEBUG_CAPE, GTCEu.id("textures/capes/debug_cape.png"));

        event.registerFreeCape(RAINBOW_CAPE, GTCEu.id("textures/capes/rainbow_cape.png"));
        event.registerFreeCape(ACE_CAPE, GTCEu.id("textures/capes/ace_cape.png"));
        event.registerFreeCape(AGENDER_CAPE, GTCEu.id("textures/capes/agender_cape.png"));
        event.registerFreeCape(AROMANTIC_CAPE, GTCEu.id("textures/capes/aromantic_cape.png"));
        event.registerFreeCape(BI_CAPE, GTCEu.id("textures/capes/bi_cape.png"));
        event.registerFreeCape(GENDERFLUID_CAPE, GTCEu.id("textures/capes/genderfluid_cape.png"));
        event.registerFreeCape(GENDERQUEER_CAPE, GTCEu.id("textures/capes/genderqueer_cape.png"));
        event.registerFreeCape(INTERSEX_CAPE, GTCEu.id("textures/capes/intersex_cape.png"));
        event.registerFreeCape(LESBIAN_CAPE, GTCEu.id("textures/capes/lesbian_cape.png"));
        event.registerFreeCape(NONBINARY_CAPE, GTCEu.id("textures/capes/nonbinary_cape.png"));
        event.registerFreeCape(PAN_CAPE, GTCEu.id("textures/capes/pan_cape.png"));
        event.registerFreeCape(TRANS_CAPE, GTCEu.id("textures/capes/trans_cape.png"));
    }

    public static void giveDevCapes(RegisterGTCapesEvent event) {
        // updated on 7.9.2025 - DilithiumThoride
        event.unlockCapeFor(UUID.fromString("a24a9108-23d2-43fc-8db7-43f809d017db"), GREGTECH_CAPE); // ALongStringOfNumbers
        event.unlockCapeFor(UUID.fromString("fbd96f69-60f9-481c-b71e-4b190cd5fc72"), GREGTECH_CAPE); // Anne-Marie
        event.unlockCapeFor(UUID.fromString("77e2129d-8f68-4025-9394-df946f1f3aee"), GREGTECH_CAPE); // Brachy84
        event.unlockCapeFor(UUID.fromString("c1377a67-4585-46b6-b70e-dfaa419f1e71"), GREGTECH_CAPE); // BraggestSage833
        event.unlockCapeFor(UUID.fromString("aaf70ec1-ac70-494f-9966-ea5933712750"), GREGTECH_CAPE); // Bruberu
        event.unlockCapeFor(UUID.fromString("274846e6-1d07-4e59-8dea-f4f73e76f9fb"), GREGTECH_CAPE); // DilithiumThoride
        event.unlockCapeFor(UUID.fromString("c43b3c3d-7da6-4c2b-b335-703fce2ed795"), GREGTECH_CAPE); // Ghostipedia
        event.unlockCapeFor(UUID.fromString("88374b6a-4710-46cd-bb04-a1580905a918"), GREGTECH_CAPE); // Ghzdude
        event.unlockCapeFor(UUID.fromString("fe4bafe8-8ea9-494a-b4e9-29397cea89fc"), GREGTECH_CAPE); // Gustavo
        event.unlockCapeFor(UUID.fromString("5d7073e3-882f-4c4a-94b3-0e5ba1c11e02"), GREGTECH_CAPE); // htmlcsjs
        event.unlockCapeFor(UUID.fromString("c18c1d7f-3174-42c6-81dc-3c7ff9f720c3"), GREGTECH_CAPE); // jurrejelle
        event.unlockCapeFor(UUID.fromString("29f1e04c-58d8-4a3b-9eff-f85be7825256"), GREGTECH_CAPE); // kross000
        event.unlockCapeFor(UUID.fromString("2fa297a6-7803-4629-8360-7059155cf43e"), GREGTECH_CAPE); // KilaBash
        event.unlockCapeFor(UUID.fromString("44f38ff8-aad7-49c3-acb3-d92317af9078"), GREGTECH_CAPE); // LAGIdiot
        event.unlockCapeFor(UUID.fromString("3dbb689e-edcf-41b9-9c09-6ae4ebb6ca5b"), GREGTECH_CAPE); // M_W_K
        // todo Mikerooni (when he gets a new account)
        event.unlockCapeFor(UUID.fromString("5cb66945-2ca4-498d-8c9a-29a676769363"), GREGTECH_CAPE); // omergunr100
        event.unlockCapeFor(UUID.fromString("4a57640e-c1b1-4413-a6ab-e9a8b60ec167"), GREGTECH_CAPE); // PrototypeTrousers
        event.unlockCapeFor(UUID.fromString("12892f29-9eef-47ed-b8fb-df3e0e90db0c"), GREGTECH_CAPE); // Quarri6343
        event.unlockCapeFor(UUID.fromString("1184eb79-5831-4f7d-b8f4-3a46fccf7a1d"), GREGTECH_CAPE); // screret
        event.unlockCapeFor(UUID.fromString("a82fb558-64f9-4dd6-a87d-84040e84bb43"), GREGTECH_CAPE); // serenibyss
        event.unlockCapeFor(UUID.fromString("f76fc8b3-ac6b-44b9-9023-76edaf3d5909"), GREGTECH_CAPE); // spicierspace153
        event.unlockCapeFor(UUID.fromString("24ab5496-0c9d-45d7-bfa6-c57760263be6"), GREGTECH_CAPE); // TarLaboratories
        event.unlockCapeFor(UUID.fromString("5c2933b3-5340-4356-81e7-783c53bd7845"), GREGTECH_CAPE); // Tech22
        event.unlockCapeFor(UUID.fromString("30628e4c-f7ac-427f-8ca7-aab2c0572be8"), GREGTECH_CAPE); // TheLastKumquat
        event.unlockCapeFor(UUID.fromString("e6e784af-bd04-46ad-8141-47b8b9102cb9"), GREGTECH_CAPE); // Tictim
        event.unlockCapeFor(UUID.fromString("60057953-6a71-4f11-9e72-bb0c81fa0085"), GREGTECH_CAPE); // ursamina
        event.unlockCapeFor(UUID.fromString("3a27782d-6864-4814-a9fe-7c6931f29a8a"), GREGTECH_CAPE); // YoungOnion
        event.unlockCapeFor(UUID.fromString("56bd41d0-06ef-4ed7-ab48-926ce45651f9"), GREGTECH_CAPE); // Zalgo239
        event.unlockCapeFor(UUID.fromString("2c69579f-a7fa-46ad-814e-9837e01215c1"), GREGTECH_CAPE); // Zorbatron
    }
}
