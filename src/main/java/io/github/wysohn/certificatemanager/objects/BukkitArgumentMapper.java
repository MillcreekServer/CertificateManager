package io.github.wysohn.certificatemanager.objects;

import io.github.wysohn.certificatemanager.main.CertificateManagerLangs;
import io.github.wysohn.rapidframework2.core.manager.command.ArgumentMapper;
import io.github.wysohn.rapidframework2.core.manager.command.InvalidArgumentException;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

public interface BukkitArgumentMapper<T> extends ArgumentMapper<T> {
    BukkitArgumentMapper<Player> PLAYER = s -> {
        Player player = Bukkit.getPlayer(s);
        if (player == null) {
            throw new InvalidArgumentException(CertificateManagerLangs.General_InvalidPlayer, (sen, langman) ->
                    langman.addString(s));
        }

        return player;
    };

    BukkitArgumentMapper<OfflinePlayer> OFFLINE_PLAYER = s -> {
        OfflinePlayer player = Bukkit.getOfflinePlayer(s);
        if (!player.hasPlayedBefore() || player.getFirstPlayed() < 1L) {
            throw new InvalidArgumentException(CertificateManagerLangs.General_InvalidOfflinePlayer, (sen, langman) ->
                    langman.addString(s));
        }

        return player;
    };
}
