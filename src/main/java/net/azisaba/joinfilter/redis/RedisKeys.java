package net.azisaba.joinfilter.redis;

import org.jetbrains.annotations.NotNull;

public final class RedisKeys {
    private RedisKeys() {
        throw new AssertionError();
    }

    public static @NotNull String getJoinFilterKey(@NotNull String group, @NotNull String uuid, @NotNull String server) {
        return "joinfilter:" + group + ":" + uuid + ":" + server;
    }
}
