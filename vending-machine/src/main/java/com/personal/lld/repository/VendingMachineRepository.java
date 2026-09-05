package com.personal.lld.repository;

import com.personal.lld.domain.VendingMachine;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
@Slf4j
public class VendingMachineRepository {

    private final Map<Integer, VendingMachine> machines = new HashMap<>();
    private int nextMachineId = 1;

    public VendingMachineRepository() {
        log.info("VendingMachineRepository initialized");
    }

    public VendingMachine findById(int machineId) {
        VendingMachine machine = machines.get(machineId);

        if (machine == null) {
            log.warn("Repository: Machine not found with ID: {}", machineId);
        } else {
            log.info("Repository: Found machine with ID: {}", machineId);
        }

        return machine;
    }

    public VendingMachine save(VendingMachine machine) {
        if (machine.getId() == 0) {
            machine.setId(nextMachineId++);
        }

        machines.put(machine.getId(), machine);
        log.info("Repository: Saved machine with ID: {}", machine.getId());

        return machine;
    }

    public void updateInventory(
        int machineId,
        int productId,
        int quantity
    ) {
        VendingMachine machine = findById(machineId);

        if (machine != null) {
            // This would typically update the inventory in the machine.
            log.info(
                "Repository: Updated inventory for machine {}, product {}, quantity {}",
                machineId,
                productId,
                quantity
            );
        }
    }

    public void updateMachineState(int machineId, String newState) {
        VendingMachine machine = findById(machineId);

        if (machine != null) {
            // This would typically update the machine state.
            log.info(
                "Repository: Updated machine {} state to {}",
                machineId,
                newState
            );
        }
    }

    public boolean exists(int machineId) {
        return machines.containsKey(machineId);
    }

    public int getTotalMachines() {
        return machines.size();
    }

    public List<VendingMachine> findAll() {
        return new ArrayList<>(machines.values());
    }
}
