package io.github.wysohn.certificatemanager.objects.reward;

import io.github.wysohn.certificatemanager.objects.User;
import io.github.wysohn.rapidframework2.core.main.PluginMain;

public abstract class Reward {
    public abstract void reward(PluginMain main, User user);
}
