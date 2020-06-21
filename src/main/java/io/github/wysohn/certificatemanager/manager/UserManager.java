package io.github.wysohn.certificatemanager.manager;

import io.github.wysohn.certificatemanager.objects.User;
import io.github.wysohn.rapidframework2.bukkit.manager.user.AbstractUserManager;
import io.github.wysohn.rapidframework2.core.database.Database;
import org.bukkit.Bukkit;

import java.util.Optional;
import java.util.UUID;

public class UserManager extends AbstractUserManager<User> {
    public UserManager(int loadPriority) {
        super(loadPriority);

        setConstructionHandle(user -> {
            UUID uuid = user.getKey();
            Optional.of(uuid)
                    .map(Bukkit::getPlayer)
                    .ifPresent(user::setSender);
        });
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
