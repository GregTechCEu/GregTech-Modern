package com.gregtechceu.gtceu.core.util.extensions;

import lombok.experimental.UtilityClass;
import org.jetbrains.annotations.ApiStatus;
import org.joml.Vector3f;

// package-private so only things in this package can use it
@UtilityClass
class ExtensionHelpers {

    /// Avoid allocations in hot code by using thread-local temporary vectors in {@link VertexConsumerExt}
    @ApiStatus.Internal
    static final ThreadLocal<Vector3f> scratch = ThreadLocal.withInitial(Vector3f::new);
}
