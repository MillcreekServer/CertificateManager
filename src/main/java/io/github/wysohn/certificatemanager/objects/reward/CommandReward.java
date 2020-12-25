package io.github.wysohn.certificatemanager.objects.reward;

import io.github.wysohn.certificatemanager.objects.User;
import io.github.wysohn.rapidframework3.core.main.PluginMain;
import org.bukkit.Bukkit;

import java.util.List;

public class CommandReward extends StringListReward{
    public CommandReward(List<String> list) {
        super(list);
    }

    @Override
    public void reward(PluginMain main, User user) {
        stringList.stream()
                .map(str -> str.replaceAll("<p>", user.getDisplayName()))
                .forEach(str -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), str));
    }
}
