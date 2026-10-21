package assignments.local;

import java.net.InetAddress;
import java.net.UnknownHostException;

/** Prevents the training runtime from accidentally listening on a network interface. */
public final class LocalBindAddressGuard {
    private LocalBindAddressGuard() { }

    public static void requireLoopback(String address) {
        if (address == null || address.isBlank()) throw new IllegalArgumentException("server.address must be loopback");
        try {
            if (!InetAddress.getByName(address).isLoopbackAddress()) {
                throw new IllegalArgumentException("local CoverStory runtime must bind to loopback only");
            }
        } catch (UnknownHostException ex) {
            throw new IllegalArgumentException("server.address must resolve to loopback", ex);
        }
    }
}
