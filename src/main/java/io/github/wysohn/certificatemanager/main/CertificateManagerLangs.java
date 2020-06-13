package io.github.wysohn.certificatemanager.main;

import io.github.wysohn.rapidframework2.core.manager.lang.Lang;

public enum CertificateManagerLangs implements Lang {

    ;

    private final String[] def;

    CertificateManagerLangs(String... def) {
        this.def = def;
    }

    @Override
    public String[] getEngDefault() {
        return def;
    }
}
