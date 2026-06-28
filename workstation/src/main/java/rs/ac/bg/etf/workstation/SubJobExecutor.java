package rs.ac.bg.etf.workstation;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * Thread pool for executing {@link LocalSimulationTask} instances with fixed parallelism.
 */
public class SubJobExecutor {

    private final ExecutorService executor;

    /**
     * @param parallelism maximum concurrent sub-jobs
     */
    public SubJobExecutor(int parallelism) {
        this.executor = Executors.newFixedThreadPool(parallelism);
    }

    /**
     * Submits a sub-job task for execution.
     *
     * @param task callable simulation task
     * @return future completing with the sub-job result
     */
    public Future<SubJobResult> submit(Callable<SubJobResult> task) {
        return executor.submit(task);
    }

    /**
     * Shuts down the executor and waits briefly for tasks to finish.
     */
    public void shutdown() {
        executor.shutdown();
        try {
            executor.awaitTermination(30, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            executor.shutdownNow();
        }
    }
}
