package com.gregtechceu.gtceu.integration.jsonthings.serializers;

import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.MetaMachine;

import com.google.gson.JsonObject;

public interface IMachineSerializer<D extends MachineDefinition, M extends MetaMachine> {

    IMachineFactory<D, M> createFactory(JsonObject data);
}
