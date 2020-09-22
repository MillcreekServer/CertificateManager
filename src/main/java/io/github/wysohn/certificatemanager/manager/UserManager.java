package io.github.wysohn.certificatemanager.manager;

import io.github.wysohn.certificatemanager.main.CertificateManagerLangs;
import io.github.wysohn.certificatemanager.objects.User;
import io.github.wysohn.rapidframework2.bukkit.main.objects.BukkitWrapper;
import io.github.wysohn.rapidframework2.bukkit.manager.user.AbstractUserManager;
import io.github.wysohn.rapidframework2.core.database.Database;
import io.github.wysohn.rapidframework2.core.objects.location.SimpleLocation;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.lang.ref.Reference;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class UserManager extends AbstractUserManager<User> {
    public Map<UUID, SimpleLocation> currentLocation = new HashMap<>();

    public UserManager(int loadPriority) {
        super(loadPriority);

        setConstructionHandle(user -> {
            UUID uuid = user.getKey();
            Optional.of(uuid)
                    .map(Bukkit::getPlayer)
                    .ifPresent(user::setSender);
        });
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        super.onJoin(event);

        Player player = event.getPlayer();

        currentLocation.put(player.getUniqueId(), new SimpleLocation(player.getWorld().getName(),
                player.getLocation().getBlockX() + 0.5,
                player.getLocation().getBlockY(),
                player.getLocation().getBlockZ() + 0.5));
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();

        currentLocation.remove(player.getUniqueId());

        get(player.getUniqueId())
                .map(Reference::get)
                .ifPresent(user -> {
                    user.setPreventMovement(false);
                    user.setPreventCommands(false);
                });

        super.onQuit(event);
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        Location from = event.getFrom();
        Location to = event.getTo();

        if (to == null)
            return;

        SimpleLocation prev = currentLocation.get(player.getUniqueId());

        Optional<User> optUser = get(player.getUniqueId()).map(Reference::get);
        if (optUser.map(User::isPreventMovement).orElse(false)) {
            if (to.getBlockX() != prev.getX() || to.getBlockZ() != prev.getZ()) {
                event.setTo(new Location(Bukkit.getWorld(prev.getWorld()),
                        prev.getX() + 0.5,
                        prev.getY(),
                        prev.getZ() + 0.5));

                main().lang().sendMessage(BukkitWrapper.player(player),
                        CertificateManagerLangs.UserManager_CannotDoThatTakingExam, true);
                player.chat("&8X");
            }
        } else {
            currentLocation.put(player.getUniqueId(), new SimpleLocation(to.getWorld().getName(),
                    to.getBlockX() + 0.5,
                    to.getBlockY(),
                    to.getBlockZ() + 0.5));
        }
    }

    @EventHandler
    public void onCommand(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        Optional<User> optUser = get(player.getUniqueId()).map(Reference::get);

        if (optUser.map(User::isPreventCommands).orElse(false)) {
            event.setCancelled(true);

            main().lang().sendMessage(BukkitWrapper.player(player),
                    CertificateManagerLangs.UserManager_CannotDoThatTakingExam, true);
            player.chat("&8X");
        }
    }

    @Override
    protected Database.DatabaseFactory<User> createDatabaseFactory() {
        return getDatabaseFactory(User.class, "User");
    }

    @Override
    protected UUID fromString(String s) {
        return UUID.fromString(s);
    }

    @Override
    protected User newInstance(UUID uuid) {
        return new User(uuid);
    }
}
