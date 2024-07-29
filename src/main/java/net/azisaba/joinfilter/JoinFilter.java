package net.azisaba.joinfilter;

import net.azisaba.joinfilter.listener.PlayerListener;
import net.azisaba.joinfilter.redis.JedisBox;
import net.azisaba.joinfilter.redis.RedisKeys;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.UUID;

public class JoinFilter extends JavaPlugin {
    private JedisBox jedisBox;
    private String groupName;
    private String serverName;
    private String kickMessage;

    @Override
    public void onEnable() {
        reload();
        Bukkit.getPluginManager().registerEvents(new PlayerListener(this), this);
        Objects.requireNonNull(getCommand("joinfilter")).setExecutor(new JoinFilterCommand(this));
    }

    public void reload() {
        if (jedisBox != null) {
            jedisBox.close();
        }
        saveDefaultConfig();
        reloadConfig();
        jedisBox = new JedisBox(
                getConfig().getString("redis.hostname"),
                getConfig().getInt("redis.port"),
                getConfig().getString("redis.username"),
                getConfig().getString("redis.password")
        );
        groupName = getConfig().getString("group-name", "default");
        serverName = getConfig().getString("server-name", "default");
        kickMessage = getConfig().getString("kick-message", "You are not allowed to join this server.");
    }

    public @NotNull String getGroupName() {
        return Objects.requireNonNull(groupName, "group-name is not set");
    }

    public @NotNull String getServerName() {
        return Objects.requireNonNull(serverName, "server-name is not set");
    }

    public @NotNull String getKickMessage() {
        return Objects.requireNonNull(kickMessage, "kick-message is not set");
    }

    public void addPlayer(@NotNull UUID uuid, @NotNull String server) {
        jedisBox.set(RedisKeys.getJoinFilterKey(getGroupName(), uuid.toString(), server), "1");
    }

    public void addPlayerWithExpire(@NotNull UUID uuid, @NotNull String server, long expireSeconds) {
        jedisBox.setWithExpire(RedisKeys.getJoinFilterKey(getGroupName(), uuid.toString(), server), "1", expireSeconds);
    }

    public void removePlayer(@NotNull String uuid, @NotNull String server) {
        jedisBox.delPattern(RedisKeys.getJoinFilterKey(getGroupName(), uuid, server));
    }

    public boolean isAllowed(@NotNull UUID uuid) {
        if (jedisBox == null) return false;
        try {
            return jedisBox.get(RedisKeys.getJoinFilterKey(getGroupName(), uuid.toString(), getServerName()), 1000).equals("1");
        } catch (NoSuchElementException ignored) {
            return false;
        }
    }

    public boolean isAllowed(@NotNull UUID uuid, @NotNull String serverName) {
        if (jedisBox == null) return false;
        try {
            return jedisBox.get(RedisKeys.getJoinFilterKey(getGroupName(), uuid.toString(), serverName), 1000).equals("1");
        } catch (NoSuchElementException ignored) {
            return false;
        }
    }
}
