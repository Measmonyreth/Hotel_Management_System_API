package com.veth007.hotelmanagement.service.Task.Implement;
import java.util.function.BiConsumer;
import java.util.function.Function;
import com.veth007.hotelmanagement.model.request.Task.UpdateTaskRequest;
import com.veth007.hotelmanagement.model.response.Task.TaskResponse;

import lombok.Getter;
import lombok.RequiredArgsConstructor;


@Getter
@RequiredArgsConstructor
public enum TaskGroupBy {
    TASK_TYPE(dto -> dto.getTask_type() != null ? dto.getTask_type() : "N/A",
            (filter, value) -> filter.setTaskType(value)),
    TASK_STATUS(dto -> dto.getTask_status() != null ? dto.getTask_status() : "N/A",
            (filter, value) -> filter.setTaskStatus(value)),
    CREATED_DATE(dto -> dto.getCreatedDate() != null ?
            dto.getCreatedDate().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd")) : "N/A",
            (filter, value) -> filter.setCreateDated(value));

    private final Function<TaskResponse, String> extractor;
    private final BiConsumer<UpdateTaskRequest, String> filterSetter;

    public String extractor(TaskResponse dto) {
        return extractor.apply(dto);
    }

    public void applyFilter(UpdateTaskRequest filter, String value) {
        filterSetter.accept(filter, value);
    }
}
