package com.veth007.hotelmanagement.model.response.Task;

import java.util.List;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class TaskListResponse {
    private int totalTasks;
    private int totalInProgress;
    private int totalCompleted;
    private int totalPending;
    private int totalCancelled;
    private List<TaskResponse> tasks;
}
