package com.veth007.hotelmanagement.service.Task;

import java.util.List;
import java.util.Map;
import com.veth007.hotelmanagement.exception.ApiResponse;
import com.veth007.hotelmanagement.model.entity.TaskEnttity;
import com.veth007.hotelmanagement.model.request.Task.TaskRequest;
import com.veth007.hotelmanagement.model.request.Task.UpdateTaskRequest;
import com.veth007.hotelmanagement.model.response.Task.TaskListResponse;
import com.veth007.hotelmanagement.model.response.Task.TaskResponse;

public interface TaskService {

    ApiResponse<?> createTask(TaskRequest request);

    ApiResponse<TaskResponse> update(Long id, UpdateTaskRequest request);

    ApiResponse<String> deleted(Long id);

    ApiResponse<List<TaskResponse>> getTaskByEmployeeId(Long employeeId);

    ApiResponse<TaskListResponse> getAll(int page, int size, boolean isPage);
    ApiResponse<TaskResponse> getById(Long id);
    
    ApiResponse<String> restore(Long id);

    ApiResponse<String> updateStatus(Long id);

    ApiResponse<List<TaskResponse>> list_deleted();

    ApiResponse<TaskListResponse> search_filter(Map<String, String> allParams, String sort, String search, int page, int size, boolean isPage);

    List<TaskResponse> toResponses(List<TaskEnttity> entities);
}
