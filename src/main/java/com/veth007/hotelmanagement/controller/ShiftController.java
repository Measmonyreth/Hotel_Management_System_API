package com.veth007.hotelmanagement.controller;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.veth007.hotelmanagement.config.HashIdUtil;
import com.veth007.hotelmanagement.exception.ApiResponse;
import com.veth007.hotelmanagement.model.request.Shift.ShiftRequest;
import com.veth007.hotelmanagement.model.response.Schedule.ShiftListResponse;
import com.veth007.hotelmanagement.model.response.Schedule.ShiftResponse;
import com.veth007.hotelmanagement.service.Shift.ShiftService;
import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
@RequestMapping("/api/shift")
public class ShiftController {
    private final ShiftService shiftService;

    // @PreAuthorize("hasAuthority('ADMIN')")
    @PostMapping("/create")
    public ResponseEntity<ApiResponse<ShiftResponse>> createShift(@RequestBody ShiftRequest request) {
        ApiResponse<ShiftResponse> response = this.shiftService.createShift(request);
        try {
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            // TODO: handle exception
            throw new RuntimeException("Failed to create shift", e);
        }
    }

    // @PreAuthorize("hasAuthority('ADMIN')")
    @GetMapping("/getall")
    public ResponseEntity<ApiResponse<List<ShiftListResponse>>> getAll(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "3") int size,
            @RequestParam(name = "isPage", defaultValue = "false") boolean isPage) {
        return ResponseEntity.ok(
                this.shiftService.getAllShift(page, size, isPage));
    }

    // @PreAuthorize("hasAuthority('ADMIN')")
    @PostMapping("/update/{id}")
    public ResponseEntity<ApiResponse<ShiftResponse>> updateShift(@PathVariable String id,
            @RequestBody ShiftRequest request) {
        return ResponseEntity.ok(this.shiftService.updateShift(HashIdUtil.resolveId(id), request));
    }

    @PreAuthorize("(hasAuthority('ADMIN') or hasAuthority('MANAGER'))")
    @GetMapping("/getbyid/{id}")
    public ResponseEntity<ApiResponse<ShiftResponse>> getById(@PathVariable String id) {
        return ResponseEntity.ok(this.shiftService.getShiftById(HashIdUtil.resolveId(id)));
    }

    @PreAuthorize("(hasAuthority('ADMIN') or hasAuthority('MANAGER'))")
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<ApiResponse<String>> deleteShift(@PathVariable String id) {

        return ResponseEntity.ok(this.shiftService.deleteShift(HashIdUtil.resolveId(id)));

    }

    @PreAuthorize("(hasAuthority('ADMIN') or hasAuthority('MANAGER'))")
    @GetMapping("/list_deleted")
    public ResponseEntity<ApiResponse<List<ShiftResponse>>> listDeleted() {
        return ResponseEntity.ok(this.shiftService.list_deleted());
    }

    @PreAuthorize("(hasAuthority('ADMIN') or hasAuthority('MANAGER'))")
    @PostMapping("/restore/{id}")
    public ResponseEntity<ApiResponse<String>> restoreShift(@PathVariable String id) {
        return ResponseEntity.ok(this.shiftService.restoreShift(HashIdUtil.resolveId(id)));
    }

    @PreAuthorize("(hasAuthority('ADMIN') or hasAuthority('MANAGER'))")
    @GetMapping("/list")
    public ResponseEntity<ApiResponse<ShiftListResponse>> list(
            @RequestParam(required = false) Map<String, String> allParams,
            @RequestParam(required = false) String sort,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "3") int size,
            @RequestParam(name = "isPage", defaultValue = "false") boolean isPage) {

        return ResponseEntity.ok(this.shiftService.searchShifts(allParams, sort, page, size, isPage));
    }
}
