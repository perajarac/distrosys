package rs.ac.bg.etf.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Workstation configuration loaded from {@code workstation.properties} with CLI overrides.
 */
public class WorkstationConfig {

    private final int grpcPort;
    private final String serverHost;
    private final int serverPort;
    private final int heartbeatIntervalSec;
    private final int parallelism;
    private final boolean headless;
    private final String grpcHost;

    private WorkstationConfig(
            int grpcPort,
            String grpcHost,
            String serverHost,
            int serverPort,
            int heartbeatIntervalSec,
            int parallelism,
            boolean headless) {
        this.grpcPort = grpcPort;
        this.grpcHost = grpcHost;
        this.serverHost = serverHost;
        this.serverPort = serverPort;
        this.heartbeatIntervalSec = heartbeatIntervalSec;
        this.parallelism = parallelism;
        this.headless = headless;
    }

    /**
     * Loads defaults from classpath and applies CLI arguments.
     *
     * <p>Usage: {@code <serverHost> <serverPort> <parallelism> [--headless] [--grpc-port PORT]
     * [--grpc-host HOST]}
     *
     * @param args command-line arguments
     * @return resolved configuration
     * @throws IOException if properties cannot be read
     */
    public static WorkstationConfig fromArgs(String[] args) throws IOException {
        Properties properties = new Properties();
        try (InputStream in = WorkstationConfig.class.getClassLoader().getResourceAsStream("workstation.properties")) {
            if (in != null) {
                properties.load(in);
            }
        }

        int defaultGrpcPort = Integer.parseInt(properties.getProperty("grpc.port", "50052"));
        String defaultServerHost = properties.getProperty("server.host", "localhost");
        int defaultServerPort = Integer.parseInt(properties.getProperty("server.port", "50051"));
        int heartbeatIntervalSec = Integer.parseInt(properties.getProperty("heartbeat.interval.sec", "10"));

        if (args.length < 3) {
            throw new IllegalArgumentException(
                    "Usage: <serverHost> <serverPort> <parallelism> [--headless] [--grpc-port PORT] [--grpc-host HOST]");
        }

        String serverHost = args[0];
        int serverPort = Integer.parseInt(args[1]);
        int parallelism = Integer.parseInt(args[2]);
        if (parallelism < 1) {
            throw new IllegalArgumentException("parallelism must be >= 1");
        }

        boolean headless = false;
        int grpcPort = defaultGrpcPort;
        String grpcHost = defaultServerHost;

        for (int i = 3; i < args.length; i++) {
            switch (args[i]) {
                case "--headless" -> headless = true;
                case "--grpc-port" -> {
                    if (i + 1 >= args.length) {
                        throw new IllegalArgumentException("--grpc-port requires a value");
                    }
                    grpcPort = Integer.parseInt(args[++i]);
                }
                case "--grpc-host" -> {
                    if (i + 1 >= args.length) {
                        throw new IllegalArgumentException("--grpc-host requires a value");
                    }
                    grpcHost = args[++i];
                }
                default -> throw new IllegalArgumentException("Unknown argument: " + args[i]);
            }
        }

        if (serverHost.isBlank()) {
            serverHost = defaultServerHost;
        }

        return new WorkstationConfig(
                grpcPort, grpcHost, serverHost, serverPort, heartbeatIntervalSec, parallelism, headless);
    }

    /**
     * @return local gRPC listen port for {@link rs.ac.bg.etf.proto.WorkstationServiceGrpc}
     */
    public int getGrpcPort() {
        return grpcPort;
    }

    /**
     * @return host name advertised to the server for inbound workstation RPCs
     */
    public String getGrpcHost() {
        return grpcHost;
    }

    /**
     * @return server gRPC host
     */
    public String getServerHost() {
        return serverHost;
    }

    /**
     * @return server gRPC port
     */
    public int getServerPort() {
        return serverPort;
    }

    /**
     * @return interval between heartbeat RPCs in seconds
     */
    public int getHeartbeatIntervalSec() {
        return heartbeatIntervalSec;
    }

    /**
     * @return sub-job thread pool size
     */
    public int getParallelism() {
        return parallelism;
    }

    /**
     * @return {@code true} when running without a GUI
     */
    public boolean isHeadless() {
        return headless;
    }
}
