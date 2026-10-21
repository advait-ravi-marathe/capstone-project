import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

public class ParcelDemo {
    public static void main(String[] args) throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/", exchange -> {
            String path = exchange.getRequestURI().getPath();
            int status = 200;
            String mime = "text/plain; charset=utf-8";
            byte[] bytes;
            if (path.equals("/") || path.equals("/parcel.js")) {
                String resource = path.equals("/") ? "/index.html" : "/parcel.js";
                try (var input = ParcelDemo.class.getResourceAsStream(resource)) {
                    if (input == null) {
                        status = 500;
                        bytes = "missing packaged asset".getBytes(StandardCharsets.UTF_8);
                    } else {
                        bytes = input.readAllBytes();
                    }
                }
                mime = path.equals("/") ? "text/html; charset=utf-8" : "text/javascript; charset=utf-8";
            } else if (path.equals("/health")) {
                bytes = "ready".getBytes(StandardCharsets.UTF_8);
            } else {
                status = path.equals("/api/session") ? 401 : 404;
                bytes = "not available".getBytes(StandardCharsets.UTF_8);
            }
            exchange.getResponseHeaders().set("Content-Type", mime);
            exchange.sendResponseHeaders(status, bytes.length);
            try (var output = exchange.getResponseBody()) {
                output.write(bytes);
            }
        });
        Runtime.getRuntime().addShutdownHook(new Thread(() -> server.stop(0)));
        server.start();
        System.out.println("PORT=" + server.getAddress().getPort());
    }
}
