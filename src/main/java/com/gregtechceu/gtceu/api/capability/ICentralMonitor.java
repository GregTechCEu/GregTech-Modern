package com.gregtechceu.gtceu.api.capability;

import com.gregtechceu.gtceu.common.machine.multiblock.electric.monitor.MonitorGroup;

import java.util.List;

public interface ICentralMonitor {

    List<MonitorGroup> getMonitorGroups();
}
