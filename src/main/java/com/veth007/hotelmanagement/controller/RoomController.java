package com.veth007.hotelmanagement.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.veth007.hotelmanagement.config.HashIdUtil;
import com.veth007.hotelmanagement.exception.ApiResponse;
import com.veth007.hotelmanagement.model.request.RoomRequest.RoomRequest;
import com.veth007.hotelmanagement.model.request.RoomRequest.UpdateRoomRequest;
import com.veth007.hotelmanagement.model.response.Room.RoomListResponse;
import com.veth007.hotelmanagement.model.response.Room.RoomResponse;
import com.veth007.hotelmanagement.model.response.ValidationResponse;
import com.veth007.hotelmanagement.service.Room.RoomService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/room")
@RequiredArgsConstructor
public class RoomController {
    private final RoomService roomService;

    @PreAuthorize("isAuthenticated() and (hasAuthority('ADMIN') or hasAuthority('STAFF') or hasAuthority('MANAGER'))")
    @GetMapping("/validate/name")
    public ResponseEntity<ApiResponse<ValidationResponse>> validateName(@RequestParam String name) {
        return ResponseEntity.ok(roomService.isRoomNameAvailable(name));
    }

    @PreAuthorize("isAuthenticated() and (hasAuthority('ADMIN') or hasAuthority('STAFF') or hasAuthority('MANAGER'))")
    @GetMapping("/validate/number")
    public ResponseEntity<ApiResponse<ValidationResponse>> validateNumber(@RequestParam String number) {
        return ResponseEntity.ok(roomService.isRoomNumberAvailable(number));
    }

    @PreAuthorize("isAuthenticated() and (hasAuthority('ADMIN') or hasAuthority('STAFF') or hasAuthority('MANAGER'))")
    @GetMapping("/validate/available")
    public ResponseEntity<ApiResponse<ValidationResponse>> validateAvailable(@RequestParam String number) {
        return ResponseEntity.ok(roomService.isRoomAvailable(number));
    }
    // private final RoomImageService roomImageService;
    // private static final String FOLDER_PATH = "uploads/room_images";

    // @GetMapping("/room/{roomId}/images/{filename}")
    // public ResponseEntity<Resource> getImage(
    // @PathVariable Long roomId,
    // @PathVariable String filename) throws IOException {

    // if (roomId == null || roomId <= 0 || filename == null || filename.isBlank())
    // {
    // return ResponseEntity.badRequest().build();
    // }

    // // Prevent path traversal attacks
    // if (filename.contains("..") || filename.contains("/") ||
    // filename.contains("\\")) {
    // return ResponseEntity.badRequest().build();
    // }

    // Path basePath = Paths.get(FOLDER_PATH, roomId.toString()).toAbsolutePath();
    // Path filePath = basePath.resolve(filename).toAbsolutePath();

    // // Ensure the resolved path is within the base directory
    // if (!filePath.startsWith(basePath)) {
    // return ResponseEntity.status(403).build();
    // }

    // Resource resource = new UrlResource(filePath.toUri());

    // if (!resource.exists() || !resource.isReadable()) {
    // return ResponseEntity.notFound().build();
    // }

    // String contentType = Files.probeContentType(filePath);
    // if (contentType == null)
    // contentType = "application/octet-stream";

    // return ResponseEntity.ok()
    // .contentType(MediaType.parseMediaType(contentType))
    // .body(resource);
    // }

    @PreAuthorize("isAuthenticated() and (hasAuthority('ADMIN') or hasAuthority('MANAGER'))")
    @PostMapping(value = "/create", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<RoomResponse>> create(
            @Valid @ModelAttribute RoomRequest request,
            @RequestParam(value = "file", required = false) MultipartFile file) {
        return ResponseEntity.ok(this.roomService.createRoom(request, file));
    }

    @GetMapping("/getroom/{id}")
    public ResponseEntity<ApiResponse<RoomResponse>> getById(@PathVariable String id) {
        return ResponseEntity.ok(this.roomService.getRoomById(HashIdUtil.resolveId(id)));

    }

    @PreAuthorize("isAuthenticated() and (hasAuthority('ADMIN') or hasAuthority('MANAGER'))")
    @DeleteMapping("/deleteroom/{id}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> deletedById(@PathVariable String id) {

        this.roomService.deleteRoomById(HashIdUtil.resolveId(id));
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Room deleted successfully!");
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/all")
    public ResponseEntity<ApiResponse<RoomListResponse>> findAll(
            @RequestParam(required = false) String sort,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "3") int size,
            @RequestParam(name = "isPage", defaultValue = "false") boolean isPage) {
        try {
            return ResponseEntity.ok(this.roomService.getAllRooms(sort, page, size, isPage));
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("message", "Failed to find all rooms: " + e.getMessage());
            error.put("status", 500);
            return ResponseEntity.status(500)
                    .body(ApiResponse.error("Failed to find all rooms: " + e.getMessage(), 500));
        }
    }

    @PreAuthorize("isAuthenticated() and (hasAuthority('ADMIN') or hasAuthority('MANAGER'))")
    @PutMapping(value = "/updateroom/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<RoomResponse>> update(
            @PathVariable Long id,
            @ModelAttribute UpdateRoomRequest request,
            @RequestParam(name = "file", required = false) MultipartFile file) {
        try {
            return ResponseEntity.ok(this.roomService.updateRoom(id, request, file));
        } catch (Exception e) {
            e.printStackTrace(); // check your console/logs
            Map<String, Object> error = new HashMap<>();
            error.put("message", e.getCause() != null ? e.getCause().getMessage() : e.getMessage());
            error.put("status", 500);
            return ResponseEntity.status(500).body(ApiResponse.error("Failed to update room: " + e.getMessage(), 500));
        }
    }

    @PreAuthorize("isAuthenticated() and (hasAuthority('ADMIN') or hasAuthority('STAFF') or hasAuthority('MANAGER'))")
    @GetMapping("/list")
    public ResponseEntity<ApiResponse<RoomListResponse>> list(
            @RequestParam(required = false) Map<String, String> allParams,
            @RequestParam(required = false, defaultValue = "") String search,
            @RequestParam(required = false) String sort,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "3") int size,
            @RequestParam(name = "isPage", defaultValue = "false") boolean isPage

    ) {

        try {
            return ResponseEntity.ok(this.roomService.search(allParams, sort, search, page, size, isPage));
        } catch (Exception e) {

            return ResponseEntity.status(500).body(ApiResponse.error("Failed to list rooms:", 500));
        }
    }

    @PreAuthorize("isAuthenticated() and (hasAuthority('ADMIN') or hasAuthority('MANAGER'))")
    @GetMapping("/deleted")
    @Transactional(readOnly = true)
    public ResponseEntity<ApiResponse<List<RoomResponse>>> listDeleted() {
        try {
            // Convert to response - floor will be loaded due to JOIN FETCH
            return ResponseEntity.ok(this.roomService.getAllDeletedRooms("id"));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(ApiResponse.error("Failed to list deleted rooms:", 500));
        }
    }

    @PreAuthorize("isAuthenticated() and (hasAuthority('ADMIN') or hasAuthority('MANAGER'))")
    @PostMapping("/restore/{id}")
    public ResponseEntity<ApiResponse<String>> restore(@PathVariable String id) {

        return ResponseEntity.ok(this.roomService.restore(HashIdUtil.resolveId(id)));

    }
}
