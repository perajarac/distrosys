package rs.ac.bg.etf.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Server configuration loaded from {@code server.properties} with sensible defaults.
 */
public class ServerConfig {

    private final int grpcPort;
    private final int textPort;
    private final String jobsDir;
    private final int schedulerPoolSize;

    private ServerConfig(int grpcPort, int textPort, String jobsDir, int schedulerPoolSize) {
        this.grpcPort = grpcPort;
        this.textPort = textPort;
        this.jobsDir = jobsDir;
        this.schedulerPoolSize = schedulerPoolSize;
    }

    /**
     * Loads configuration from the classpath resource {@code server.properties}.
     *
     * @return loaded configuration
     * @throws IOException if the properties resource cannot be read
     */
    public static ServerConfig load() throws IOException {
        Properties properties = new Properties();
        try (InputStream in = ServerConfig.class.getClassLoader().getResourceAsStream("server.properties")) {
            if (in != null) {
                properties.load(in);
            }
        }
        int grpcPort = Integer.parseInt(properties.getProperty("grpc.port", "50051"));
        int textPort = Integer.parseInt(properties.getProperty("text.port", "50050"));
        String jobsDir = properties.getProperty("jobs.dir", "./jobs");
        int schedulerPoolSize = Integer.parseInt(properties.getProperty("scheduler.pool.size", "4"));
        return new ServerConfig(grpcPort, textPort, jobsDir, schedulerPoolSize);
    }

    /**
     * @return gRPC server listen port
     */
    public int getGrpcPort() {
        return grpcPort;
    }

    /**
     * @return text protocol server listen port
     */
    public int getTextPort() {
        return textPort;
    }

    /**
     * @return directory for persisted job metadata
     */
    public String getJobsDir() {
        return jobsDir;
    }

    /**
     * @return thread pool size for async job scheduling
     */
    public int getSchedulerPoolSize() {
        return schedulerPoolSize;
    }
}
