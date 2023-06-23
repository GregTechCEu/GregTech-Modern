package com.gregtechceu.gtceu.api.syncdata;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author KilaBash
 * @date 2023/6/23
 * @implNote RequireRerender.
 * <br>
 * When the annotated fields updated (synced from server) will call the listener method.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
public @interface UpdateListener {
    String methodName();
}
