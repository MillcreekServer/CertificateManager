package io.github.wysohn.%pluginname%.main;

import io.github.wysohn.rapidframework3.interfaces.language.ILang;

public enum $pluginname$Langs implements ILang {

    ;

    private final String[] def;

    $pluginname$Langs(String... def) {
        this.def = def;
    }

    @Override
    public String[] getEngDefault() {
        return def;
    }
}
