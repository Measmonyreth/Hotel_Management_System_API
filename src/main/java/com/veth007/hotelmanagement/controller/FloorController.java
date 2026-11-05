package com.veth007.hotelmanagement.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.veth007.hotelmanagement.config.HashIdUtil;
import com.veth007.hotelmanagement.model.request.FloorRequest.FloorRequest;
import com.veth007.hotelmanagement.model.request.FloorRequest.UpdateFloorRequest;
import com.veth007.hotelmanagement.model.response.Floor.FloorListResponse;
import com.veth007.hotelmanagement.model.response.Floor.FloorResponse;
import com.veth007.hotelmanagement.model.response.ValidationResponse;
import com.veth007.hotelmanagement.service.Floor.FloorService;
import com.veth007.hotelmanagement.exception.ApiResponse;

import org.springframework.web.bind.annotation.RequestBody;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/floors")
@RequiredArgsConstructor
public class FloorController {
   private final FloorService floorService;

   @GetMapping("/validate/name")
   public ResponseEntity<ApiResponse<ValidationResponse>> validateName(
         @RequestParam String name) {
      return ResponseEntity.ok(floorService.isFloorNameAvailable(name));
   }

   @GetMapping("/validate/number")
   public ResponseEntity<ApiResponse<ValidationResponse>> validateNumber(
         @RequestParam String number) {
      return ResponseEntity.ok(floorService.isFloorNumberAvailable(number));
   }

    @PreAuthorize(" isAuthenticated() and (hasAuthority('ADMIN') or hasAuthority('MANAGER'))")
   @PostMapping("/create")
   public ResponseEntity<ApiResponse<FloorResponse>> createFloor(@Valid @RequestBody FloorRequest floorRequest) {

      return ResponseEntity.ok(this.floorService.createFloor(floorRequest));

   }

   @PreAuthorize(" isAuthenticated() and (hasAuthority('ADMIN') or hasAuthority('MANAGER'))")
   @GetMapping("/getfloorbyid/{id}")
   public ResponseEntity<ApiResponse<FloorResponse>> getFloorById(@PathVariable Long id) {
      return ResponseEntity.ok(this.floorService.getFloorById(id));
   }

   @PreAuthorize(" isAuthenticated() and (hasAuthority('ADMIN') or hasAuthority('STAFF') or hasAuthority('MANAGER'))")
   @GetMapping({ "/getallfloors", "/auth/getallfloors" })
   public ResponseEntity<ApiResponse<FloorListResponse>> getAllFloors(
         @RequestParam(name = "page", defaultValue = "0") int page,
         @RequestParam(name = "size", defaultValue = "3") int size,
         @RequestParam(name = "isPage", defaultValue = "false") boolean isPage) {
      return ResponseEntity.ok(this.floorService.getAllFloor(page, size, isPage));

   }

   @PreAuthorize(" isAuthenticated() and (hasAuthority('ADMIN') or hasAuthority('MANAGER'))")
   @PostMapping("/update/{id}")
   public ResponseEntity<ApiResponse<FloorResponse>> updateFloor(@PathVariable Long id,
         @Valid @RequestBody UpdateFloorRequest floorRequest) {
      return ResponseEntity.ok(this.floorService.update(id, floorRequest));

   }

   @PreAuthorize("(hasAuthority('ADMIN') or hasAuthority('MANAGER'))")
   @DeleteMapping("/delete/{id}")
   public ResponseEntity<ApiResponse<String>> deleteFloor(@PathVariable Long id) {
      try {
         return ResponseEntity.ok(this.floorService.delete(id));
      } catch (Exception e) {
         Map<String, String> error = new HashMap<>();
         error.put("message", e.getMessage());
         return ResponseEntity.badRequest().body(ApiResponse.error(error.get("message"), 400));
      }
   }

   @PreAuthorize(" isAuthenticated() and (hasAuthority('ADMIN') or hasAuthority('STAFF') or hasAuthority('MANAGER'))")
   @GetMapping("/list_deleted")
   public ResponseEntity<ApiResponse<List<FloorResponse>>> listDeleted() {
      try {
         return ResponseEntity.ok(this.floorService.list_deleted());
      } catch (Exception e) {
         Map<String, String> error = new HashMap<>();
         error.put("message", e.getMessage());
         return ResponseEntity.badRequest().body(ApiResponse.error(error.get("message"), 400));
      }
   }

   @PreAuthorize("(hasAuthority('ADMIN') or hasAuthority('MANAGER'))")
   @PostMapping("/restore/{id}")
   public ResponseEntity<ApiResponse<String>> restore(@PathVariable Long id) {
      try {
         return ResponseEntity.ok(this.floorService.restore(id));
      } catch (Exception e) {
         Map<String, String> error = new HashMap<>();
         error.put("message", e.getMessage());
         return ResponseEntity.badRequest().body(ApiResponse.error(error.get("message"), 400));
      }
   }

   @PreAuthorize(" isAuthenticated() and (hasAuthority('ADMIN') or hasAuthority('STAFF') or hasAuthority('MANAGER'))")
   @GetMapping("/list_group")
   public ResponseEntity<ApiResponse<FloorListResponse>> listGroup(
         @RequestParam(required = false) Map<String, String> allParams,
         @RequestParam(required = false, defaultValue = "") String search,
         @RequestParam(required = false) String sort,
         @RequestParam(name = "page", defaultValue = "0") int page,
         @RequestParam(name = "size", defaultValue = "3") int size,
         @RequestParam(name = "isPage", defaultValue = "false") boolean isPage) {
      return ResponseEntity.ok(this.floorService.search_floor(allParams, sort, search, page, size, isPage));
   }
}
