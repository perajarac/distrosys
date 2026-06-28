package rs.ac.bg.etf.job;

import rs.ac.bg.etf.proto.JobStatus;
import rs.ac.bg.etf.proto.SimulationType;
import rs.ac.bg.etf.proto.SubJobStatus;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Server-side representation of a submitted simulation job.
 */
public class Job {

    private long id;
    private JobStatus status;
    private SimulationType simType;
    private long endTime;
    private String componentsPath;
    private String connectionsPath;
    private String clientOutputName;
    private Instant createdAt;
    private Instant finishedAt;
    private String errorMessage;
    private List<SubJob> subJobs;
    private byte[] resultContent;

    /** Creates a job in {@link JobStatus#READY} state with an empty sub-job list. */
    public Job() {
        this.status = JobStatus.READY;
        this.subJobs = new ArrayList<>();
        this.createdAt = Instant.now();
    }

    /**
     * @return unique job identifier
     */
    public long getId() {
        return id;
    }

    /**
     * @param id unique job identifier
     */
    public void setId(long id) {
        this.id = id;
    }

    /**
     * @return current job lifecycle status
     */
    public JobStatus getStatus() {
        return status;
    }

    /**
     * @param status current job lifecycle status
     */
    public void setStatus(JobStatus status) {
        this.status = status;
    }

    /**
     * @return requested simulation execution strategy
     */
    public SimulationType getSimType() {
        return simType;
    }

    /**
     * @param simType requested simulation execution strategy
     */
    public void setSimType(SimulationType simType) {
        this.simType = simType;
    }

    /**
     * @return simulation end time (logical time)
     */
    public long getEndTime() {
        return endTime;
    }

    /**
     * @param endTime simulation end time (logical time)
     */
    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }

    /**
     * @return path to the components input file
     */
    public String getComponentsPath() {
        return componentsPath;
    }

    /**
     * @param componentsPath path to the components input file
     */
    public void setComponentsPath(String componentsPath) {
        this.componentsPath = componentsPath;
    }

    /**
     * @return path to the connections input file
     */
    public String getConnectionsPath() {
        return connectionsPath;
    }

    /**
     * @param connectionsPath path to the connections input file
     */
    public void setConnectionsPath(String connectionsPath) {
        this.connectionsPath = connectionsPath;
    }

    /**
     * @return client-provided name for the output file
     */
    public String getClientOutputName() {
        return clientOutputName;
    }

    /**
     * @param clientOutputName client-provided name for the output file
     */
    public void setClientOutputName(String clientOutputName) {
        this.clientOutputName = clientOutputName;
    }

    /**
     * @return timestamp when the job was created
     */
    public Instant getCreatedAt() {
        return createdAt;
    }

    /**
     * @param createdAt timestamp when the job was created
     */
    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    /**
     * @return timestamp when the job reached a terminal state
     */
    public Instant getFinishedAt() {
        return finishedAt;
    }

    /**
     * @param finishedAt timestamp when the job reached a terminal state
     */
    public void setFinishedAt(Instant finishedAt) {
        this.finishedAt = finishedAt;
    }

    /**
     * @return error description for failed or aborted jobs
     */
    public String getErrorMessage() {
        return errorMessage;
    }

    /**
     * @param errorMessage error description for failed or aborted jobs
     */
    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    /**
     * @return sub-jobs created by the scheduler for this job
     */
    public List<SubJob> getSubJobs() {
        return subJobs;
    }

    /**
     * @param subJobs sub-jobs created by the scheduler for this job
     */
    public void setSubJobs(List<SubJob> subJobs) {
        this.subJobs = subJobs;
    }

    /**
     * @return merged simulation output bytes when the job is done
     */
    public byte[] getResultContent() {
        return resultContent;
    }

    /**
     * @param resultContent merged simulation output bytes when the job is done
     */
    public void setResultContent(byte[] resultContent) {
        this.resultContent = resultContent;
    }

    /**
     * @return number of sub-jobs in a terminal success state
     */
    public int countCompletedSubJobs() {
        int count = 0;
        for (SubJob subJob : subJobs) {
            if (subJob.getStatus() == SubJobStatus.SUB_JOB_DONE) {
                count++;
            }
        }
        return count;
    }
}
