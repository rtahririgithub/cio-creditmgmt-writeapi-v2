package com.telus.credit.model.helper;

import java.io.Serializable;

/**
 * Used for determining if an attribute was included in the request body or not
 * because we cannot distinguish between null and unset
 *
 * @param <T>
 */
public class PatchField<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    private final T value;

    public static <T> PatchField<T> of(T value) {
        return new PatchField<>(value);
    }

    public static <T> PatchField<T> patchOrNull(boolean isPatched, T value) {
        if (isPatched || value != null) {
            return of(value);
        }

        return null;
    }

    private PatchField(T value) {
        this.value = value;
    }

    public T get() {
        return this.value;
    }

    public boolean isValueNull() {
        return this.value == null;
    }
}
