package io.github.wysohn.certificatemanager.main;

import io.github.wysohn.rapidframework3.bukkit.testutils.SimpleBukkitPluginMainTest;
import junit.framework.TestCase;
import org.bukkit.Server;
import org.junit.Test;

public class CertificateManagerTest extends TestCase {
    @Test
    public void testModules(){
        Server server = new SimpleBukkitPluginMainTest<CertificateManager>() {
            @Override
            public CertificateManager instantiate(Server server) {
                return new CertificateManager(server);
            }
        }.enable();
    }
}