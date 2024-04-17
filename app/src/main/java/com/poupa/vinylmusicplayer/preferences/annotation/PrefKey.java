package com.poupa.vinylmusicplayer.preferences.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface PrefKey {
    // Wherether the annotated value should be exportable
    boolean ExportImportable() default true;

    // Wherether the annotated field value is just a preference key prefix, i.e. not the whole key
    boolean IsPrefix() default false;
}
