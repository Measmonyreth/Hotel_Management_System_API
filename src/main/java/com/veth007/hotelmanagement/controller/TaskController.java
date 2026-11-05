package com.veth007.hotelmanagement.controller;

import java.util.List;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.veth007.hotelmanagement.config.HashIdUtil;
import com.veth007.hotelmanagement.exception.ApiResponse;
import com.veth007.hotelmanagement.model.request.Task.TaskRequest;
import com.veth007.hotelmanagement.model.request.Task.UpdateTaskRequest;
import com.veth007.hotelmanagement.model.response.Task.TaskListResponse;
import com.veth007.hotelmanagement.model.response.Task.TaskResponse;
import com.veth007.hotelmanagement.service.Task.TaskService;
import org.springframework.web.bind.annotation.RequestParam;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RequestMapping("/api/tasks")
@RestController
@RequiredArgsConstructor
public class TaskController {
        private final TaskService taskService;

        @PreAuthorize("isAuthenticated() and (hasAuthority('ADMIN') or hasAuthority('MANAGER'))")
        @PostMapping("/create")
        public ResponseEntity<ApiResponse<?>> createTask(@Valid @RequestBody TaskRequest request) {

                return ResponseEntity.ok(taskService.createTask(request));

        }

        @PreAuthorize("isAuthenticated() and (hasAuthority('ADMIN') or hasAuthority('MANAGER'))")
        @PutMapping("/update/{id}")
        public ResponseEntity<ApiResponse<TaskResponse>> updateTask(@Valid @RequestBody UpdateTaskRequest request,
                        @PathVariable Long id) {

                return ResponseEntity.ok(taskService.update(id, request));
        }

        @PreAuthorize("isAuthenticated() and (hasAuthority('ADMIN') or hasAuthority('MANAGER'))")
        @DeleteMapping("/delete/{id}")
        public ResponseEntity<ApiResponse<String>> deleteTask(@PathVariable Long id) {

                return ResponseEntity.ok(taskService.deleted(id));

        }

        @PreAuthorize("isAuthenticated() and (hasAuthority('ADMIN') or hasAuthority('MANAGER'))")
        @GetMapping("/getbyemployeeid/{id}")
        public ResponseEntity<ApiResponse<List<TaskResponse>>> getTaskByEmployeeId(@PathVariable Long id) {
                return ResponseEntity.ok(taskService.getTaskByEmployeeId(id));
        }

        @PreAuthorize("isAuthenticated() and (hasAuthority('ADMIN') or hasAuthority('MANAGER'))")
        @GetMapping("/getall")
        public ResponseEntity<ApiResponse<TaskListResponse>> getAllTasks(
                        @RequestParam(name = "page", defaultValue = "0") int page,
                        @RequestParam(name = "size", defaultValue = "3") int size,
                        @RequestParam(name = "isPage", defaultValue = "false") boolean isPage) {

                return ResponseEntity.ok(taskService.getAll(page, size, isPage));

        }

        @PreAuthorize("isAuthenticated() and (hasAuthority('ADMIN') or hasAuthority('MANAGER'))")
        @GetMapping("/getbyid/{id}")
        public ResponseEntity<ApiResponse<TaskResponse>> getById(@PathVariable Long id) {

                return ResponseEntity.ok(taskService.getById(id));

        }

        @PreAuthorize("isAuthenticated() and (hasAuthority('ADMIN') or hasAuthority('STAFF') or hasAuthority('MANAGER'))")
        @PutMapping("/updateStatus/{id}")
        public ResponseEntity<ApiResponse<String>> updateStatus(
                        @PathVariable Long id) {

                return ResponseEntity.ok(taskService.updateStatus(id));
        }

        @PreAuthorize("isAuthenticated() and (hasAuthority('ADMIN') or hasAuthority('MANAGER'))")
        @GetMapping("/getalldeleted")
        public ResponseEntity<ApiResponse<List<TaskResponse>>> getAllDeletedTasks() {
                return ResponseEntity.ok(taskService.list_deleted());

        }

        @PreAuthorize("isAuthenticated() and (hasAuthority('ADMIN') or hasAuthority('MANAGER'))")
        @PostMapping("/restore/{id}")
        public ResponseEntity<ApiResponse<String>> restoreTask(@PathVariable Long id) {
                return ResponseEntity.ok(taskService.restore(id));

        }

        @PreAuthorize("isAuthenticated() and (hasAuthority('ADMIN') or hasAuthority('MANAGER'))")
        @GetMapping("/search")
        public ResponseEntity<ApiResponse<TaskListResponse>> groupBy(
                        @RequestParam(required = false) Map<String, String> allParams,
                        @RequestParam(required = false, defaultValue = "") String search,
                        @RequestParam(required = false, defaultValue = "id:asc") String sort,
                        @RequestParam(name = "page", defaultValue = "0") int page,
                        @RequestParam(name = "size", defaultValue = "3") int size,
                        @RequestParam(name = "isPage", defaultValue = "false") boolean isPage) {

                return ResponseEntity.ok(taskService.search_filter(allParams, sort, search, page, size, isPage));
        }
}
