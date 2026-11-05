package com.veth007.hotelmanagement.service.Task.Implement;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.veth007.hotelmanagement.constant.enums.Status;
import com.veth007.hotelmanagement.constant.enums.StatusRoom;
import com.veth007.hotelmanagement.constant.enums.TaskStatus;
import com.veth007.hotelmanagement.constant.enums.TaskType;
import com.veth007.hotelmanagement.exception.AlreadyExistException;
import com.veth007.hotelmanagement.exception.ApiResponse;
import com.veth007.hotelmanagement.exception.NotFoundException;
import com.veth007.hotelmanagement.model.entity.EmployeeEntity;
import com.veth007.hotelmanagement.model.entity.TaskEnttity;
import com.veth007.hotelmanagement.model.entity.Room.RoomEntity;
import com.veth007.hotelmanagement.model.request.Task.TaskRequest;
import com.veth007.hotelmanagement.model.request.Task.TaskRequestBase;
import com.veth007.hotelmanagement.model.request.Task.UpdateTaskRequest;
import com.veth007.hotelmanagement.model.response.Task.TaskListResponse;
import com.veth007.hotelmanagement.model.response.Task.TaskResponse;
import com.veth007.hotelmanagement.repository.EmployeeRepository;
import com.veth007.hotelmanagement.repository.RoomRepository;
import com.veth007.hotelmanagement.repository.TaskRepository;
import com.veth007.hotelmanagement.service.Task.TaskService;

