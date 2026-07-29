package th.ac.dusit.dorm.property;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateRoomRequest(
        @NotBlank @Size(max = 20) String buildingCode,
        @NotBlank @Size(max = 20) String number,
        @Min(1) @Max(100) int floor,
        @Min(1) @Max(20) int capacity) {
}

