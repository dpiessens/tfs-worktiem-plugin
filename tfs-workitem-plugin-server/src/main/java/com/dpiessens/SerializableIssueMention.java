package com.dpiessens;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.Serializable;

public class SerializableIssueMention implements Serializable {
    private final String myId;
    private final String myUrl;

    public SerializableIssueMention(@NotNull String id, @Nullable String url) {
        myId = id;
        myUrl = url;
    }

    @NotNull
    public String getId() {
        return myId;
    }

    @Nullable
    public String getUrl() {
        return myUrl;
    }

    public boolean equals(Object o) {
        return o instanceof SerializableIssueMention && myId.equals(((SerializableIssueMention) o).getId());
    }

    public int hashCode() {
        return myId.hashCode();
    }

    public String toString() {
        return String.format("[%s: %s]", myId, myUrl);
    }
}
