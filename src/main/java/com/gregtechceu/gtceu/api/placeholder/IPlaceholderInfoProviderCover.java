package com.gregtechceu.gtceu.api.placeholder;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import java.util.List;

public interface IPlaceholderInfoProviderCover {

    long getTicksSincePlaced();

    List<? extends Component> getCreateDisplayTargetBuffer();

    void setDisplayTargetBufferLine(int line, MutableComponent component);
}
