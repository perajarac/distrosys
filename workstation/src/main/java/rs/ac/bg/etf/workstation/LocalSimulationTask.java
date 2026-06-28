package rs.ac.bg.etf.workstation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rs.ac.bg.etf.kdp.simulation.components.Field;
import rs.ac.bg.etf.kdp.simulation.io.NetlistLoader;
import rs.ac.bg.etf.kdp.simulation.io.SimulatorFactory;
import rs.ac.bg.etf.proto.SubJobRequest;
import rs.ac.bg.etf.sleep.simulation.Netlist;
import rs.ac.bg.etf.sleep.simulation.SimBufferLocal;
import rs.ac.bg.etf.sleep.simulation.Simulator;

/** Runnable simulation of a sub-job partition assigned to this workstation. */
public class LocalSimulationTask implements java.util.concurrent.Callable<SubJobResult> {

  private static final Logger log = LoggerFactory.getLogger(LocalSimulationTask.class);

  private final SubJobRequest request;
  private final WorkstationState state;

  /**
   * @param request sub-job payload from the server
   * @param state shared workstation runtime state
   */
  public LocalSimulationTask(SubJobRequest request, WorkstationState state) {
    this.request = request;
    this.state = state;
  }

  @Override
  public SubJobResult call() {
    WorkstationPeerRegistry peers = new WorkstationPeerRegistry();
    SimBufferLocal<Field> localQueue = new SimBufferLocal<>();
    SimBufferNetwork network =
        new SimBufferNetwork(
            request.getJobId(),
            request.getLocalWorkstationId(),
            request.getComponentOwnerMap(),
            peers,
            localQueue);

    if (!request.getWorkstationEndpointsMap().isEmpty()) {
      peers.registerPeers(request.getWorkstationEndpointsMap());
    }

    state.registerSimulation(request.getJobId(), new ActiveSimulation(request.getJobId(), network));
    try {
      Netlist<Object> netlist =
          NetlistLoader.loadFromLines(
              request.getComponentLinesList(), request.getConnectionLinesList());
      rs.ac.bg.etf.kdp.simulation.io.SimulationType simType =
          WorkstationProtoMapper.toCore(request.getSimType());
      Simulator<Object> simulator = (Simulator<Object>) SimulatorFactory.create(simType, 1);
      simulator.setNetlist(netlist);
      @SuppressWarnings("unchecked")
      rs.ac.bg.etf.sleep.simulation.SimBuffer<Object> queue =
          (rs.ac.bg.etf.sleep.simulation.SimBuffer<Object>)
              (rs.ac.bg.etf.sleep.simulation.SimBuffer<?>) network;
      simulator.setQueue(queue);
      simulator.init();
      while (simulator.getlTime() < request.getEndTime()) {
        simulator.execute();
      }
      return SubJobResult.success(
          WorkstationProtoMapper.toComponentStates(netlist, request.getComponentLinesList()));
    } catch (Exception e) {
      log.error(
          "Simulation failed for job {} sub-job {}", request.getJobId(), request.getSubjobId(), e);
      return SubJobResult.failure(e.getMessage() != null ? e.getMessage() : e.toString());
    } finally {
      state.unregisterSimulation(request.getJobId());
      peers.shutdown();
    }
  }
}
