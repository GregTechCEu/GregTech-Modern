package com.gregtechceu.gtceu.api.graphnet.logic;

public interface INetLogicEntryListener {

    void markLogicEntryAsUpdated(NetLogicEntry<?, ?> entry, boolean fullChange);
}
