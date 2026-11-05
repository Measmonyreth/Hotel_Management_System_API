package com.veth007.hotelmanagement.model.request.Task;

import java.io.Serializable;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateTaskRequest implements Serializable, TaskRequestBase {
    private String description;
    private String taskType;
    private String taskStatus;
    private Long roomId;
    private Long employeeId;
    private String createDated;
}
