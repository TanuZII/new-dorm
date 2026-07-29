package th.ac.dusit.dorm.identity;

import java.util.List;

public record LoginResponse(String username, List<String> roles) {
}

