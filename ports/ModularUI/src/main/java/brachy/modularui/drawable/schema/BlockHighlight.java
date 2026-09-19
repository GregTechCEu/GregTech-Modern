package brachy.modularui.drawable.schema;

import net.minecraft.core.Direction;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.joml.Vector3f;
import java.util.List;

/** Extracts selection overlays without touching global rendering state. */
@Accessors(fluent = true, chain = true)
public class BlockHighlight {
    private static final float[][] VERTICES = {
            {1,0,0, 1,0,1, 0,0,1, 0,0,0}, {0,1,0, 0,1,1, 1,1,1, 1,1,0},
            {0,1,0, 1,1,0, 1,0,0, 0,0,0}, {0,0,1, 1,0,1, 1,1,1, 0,1,1},
            {0,0,0, 0,0,1, 0,1,1, 0,1,0}, {1,1,0, 1,1,1, 1,0,1, 1,0,0}
    };
    @Getter @Setter private int color;
    @Getter @Setter private boolean allSides;
    @Getter @Setter private float thickness;
    public BlockHighlight(int color) { this(color, true); }
    public BlockHighlight(int color, float thickness) { this(color, true, thickness); }
    public BlockHighlight(int color, boolean allSides) { this(color, allSides, 0); }
    public BlockHighlight(int color, boolean allSides, float thickness) {
        this.color = color;
        this.allSides = allSides;
        this.thickness = thickness;
    }
    public List<SchemaGeometry.Vertex> extract(BlockHitResult hit, Vector3f camera) {
        if (hit == null || hit.getType() != HitResult.Type.BLOCK) return List.of();
        var pos = hit.getBlockPos();
        var builder = new SchemaGeometry.Builder().offset(pos.getX(), pos.getY(), pos.getZ());
        float distance = camera.distance(pos.getX() + .5f, pos.getY() + .5f, pos.getZ() + .5f);
        float inset = Math.min(.505f, (float) (thickness * (1 + Math.max(0, Math.sqrt(distance) - 3) / 5)));
        for (Direction side : Direction.values()) {
            if (!allSides && side != hit.getDirection()) continue;
            Vector3f[] outer = new Vector3f[4], inner = new Vector3f[4];
            float[] coordinates = VERTICES[side.get3DDataValue()];
            for (int i = 0; i < 4; i++) {
                outer[i] = new Vector3f(coordinates[i*3], coordinates[i*3+1], coordinates[i*3+2]).mul(1.01f).sub(.005f,.005f,.005f);
                inner[i] = new Vector3f(outer[i]);
                for (int axis = 0; axis < 3; axis++) {
                    if (side.getAxis().ordinal() != axis) inner[i].setComponent(axis,
                            inner[i].get(axis) + (inner[i].get(axis) > .5f ? -inset : inset));
                }
            }
            if (thickness < 0) {
                for (var vertex : outer) vertex(builder, vertex);
            } else if (inset > 0) {
                for (int i = 0; i < 4; i++) {
                    int next = (i + 1) % 4;
                    vertex(builder, outer[i]); vertex(builder, outer[next]);
                    vertex(builder, inner[next]); vertex(builder, inner[i]);
                }
            }
        }
        return builder.build();
    }
    private void vertex(SchemaGeometry.Builder builder, Vector3f vertex) {
        builder.addVertex(vertex.x, vertex.y, vertex.z).setColor(color);
    }
}
