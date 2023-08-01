package com.gregtechceu.gtceu.common.pipelike.longdistance;


import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;

public class LDPipeProperties {

    public enum NodeType {
        IN, OUT, PIPE
    }

    @Getter @Setter
    private NodeType nodeType;

    public LDPipeProperties(NodeType nodeType) {
        this.nodeType = nodeType;
    }

}
