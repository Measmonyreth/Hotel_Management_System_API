package com.veth007.hotelmanagement.model.response.Task;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

import com.veth007.hotelmanagement.model.entity.TaskEnttity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TaskResponse implements Serializable {
    private Long id;
    private Map<String, Object> room;
    private Map<String, Object> employee;
    private String task_type;
    private String task_status;
    private String description;
    private LocalDateTime createdDate;
    private LocalDateTime lastModifiedDate;
    private LocalDateTime deletedDate;
    private String createBy;
    private String lastModifiedBy;
    private String deletedBy;

    public static TaskResponse fromEntity(TaskEnttity task) {
        return TaskResponse.builder()
                .id(task.getId())
                .room(task.getRoom() != null ? roomDetails(task) : null)
                .employee(task.getEmployee() != null ? employeeDetails(task) : null)
                .task_type(task.getTaskType().name())
                .task_status(task.getTaskStatus().name())
                .description(task.getDescription())
                .createdDate(task.getCreatedDate())
                .lastModifiedDate(task.getLastModifiedDate())
                .deletedDate(task.getDeletedDate())
                .createBy(task.getCreatedBy())
                .lastModifiedBy(task.getLastModifiedBy())
                .deletedBy(task.getDeleted_by())
                .build();
    }

    private static Map<String, Object> roomDetails(TaskEnttity task) {
        Map<String, Object> room = new LinkedHashMap<>();
        room.put("id", task.getRoom().getId());
        room.put("roomNumber", task.getRoom().getRoomNumber());
        room.put("roomName", task.getRoom().getRoomName());

        return room;
    }

    private static Map<String, Object> employeeDetails(TaskEnttity task) {
        Map<String, Object> employee = new LinkedHashMap<>();
        employee.put("id", task.getEmployee().getId());
        employee.put("employeeId", task.getEmployee().getEmployeeId());
        employee.put("name", task.getEmployee().getName());

        return employee;
    }
}
