package net.azisaba.joinfilter.redis;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.io.Closeable;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public final class JedisBox implements Closeable {
    private final JedisPool jedisPool;

    public JedisBox(@NotNull String hostname, int port, @Nullable String username, @Nullable String password) {
        this.jedisPool = createPool(hostname, port, username, password);
    }

    public @NotNull String get(@NotNull String key) throws NoSuchElementException {
        return get(key, 1000);
    }

    public @NotNull String get(@NotNull String key, int timeoutMillis) throws NoSuchElementException {
        try {
            return CompletableFuture.supplyAsync(() -> {
                try (Jedis jedis = jedisPool.getResource()) {
                    String data = jedis.get(key);
                    if (data == null) {
                        throw new NoSuchElementException();
                    }
                    return data;
                }
            }).get(timeoutMillis, TimeUnit.MILLISECONDS);
        } catch (InterruptedException | TimeoutException | ExecutionException e) {
            if (e.getCause() instanceof NoSuchElementException) {
                throw (NoSuchElementException) e.getCause();
            }
            throw new RuntimeException(e);
        }
    }

    public void set(@NotNull String key, @NotNull String value) {
        try (Jedis jedis = jedisPool.getResource()) {
            jedis.set(key, value);
        }
    }

    public void del(@NotNull String key) {
        try (Jedis jedis = jedisPool.getResource()) {
            jedis.del(key);
        }
    }

    public void delPattern(@NotNull String pattern) {
        if (pattern.equals("*")) {
            throw new IllegalArgumentException("Blocking unsafe pattern: '*' (use '**' if you are sure)");
        }
        try (Jedis jedis = jedisPool.getResource()) {
            for (String key : jedis.keys(pattern)) {
                jedis.del(key);
            }
        }
    }

    public void setWithExpire(@NotNull String key, @NotNull String value, long expireSeconds) {
        try (Jedis jedis = jedisPool.getResource()) {
            jedis.setex(key, expireSeconds, value);
        }
    }

    @Contract(pure = true)
    @NotNull
    public JedisPool getJedisPool() {
        return jedisPool;
    }

    @Override
    public void close() {
        getJedisPool().close();
    }

    @Contract("_, _, _, _ -> new")
    public static @NotNull JedisPool createPool(@NotNull String hostname, int port, @Nullable String username, @Nullable String password) {
        Objects.requireNonNull(hostname, "hostname");
        if (username != null && password != null) {
            return new JedisPool(hostname, port, username, password);
        } else if (password != null) {
            return new JedisPool(new JedisPoolConfig(), hostname, port, 3000, password);
        } else if (username != null) {
            throw new IllegalArgumentException("password must not be null when username is provided");
        } else {
            return new JedisPool(new JedisPoolConfig(), hostname, port);
        }
    }
}

