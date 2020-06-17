package io.github.wysohn.certificatemanager.objects.reward;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

public abstract class StringListReward extends Reward{
    protected final List<String> stringList = new ArrayList<>();

    public StringListReward(List<String> list) {
        stringList.addAll(list);
    }
}
