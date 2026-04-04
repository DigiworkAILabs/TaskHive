package com.digiwork.taskhive.module.employee.listener;

import com.digiwork.taskhive.module.auth.event.AccountActivatedEvent;
import com.digiwork.taskhive.module.employee.model.Employee;
import com.digiwork.taskhive.module.employee.model.EmployeeStatusHistory;
import com.digiwork.taskhive.module.employee.repository.EmployeeRepository;
import com.digiwork.taskhive.module.employee.repository.EmployeeStatusHistoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class EmployeeEventListener {

    private static final String STATUS_ACTIVE = "ACTIVE";
    private final EmployeeRepository employeeRepository;
    private final EmployeeStatusHistoryRepository statusHistoryRepository;

    @Transactional
    @EventListener
    public void handleAccountActivated(AccountActivatedEvent event) {
        log.debug("Synchronizing Employee status for activated User [{}]", event.getUserId());
        
        employeeRepository.findByUserIdAndIsDeletedFalse(event.getUserId())
            .ifPresent(employee -> {
                String oldStatus = employee.getStatus();
                if (!STATUS_ACTIVE.equals(oldStatus)) {
                    employee.setStatus(STATUS_ACTIVE);
                    employeeRepository.save(employee);
                    
                    // Record status history
                    statusHistoryRepository.save(EmployeeStatusHistory.builder()
                            .employeeId(employee.getId())
                            .oldStatus(oldStatus)
                            .newStatus(STATUS_ACTIVE)
                            .changedBy(event.getUserId())
                            .reason("Account activated via email")
                            .build());
                            
                    log.info("Employee [{}] status synchronized to ACTIVE following user activation", employee.getId());
                }
            });
    }
}
