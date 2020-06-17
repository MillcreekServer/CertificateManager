package io.github.wysohn.certificatemanager.manager;

import java.util.Collection;
import java.util.Locale;

public interface IExamTaker {
    boolean containsCertificate(String o);

    boolean addCertificate(String s);

    boolean removeCertificate(String o);

    void clearCertificates();

    Collection<String> getCertificates();

    long getExpireDate(String o);

    void setExpiration(String s, long expire);

    void clearExpirations();

    Locale getLocale();
}