import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TaskServiceImp implements TaskService {
    private final TaskRepository taskRepository;
    private final RoomRepository roomRepository;
    private final EmployeeRepository employeeRepository;

    @Transactional
    public ApiResponse<?> createTask(TaskRequest request) {
        RoomEntity room = roomRepository.findById(request.getRoomId())
                .orElseThrow(() -> new RuntimeException("Room not found with id: " + request.getRoomId()));
        if (!roomRepository.existsByIdAndStatusRoom(request.getRoomId(), StatusRoom.AVAILABLE)) {
            if (!roomRepository.existsByIdAndStatusRoom(request.getRoomId(), StatusRoom.WAIT_CLEAN)) {
                throw new RuntimeException("Room is not available with id: " + request.getRoomId());

            }

        }
        EmployeeEntity employee = employeeRepository.findById(request.getEmployeeId())
                .orElseThrow(() -> new RuntimeException("Employee not found with id: " + request.getEmployeeId()));
        validate_room_and_employee(request);

        try {

            TaskEnttity task = TaskEnttity.builder()
                    .room(room)
                    .employee(employee)
                    .taskType(TaskType.valueOf(request.getTaskType()))
                    .taskStatus(TaskStatus.PENDING)
                    .description(request.getDescription())
                    .build();

            if (room.getStatusRoom() == StatusRoom.AVAILABLE || room.getStatusRoom() == StatusRoom.WAIT_CLEAN) {
                room.setStatusRoom(StatusRoom.valueOf(task.getTaskType().name()));
                roomRepository.save(room);
                return ApiResponse.success(TaskResponse.fromEntity(taskRepository.save(task)));
            }
            return ApiResponse.error("Creating a task is error.", 400);
        } catch (Exception e) {
            throw new RuntimeException("Failed to create task", e);
        }
    }

    private <T extends TaskRequestBase> void validate_room_and_employee(T request) {
        if (roomRepository.existsByIdAndDeletedDateIsNotNull(request.getRoomId())) {
            throw new AlreadyExistException("Room is already deleted with id: " + request.getRoomId());
        }
        if (employeeRepository.existsByIdAndDeletedDateIsNotNull(request.getEmployeeId())) {
            throw new AlreadyExistException("Employee is already deleted with id: " + request.getEmployeeId());
        }

        if (!employeeRepository.existsByIdAndStatus(request.getEmployeeId(),
                com.veth007.hotelmanagement.constant.enums.Status.ACTIVE)) {
            throw new RuntimeException("Employee is not active with id: " + request.getEmployeeId());
        }
        if (!roomRepository.existsByIdAndAction(request.getRoomId(), Status.ACTIVE)) {
            throw new RuntimeException("Room is not active with id: " + request.getRoomId());
        }
    }

    @Transactional
    public ApiResponse<TaskResponse> update(Long id, UpdateTaskRequest request) {
        validate_room_and_employee(request);
        TaskEnttity task = taskRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Task not found with id: " + id));
        if (this.taskRepository.existsByIdAndDeletedDateIsNotNull(id)) {
            throw new RuntimeException("Task already deleted");
        }
        try {
            String username = SecurityContextHolder.getContext().getAuthentication().getName();
            task.setLastModifiedDate(LocalDateTime.now());
            task.setLastModifiedBy(username);
            task.setTaskStatus(TaskStatus.valueOf(request.getTaskStatus().toUpperCase()));
            task.setDescription(request.getDescription());
            task.setTaskType(TaskType.valueOf(request.getTaskType().toUpperCase()));
            return ApiResponse.success(TaskResponse.fromEntity(this.taskRepository.save(task)));
        } catch (Exception e) {
            // TODO: handle exception
            throw new RuntimeException("Failed to update task with id: " + id, e);
        }
    }

    @Transactional
    public ApiResponse<String> deleted(Long id) {
        if (taskRepository.existsByIdAndDeletedDateIsNotNull(id)) {
            throw new AlreadyExistException("Task already deleted with id: " + id);
        }
        TaskEnttity task = taskRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Task not found with id: " + id));
        try {

            String username = SecurityContextHolder.getContext().getAuthentication().getName();
            task.setDeletedDate(LocalDateTime.now());
            task.setTaskStatus(TaskStatus.CANCELLED);
            task.setDeletedBy(username);

            RoomEntity room = task.getRoom();
            if(room != null && room.getStatusRoom() != StatusRoom.AVAILABLE) {
                room.setStatusRoom(StatusRoom.AVAILABLE);
                roomRepository.save(room);
            }

            taskRepository.save(task);

            return ApiResponse.success("Task deleted successfully");
        } catch (Exception e) {
            throw new RuntimeException("Failed to delete task with id: " + id, e);
        }
    }

    // get all task by id of employee
    public ApiResponse<List<TaskResponse>> getTaskByEmployeeId(Long employeeId) {
        List<TaskEnttity> taskEnttity = this.taskRepository.findByEmployeeId(employeeId);
        if (taskEnttity.isEmpty()) {
            throw new NotFoundException("No tasks found for employee with id: " + employeeId);
        }
        try {

            return ApiResponse.success(toResponses(taskEnttity));
        } catch (Exception e) {
            // TODO: handle exception
            throw new RuntimeException("Failed to get task by id: " + employeeId, e);
        }
    }

    @Transactional(readOnly = true)
    public ApiResponse<TaskListResponse> getAll(int page, int size, boolean isPage) {
        try {
            List<TaskEnttity> taskEnttity;
            if (isPage) {
                Pageable pageable = PageRequest.of(Math.max(page, 0), size,
                        Sort.by(Sort.Direction.DESC, "createdDate"));
                taskEnttity = this.taskRepository.findAll(pageable).getContent();
            } else {
                Pageable pageable = PageRequest.of(0, Integer.MAX_VALUE, Sort.by(Sort.Direction.DESC, "createdDate"));
                taskEnttity = this.taskRepository.findAll(pageable).getContent();
            }
            return ApiResponse.success(toTaskListResponse(taskEnttity));
        } catch (Exception e) {

            throw new RuntimeException("Failed to fetch all tasks", e);
        }
    }

    @Transactional(readOnly = true)
    @Override
    public ApiResponse<TaskResponse> getById(Long id) {
        TaskEnttity taskEnttity = this.taskRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Task not found with id: " + id));
        try {
            return ApiResponse.success(TaskResponse.fromEntity(taskEnttity));
        } catch (Exception e) {

            throw new RuntimeException("Failed to fetch task with id: " + id, e);
        }
    }

    @Transactional
    @Override
    public ApiResponse<String> updateStatus(Long id) {
        TaskEnttity task = taskRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Task not found with id: " + id));
        try {
            if (task.getTaskStatus() == TaskStatus.PENDING) {
                task.setTaskStatus(TaskStatus.IN_PROGRESS);
            } else if (task.getTaskStatus() == TaskStatus.IN_PROGRESS) {
                task.setTaskStatus(TaskStatus.COMPLETED);
                RoomEntity roomEntity = task.getRoom();
                roomEntity.setStatusRoom(StatusRoom.ALREADY_CLEAN);
                roomRepository.save(roomEntity);
            }
            taskRepository.save(task);
            return ApiResponse.success("Task status updated successfully");
        } catch (Exception e) {
            throw new RuntimeException("Failed to update task status with id: " + id, e);
        }
    }

    private TaskEnttity findOne(Long id) {
        if (!taskRepository.existsByIdAndDeletedDateIsNotNull(id)) {
            throw new AlreadyExistException("Task is not deleted with id: " + id);
        }
        TaskEnttity task = taskRepository.findByIdAndDeletedDateIsNotNull(id);
        if (task == null) {
            throw new NotFoundException("Task not found with id: " + id);
        }
        try {
            return task;
        } catch (Exception e) {
            // TODO: handle exception
            throw new RuntimeException("Failed to fetch task with id: " + id, e);
        }
    }

    public ApiResponse<String> restore(Long id) {
        TaskEnttity task = this.findOne(id);
        try {
            task.setDeletedBy(null);
            task.setDeletedDate(null);
            this.taskRepository.save(task);
            return ApiResponse.success("Task restored successfully");
        } catch (Exception e) {
            throw new RuntimeException("Failed to restore task with id: " + id, e);
        }
    }

    public ApiResponse<List<TaskResponse>> list_deleted() {

        if (taskRepository.existsByDeletedByIsNotNull() && taskRepository.existsByDeletedDateIsNotNull()) {
            return ApiResponse.success(
                    taskRepository.findByDeletedByIsNotNullAndDeletedDateIsNotNull().stream()
                            .map(TaskResponse::fromEntity).collect(Collectors.toList()));
        } else if (taskRepository.existsByDeletedByIsNotNull()) {
            return ApiResponse.success(
                    taskRepository.findByDeletedByIsNotNull().stream()
                            .map(TaskResponse::fromEntity).collect(Collectors.toList()));
        } else if (taskRepository.existsByDeletedDateIsNotNull()) {
            return ApiResponse.success(
                    taskRepository.findByDeletedDateIsNotNull().stream()
                            .map(TaskResponse::fromEntity).collect(Collectors.toList()));
        } else {
            throw new NotFoundException("No deleted tasks found");
        }
    }

    @Transactional(readOnly = true)
    @Override
    public ApiResponse<TaskListResponse> search_filter(Map<String, String> allParams, String sort, String search,
            int page, int size, boolean isPage) {
        try {
            UpdateTaskRequest request = new UpdateTaskRequest();
            allParams.forEach((key, value) -> {
                try {
                    TaskGroupBy taskGroupBy = TaskGroupBy.valueOf(key.toUpperCase());
                    taskGroupBy.applyFilter(request, value);
                } catch (Exception e) {
                    key = key.toUpperCase();
                    switch (key) {
                        case "TASK_STATUS" ->
                            request.setTaskStatus(value);
                        case "TASK_TYPE" ->
                            request.setTaskType(value);
                        case "CREATED_DATE" ->
                            request.setCreateDated(value);
                        case "DESCRIPTION" ->
                            request.setDescription(value);
                    }
                }
            });
            List<Sort.Order> sortOrders = new ArrayList<>();
            if (sort != null && !sort.isEmpty()) {
                for (String s : sort.split(",")) {
                    String[] sortParams = s.split(":");

                    if (sortParams.length != 2) {
                        throw new RuntimeException("Invalid sort format like: id:desc");
                    }

                    String direction = sortParams[1].toUpperCase();
                    String field = sortParams[0];

                    sortOrders.add(
                            new Sort.Order(direction.equals("ASC") ? Sort.Direction.ASC : Sort.Direction.DESC, field));
                }
            }

            Specification<TaskEnttity> spec = (root, query, criteriaBuilder) -> {
                List<Predicate> predicates = new ArrayList<>();

                if (search != null && !search.isBlank()) {
                    List<Predicate> searchPredicates = new ArrayList<>();
                    String searchPattern = "%" + search.toLowerCase() + "%";

                    searchPredicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("description")), searchPattern));
                    searchPredicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("taskType").as(String.class)), searchPattern));
                    searchPredicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("taskStatus").as(String.class)), searchPattern));
                    predicates.add(criteriaBuilder.or(searchPredicates.toArray(new Predicate[0])));
                }

                if (request.getTaskStatus() != null && !request.getTaskStatus().isEmpty()) {
                    try {
                        TaskStatus status = TaskStatus.valueOf(request.getTaskStatus().toUpperCase().trim());
                        predicates.add(criteriaBuilder.equal(root.get("taskStatus"), status));
                    } catch (IllegalArgumentException iae) {
                        return criteriaBuilder.disjunction();
                    }
                }

                if (request.getTaskType() != null && !request.getTaskType().isEmpty()) {
                    try {
                        TaskType type = TaskType.valueOf(request.getTaskType().toUpperCase().trim());
                        predicates.add(criteriaBuilder.equal(root.get("taskType"), type));
                    } catch (IllegalArgumentException iae) {
                        return criteriaBuilder.disjunction();
                    }
                }

                if (request.getCreateDated() != null && !request.getCreateDated().isBlank()) {
                    DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                    LocalDate createdDate = LocalDate.parse(request.getCreateDated().trim(), dateTimeFormatter);
                    predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("createdDate"), createdDate.atStartOfDay()));
                    predicates.add(criteriaBuilder.lessThan(root.get("createdDate"), createdDate.plusDays(1).atStartOfDay()));
                }

                if (request.getDescription() != null && !request.getDescription().isEmpty()) {
                    predicates.add(criteriaBuilder.like(root.get("description"), "%" + request.getDescription() + "%"));
                }

                return predicates.isEmpty() ? criteriaBuilder.conjunction() : criteriaBuilder.and(predicates.toArray(new Predicate[0]));
            };

            Pageable pageable;
            if (isPage) {
                pageable = PageRequest.of(page - 1, size, Sort.by(sortOrders));
            } else {
                pageable = PageRequest.of(0, Integer.MAX_VALUE, Sort.by(sortOrders));
            }

            List<TaskEnttity> entities = taskRepository.findAll(spec, pageable).getContent();

            return ApiResponse.success(this.toTaskListResponse(entities));
        } catch (Exception e) {
            throw e;
        }
    }

    public List<TaskResponse> toResponses(List<TaskEnttity> entities) {
        try {
            List<TaskResponse> responses = new ArrayList<>();
            for (TaskEnttity taskEnttity : entities) {
                responses.add(TaskResponse.fromEntity(taskEnttity));
            }
            return responses;
        } catch (Exception e) {
            throw new RuntimeException("Failed to convert TaskEnttity list to TaskResponse list", e);
        }
    }

    // private List<TaskGroupBy> filter_null(TaskGroupBy... groupBy) {
    // try {
    // return
    // Arrays.stream(groupBy).filter(Objects::nonNull).collect(Collectors.toList());
    // } catch (Exception e) {
    // // TODO: handle exception
    // throw new RuntimeException("Failed to filter null TaskGroupBy values: " +
    // groupBy, e);
    // }
    // }

    // private Map<String, Object> group( List<TaskGroupBy> groupBy, TaskRequest
    // request) {
    // try {
    // List<TaskResponse> responses = this.search_filter(request);
    // return groupReclusive(groupBy, responses, 0);
    // } catch (Exception e) {
    // // TODO: handle exception
    // throw new RuntimeException("Failed to group tasks by: " + groupBy, e);
    // }
    // }

    // private Map<String, Object> groupReclusive(List<TaskGroupBy> groupBy,
    // List<TaskResponse> responses, int level){ {
    // try {
    // TaskGroupBy currentField = groupBy.get(level);
    // boolean lastlevel = level == groupBy.size() -1;

    // Map<String, List<TaskResponse>> grouped = responses.stream().collect(
    // Collectors.groupingBy(dto -> currentField.extract(dto),
    // LinkedHashMap::new, Collectors.toList()));

    // Map<String, Object> result = new LinkedHashMap<>();
    // grouped.forEach((key, value) -> result.put(key, lastlevel? value :
    // groupReclusive(groupBy, value, level + 1)));

    // return result;

    // } catch (Exception e) {
    // throw new RuntimeException("Failed to group tasks recursively by: " +
    // groupBy, e);
    // }
    // }
    // }

    private TaskListResponse toTaskListResponse(List<TaskEnttity> entities) {
        try {
            TaskRepository taskRepository = this.taskRepository; // Assuming you have access to the repository here

            int totalTasks = (int) taskRepository.count();
            int totalInProgress = taskRepository.countByTaskStatus(TaskStatus.IN_PROGRESS);
            int totalCompleted = taskRepository.countByTaskStatus(TaskStatus.COMPLETED);
            int totalPending = taskRepository.countByTaskStatus(TaskStatus.PENDING);
            int totalCancelled = taskRepository.countByTaskStatus(TaskStatus.CANCELLED);

            return TaskListResponse.builder()
                    .totalTasks(totalTasks)
                    .totalInProgress(totalInProgress)
                    .totalCompleted(totalCompleted)
                    .totalPending(totalPending)
                    .totalCancelled(totalCancelled)
                    .tasks(entities.stream().map(TaskResponse::fromEntity).collect(Collectors.toList()))
                    .build();
        } catch (Exception e) {
            throw new RuntimeException("Failed to convert TaskEnttity list to TaskListResponse", e);
        }
    }
}