package io.github.wysohn.certificatemanager.objects;

import io.github.wysohn.certificatemanager.manager.IExamTaker;
import io.github.wysohn.rapidframework2.bukkit.main.objects.BukkitPlayer;

import java.util.*;

public class User extends BukkitPlayer implements IExamTaker {
    private final Set<String> certificates = new HashSet<>();
    private final Map<String, Long> certificateExpire = new HashMap<>();

    private User() {
        super(null);
    }

    public User(UUID key) {
        super(key);
    }

    @Override
    public boolean containsCertificate(String o) {
        return certificates.contains(o);
    }

    @Override
    public boolean addCertificate(String s) {
        final boolean add = certificates.add(s);
        notifyObservers();
        return add;
    }

    @Override
    public boolean removeCertificate(String o) {
        final boolean remove = certificates.remove(o);
        notifyObservers();
        return remove;
    }

    @Override
    public void clearCertificates() {
        certificates.clear();
        notifyObservers();
    }

    @Override
    public Collection<String> getCertificates() {
        return Collections.unmodifiableCollection(certificates);
    }

    @Override
    public long getExpireDate(String o) {
        return certificateExpire.getOrDefault(o, -1L);
    }

    @Override
    public void setExpiration(String s, long expire) {
        certificateExpire.put(s, expire);
        notifyObservers();
    }

    @Override
    public void clearExpirations() {
        certificateExpire.clear();
        notifyObservers();
    }
}
