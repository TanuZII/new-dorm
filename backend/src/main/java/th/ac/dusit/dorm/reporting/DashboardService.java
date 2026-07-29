package th.ac.dusit.dorm.reporting;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class DashboardService {
    public DashboardSummary summary() {
        return new DashboardSummary(0, 0, 0, 0, 0);
    }
}

