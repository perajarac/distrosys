package rs.ac.bg.etf.workstation;

import rs.ac.bg.etf.client.ServerClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Periodically reports workstation load to the server via heartbeat RPCs.
 */
public class HeartbeatSender {

    private static final Logger log = LoggerFactory.getLogger(HeartbeatSender.class);

    private final ServerClient serverClient;
    private final WorkstationState state;
    private final int intervalSec;
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread thread = new Thread(r, "heartbeat-sender");
        thread.setDaemon(true);
        return thread;
    });

    /**
     * @param serverClient gRPC client to the server
     * @param state        workstation runtime state
     * @param intervalSec  seconds between heartbeats
     */
    public HeartbeatSender(ServerClient serverClient, WorkstationState state, int intervalSec) {
        this.serverClient = serverClient;
        this.state = state;
        this.intervalSec = intervalSec;
    }

    /**
     * Starts the periodic heartbeat task.
     */
    public void start() {
        scheduler.scheduleAtFixedRate(this::sendHeartbeat, intervalSec, intervalSec, TimeUnit.SECONDS);
    }

    /**
     * Stops the heartbeat scheduler.
     */
    public void stop() {
        scheduler.shutdownNow();
    }

    private void sendHeartbeat() {
        String workstationId = state.getWorkstationId();
        if (workstationId == null) {
            return;
        }
        try {
            boolean acknowledged = serverClient.heartbeat(
                    workstationId, state.getActiveSubjobs(), state.getReportedLoad());
            if (!acknowledged) {
                log.warn("Heartbeat not acknowledged for workstation {}", workstationId);
            } else {
                log.debug("Heartbeat acknowledged for workstation {} (active={}, load={})",
                        workstationId, state.getActiveSubjobs(), state.getReportedLoad());
            }
        } catch (Exception e) {
            log.error("Heartbeat failed for workstation {}", workstationId, e);
        }
    }
}
