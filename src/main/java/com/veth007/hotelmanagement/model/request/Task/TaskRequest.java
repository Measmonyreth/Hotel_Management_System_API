package com.veth007.hotelmanagement.model.request.Task;

import java.io.Serializable;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.lang.Long;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TaskRequest implements Serializable, TaskRequestBase {
    private String description;
    @NotBlank(message = "Task type is required")
    private String taskType;
    @NotNull(message = "Room id is required")
    private Long roomId;
    @NotNull(message = "Employee id is required")
    private Long employeeId;
}
