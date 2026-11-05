package com.veth007.hotelmanagement.service.Room.Implement;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.veth007.hotelmanagement.constant.enums.RoomType;
import com.veth007.hotelmanagement.constant.enums.Status;
import com.veth007.hotelmanagement.constant.enums.StatusRoom;
import com.veth007.hotelmanagement.constant.enums.StatusFloor;
import com.veth007.hotelmanagement.exception.AlreadyExistException;
import com.veth007.hotelmanagement.exception.ApiResponse;
import com.veth007.hotelmanagement.exception.NotFoundException;
import com.veth007.hotelmanagement.model.entity.FloorEntity;
import com.veth007.hotelmanagement.model.entity.Room.RoomEntity;
import com.veth007.hotelmanagement.model.entity.Room.RoomImageEntity;
import com.veth007.hotelmanagement.model.request.RoomRequest.RoomRequest;
import com.veth007.hotelmanagement.model.request.RoomRequest.UpdateRoomRequest;
import com.veth007.hotelmanagement.model.response.Room.RoomListResponse;
import com.veth007.hotelmanagement.model.response.Room.RoomResponse;
import com.veth007.hotelmanagement.model.response.ValidationResponse;
import com.veth007.hotelmanagement.repository.FloorRepository;
import com.veth007.hotelmanagement.repository.RoomRepository;
import com.veth007.hotelmanagement.service.Room.RoomService;

import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RoomServiceImp implements RoomService {
    private final RoomRepository roomRepository;
    private final FloorRepository floorRepository;

    private final RoomImageService roomImageService;

    @Override
    public ApiResponse<ValidationResponse> isRoomNameAvailable(String roomName) {
        String normalizedName = roomName.trim();
        boolean exists = roomRepository.existsByRoomNameIgnoreCase(normalizedName);
        return ApiResponse.ok(
                exists ? "Room name already exists" : "Room name is available",
                new ValidationResponse(!exists, exists ? "DUPLICATE" : "AVAILABLE", normalizedName));
    }

    @Override
    public ApiResponse<ValidationResponse> isRoomNumberAvailable(String roomNumber) {
        String normalizedNumber = roomNumber.trim();
        boolean exists = roomRepository.existsByRoomNumber(normalizedNumber);
        return ApiResponse.ok(
                exists ? "Room number already exists" : "Room number is available",
                new ValidationResponse(!exists, exists ? "DUPLICATE" : "AVAILABLE", normalizedNumber));
    }

    @Override
    public ApiResponse<ValidationResponse> isRoomAvailable(String roomNumber) {
        String normalizedNumber = roomNumber.trim();
        Optional<RoomEntity> room = roomRepository.findByRoomNumber(normalizedNumber);
        if (room.isEmpty()) {
            return ApiResponse.ok("Room not found",
                    new ValidationResponse(false, "NOT_FOUND", normalizedNumber));
        }

        RoomEntity roomEntity = room.get();
        FloorEntity floor = roomEntity.getFloor();
        if (floor == null) {
            return ApiResponse.ok("Room floor is not configured",
                    new ValidationResponse(false, "FLOOR_NOT_CONFIGURED", normalizedNumber));
        }

        boolean roomActive = roomEntity.getAction() == Status.ACTIVE;
        boolean roomAvailable = roomEntity.getStatusRoom() == StatusRoom.AVAILABLE;
        boolean floorOpen = floor.getStatusFloor() == StatusFloor.OPEN;
        boolean floorActive = floor.getAction() == Status.ACTIVE;
        boolean available = roomActive && roomAvailable && floorOpen && floorActive;

        String status = !roomAvailable ? roomEntity.getStatusRoom().name()
                : !roomActive ? roomEntity.getAction() != null ? roomEntity.getAction().name() : "INACTIVE"
                : !floorActive ? floor.getAction() != null ? floor.getAction().name() : "INACTIVE"
                : floor.getStatusFloor().name();

        return ApiResponse.ok(
                available ? "Room is available" : "Room is not available",
                new ValidationResponse(available, status, normalizedNumber));
    }

    @Override
    @Transactional
    public ApiResponse<RoomResponse> createRoom(RoomRequest request, MultipartFile file) {
        String roomName = request.getRoomName().trim();
        String roomNumber = request.getRoomNumber().trim();
        request.setRoomName(roomName);
        request.setRoomNumber(roomNumber);

        if (roomRepository.existsByRoomNameIgnoreCase(roomName)) {
            throw new AlreadyExistException("Name of room already exist");
        }
        if (roomRepository.existsByRoomNumber(request.getRoomNumber())) {
            throw new AlreadyExistException("Room number already exist");
        }
        FloorEntity floor = floorRepository.findById(request.getFloorId())
                .orElseThrow(() -> new RuntimeException("Floor not found with id: " + request.getFloorId()));
        if (floorRepository.existsByIdAndDeletedByIsNotNull(floor.getId())) {
            throw new RuntimeException("Floor is deleted with id: " + request.getFloorId());
        }

        try {

            BigDecimal discount_price = BigDecimal.valueOf((request.getDiscountPercentage() * request.getPrice()) / 100)
                    .setScale(2, RoundingMode.HALF_UP);

            RoomEntity roomEntity = RoomEntity.builder()
                    .roomName(request.getRoomName())
                    .roomNumber(request.getRoomNumber())
                    .description(request.getDescription())
                    .statusRoom(StatusRoom.AVAILABLE)
                    .type(RoomType.valueOf(request.getRoomType().toUpperCase()))
                    .discountPercentage(request.getDiscountPercentage())
                    .rating(request.getRating())
                    .discountPrice(discount_price)
                    .price(BigDecimal.valueOf(request.getPrice()).setScale(2, RoundingMode.HALF_UP))
                    .action(Status.ACTIVE)
                    .floor(floor)
                    .build();

            // Save room first to generate ID
            roomEntity = roomRepository.save(roomEntity);

            // save image and add to room's images collection so DTO mapping includes it

            if (file != null && !file.isEmpty()) {
                RoomImageEntity savedImage = roomImageService.upload_image(roomEntity.getId(), file);
                if (savedImage != null) {
                    if (roomEntity.getImages() == null) {
                        roomEntity.setImages(new ArrayList<>());
                    }
                    roomEntity.getImages().add(savedImage);
                }
            }

            return ApiResponse.success(RoomResponse.fromEntity(roomEntity));
        } catch (AlreadyExistException e) {
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to create room", e);
        }
    }

    public RoomEntity findOne(Long id) throws NotFoundException {

        RoomEntity roomEntity = this.roomRepository.findByIdAndDeletedDateIsNull(id)
                .orElseThrow(() -> new NotFoundException("Room not found"));

        return roomEntity;
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<RoomResponse> getRoomById(Long id) {
        RoomEntity roomEntity = roomRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Room not found with id: " + id));
        try {

            return ApiResponse.success(RoomResponse.fromEntity(roomEntity));
        } catch (Exception e) {
            // TODO: handle exception
            throw new RuntimeException("Failed to get room by id: " + id, e);
        }
    }

    @Override
    @Transactional
    public ApiResponse<String> deleteRoomById(Long id) throws NotFoundException {
        if (this.roomRepository.existsByIdAndDeletedDateIsNotNull(id)) {
            throw new AlreadyExistException("Room already deleted");
        }
        RoomEntity roomEntity = this.findOne(id);

        try {
            String username = SecurityContextHolder.getContext().getAuthentication().getName();
            roomEntity.setDeletedBy(username);
            roomEntity.setDeletedDate(LocalDateTime.now());
            roomEntity.setAction(Status.DELETED);
            roomEntity.setStatusRoom(StatusRoom.DELETED);
            this.roomRepository.save(roomEntity);
            return ApiResponse.success("Room deleted successfully!");
        } catch (Exception e) {
            throw new RuntimeException("Failed to delete room by id: " + id, e);
        }
    }

    @Override
    public ApiResponse<String> restore(Long id) throws NotFoundException {
        if (roomRepository.existsByIdAndDeletedByIsNull(id)) {

            throw new AlreadyExistException("Room not deleted with id: " + id);

        }
        RoomEntity roomEntity = this.roomRepository.findByIdAndDeletedDateIsNotNull(id)
                .orElseThrow(() -> new NotFoundException("Room not found"));

        // validate floor is not deleted
        FloorEntity floorEntity = roomEntity.getFloor();
        if (floorEntity.getAction() == Status.DELETED) {
            throw new RuntimeException("Cannot restore room in a deleted floor");
        }

        try {
            roomEntity.setAction(Status.ACTIVE);
            roomEntity.setDeletedBy(null);
            roomEntity.setDeletedDate(null);
            roomEntity.setStatusRoom(StatusRoom.AVAILABLE);
            this.roomRepository.save(roomEntity);
            return ApiResponse.success("Room restored successfully!");
        } catch (Exception e) {
            // TODO: handle exception
            throw new RuntimeException("Failed to restore room by id: " + id, e);
        }
    }

    // public Map<RoomType, List<RoomEntity>> findall(String sort) {
    // try {
    // List<Sort.Order> sortlist = new ArrayList<>();
    // if (sort != null && !sort.isBlank()) {
    // for (String s : sort.split(",")) {
    // String[] str = s.split(":");

    // if (str.length != 2) {
    // throw new RuntimeException("Invalid sort format: " + s);
    // }

    // String direction = str[1].toUpperCase();
    // String field = str[0];

    // sortlist.add(
    // new Sort.Order(direction.equals("ASC") ? Sort.Direction.ASC :
    // Sort.Direction.DESC, field));

    // }
    // }
    // return
    // this.roomRepository.findAll(Sort.by(sortlist)).stream().collect(Collectors
    // .groupingBy(RoomEntity::getType, () -> new EnumMap<>(RoomType.class),
    // Collectors.toList()));

    // } catch (Exception e) {
    // throw new RuntimeException("Failed to find all rooms", e);
    // }
    // }

    @Override
    public ApiResponse<RoomListResponse> getAllRooms(String sort, int page, int size, boolean isPage) {
        try {
            List<Sort.Order> sortlist = new ArrayList<>();
            if (sort != null && !sort.isBlank()) {
                for (String s : sort.split(",")) {
                    String[] str = s.split(":");

                    if (str.length != 2) {
                        throw new RuntimeException("Invalid sort format: " + s);
                    }

                    String direction = str[1].toUpperCase();
                    String field = str[0];

                    sortlist.add(
                            new Sort.Order(direction.equals("ASC") ? Sort.Direction.ASC : Sort.Direction.DESC, field));

                }
            }
            return ApiResponse.success(toRoomListResponses(this.roomRepository.findAll(Sort.by(sortlist))));

        } catch (Exception e) {
            throw new RuntimeException("Failed to find all rooms", e);
        }
    }

    @Override
    public ApiResponse<RoomResponse> updateRoom(Long id, UpdateRoomRequest request, MultipartFile file) {
        if (!roomRepository.existsByIdAndAction(id, Status.ACTIVE)) {
            throw new AlreadyExistException("Room already deleted or disabled with id: " + id);
        }

        RoomEntity room = this.roomRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Room not found with id: " + id));
        try {

            // validate of provice is mustn't < 0
            if (request.getPrice() != null && request.getPrice() <= 0) {
                throw new RuntimeException("Price must be greater than 0");
            }

            String username = SecurityContextHolder.getContext().getAuthentication().getName();

            // Calculate discount price only if both percentage and price are available
            BigDecimal discount_price = BigDecimal.ZERO;
            if (request.getDiscountPercentage() != null && request.getPrice() != null && request.getPrice() > 0) {
                discount_price = BigDecimal.valueOf((request.getDiscountPercentage() * request.getPrice()) / 100)
                        .setScale(2, RoundingMode.HALF_UP);
            } else if (request.getDiscountPercentage() != null && request.getPrice() == null
                    && room.getPrice() != null) {
                // Use existing price if new price is not provided
                discount_price = (BigDecimal.valueOf(request.getDiscountPercentage()).multiply(room.getPrice()))
                        .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            } else {
                // Use existing discount price
                discount_price = room.getDiscountPrice() != null ? room.getDiscountPrice() : BigDecimal.ZERO;
            }

            room.setRoomName(request.getRoomName() != null ? request.getRoomName() : room.getRoomName());
            room.setRoomNumber(request.getRommNumber() != null ? request.getRommNumber() : room.getRoomNumber());
            room.setDescription(request.getDescription() != null ? request.getDescription() : room.getDescription());
            room.setStatusRoom(
                    request.getStatusRoom() != null ? StatusRoom.valueOf(request.getStatusRoom().toUpperCase())
                            : room.getStatusRoom());
            room.setType(request.getRoomType() != null ? RoomType.valueOf(request.getRoomType().toUpperCase())
                    : room.getType());
            room.setDiscountPercentage(
                    request.getDiscountPercentage() != null ? request.getDiscountPercentage()
                            : room.getDiscountPercentage());
            room.setRating(request.getRating() != null ? request.getRating() : room.getRating());
            room.setDiscountPrice(request.getDiscountPercentage() != null ? discount_price : room.getDiscountPrice());
            room.setAction(
                    request.getAction() != null ? Status.valueOf(request.getAction().toUpperCase()) : room.getAction());
            room.setPrice(request.getPrice() != null && request.getPrice() > 0
                    ? BigDecimal.valueOf(request.getPrice()).setScale(2, RoundingMode.HALF_UP)
                    : room.getPrice());
            room.setLastModifiedBy(username);
            room.setLastModifiedDate(LocalDateTime.now());

            RoomEntity updatedRoom = this.roomRepository.save(room);

            // Handle file upload if provided
            if (file != null && !file.isEmpty()) {
                this.roomImageService.upload_image(updatedRoom.getId(), file);
            }

            return ApiResponse.success(RoomResponse.fromEntity(updatedRoom));
        } catch (Exception e) {
            throw new RuntimeException("Failed to update room", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<List<RoomResponse>> getAllDeletedRooms(String sort) {
        try {
            if (roomRepository.existsByDeletedByIsNotNull() && roomRepository.existsByDeletedDateIsNotNull()) {
                return ApiResponse.success(roomRepository.findByDeletedByIsNotNull().stream()
                        .map(RoomResponse::fromEntity).collect(Collectors.toList()));
            }
            return ApiResponse.success(Collections.emptyList());
        } catch (Exception e) {
            return ApiResponse.error("Failed to retrieve deleted rooms", 500);
        }
    }

    @Override
    // search with with status room, type, action
    public ApiResponse<RoomListResponse> search(Map<String, String> allParams, String sort, String search, int page,
            int size, boolean isPage) {
        UpdateRoomRequest request = new UpdateRoomRequest();
        allParams.forEach((key, value) -> {
            try {
                RoomGroupBy groupBy = RoomGroupBy.valueOf(key.toUpperCase());
                groupBy.applyFilter(request, value);
            } catch (IllegalArgumentException e) {
                switch (key.toLowerCase()) {
                    case "status_room" ->
                        request.setStatusRoom(value);
                    case "room_type" ->
                        request.setRoomType(value);
                    case "action" ->
                        request.setAction(value);
                    default -> {

                    }
                }
            }
        });
        List<Sort.Order> sortlist = new ArrayList<>();
        if (sort != null && !sort.isBlank()) {
            for (String s : sort.split(",")) {
                String[] str = s.split(":");

                if (str.length != 2) {
                    throw new RuntimeException("Invalid sort format: " + s);
                }

                String direction = str[1].toUpperCase();
                String field = str[0];

                sortlist.add(
                        new Sort.Order(direction.equals("ASC") ? Sort.Direction.ASC : Sort.Direction.DESC, field));

            }
        }
        Specification<RoomEntity> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (search == null || search.isEmpty()) {

                if (request.getStatusRoom() != null) {
                    try {
                        StatusRoom statusRoom = StatusRoom.valueOf(request.getStatusRoom().toUpperCase().trim());
                        predicates.add(cb.equal(root.get("statusRoom"), statusRoom));
                    } catch (IllegalArgumentException iae) {
                        return cb.disjunction();
                    }
                }

                if (request.getRoomType() != null) {
                    try {
                        RoomType roomType = RoomType.valueOf(request.getRoomType().toUpperCase().trim());
                        predicates
                                .add(cb.equal(root.get("type"), roomType));

                    } catch (Exception e) {
                        return cb.disjunction();
                    }
                }

                if (request.getAction() != null) {
                    try {
                        Status status = Status.valueOf(request.getAction().toUpperCase().trim());
                        predicates.add(cb.equal(root.get("action"), status));
                    } catch (IllegalArgumentException iae) {
                        return cb.disjunction();
                    }
                }
                if (request.getCreateDated() != null && !request.getCreateDated().isBlank()) {
                    DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                    LocalDate createdDate = LocalDate.parse(request.getCreateDated().trim(), dateTimeFormatter);
                    predicates.add(cb.greaterThanOrEqualTo(root.get("createdDate"),
                            createdDate.atStartOfDay()));
                    predicates.add(cb.lessThan(root.get("createdDate"),
                            createdDate.plusDays(1).atStartOfDay()));
                }

                return cb.and(predicates.toArray(new Predicate[0]));

            } else {
                List<Predicate> allPredicates = new ArrayList<>();
                List<Predicate> searchPredicates = new ArrayList<>();
                String likeSearch = "%" + search.toLowerCase() + "%";

                searchPredicates.add(cb.like(cb.lower(root.get("roomName")), likeSearch));
                searchPredicates.add(cb.like(cb.lower(root.get("roomNumber")), likeSearch));
                searchPredicates.add(cb.like(cb.lower(root.get("description")), likeSearch));

                allPredicates.add(cb.or(searchPredicates.toArray(new Predicate[0])));
                if (request.getStatusRoom() != null) {
                    try {
                        StatusRoom statusRoom = StatusRoom.valueOf(request.getStatusRoom().toUpperCase().trim());
                        allPredicates.add(cb.equal(root.get("statusRoom"), statusRoom));
                    } catch (IllegalArgumentException iae) {
                        return cb.disjunction();
                    }
                }

                if (request.getRoomType() != null) {
                    try {
                        RoomType roomType = RoomType.valueOf(request.getRoomType().toUpperCase().trim());
                        allPredicates
                                .add(cb.equal(root.get("type"), roomType));

                    } catch (Exception e) {
                        return cb.disjunction();
                    }
                }

                if (request.getAction() != null) {
                    try {
                        Status status = Status.valueOf(request.getAction().toUpperCase().trim());
                        allPredicates.add(cb.equal(root.get("action"), status));
                    } catch (IllegalArgumentException iae) {
                        return cb.disjunction();
                    }
                }

                if (request.getCreateDated() != null && !request.getCreateDated().isBlank()) {
                    DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                    LocalDate createdDate = LocalDate.parse(request.getCreateDated().trim(), dateTimeFormatter);
                    allPredicates.add(cb.greaterThanOrEqualTo(root.get("createdDate"),
                            createdDate.atStartOfDay()));
                    allPredicates.add(cb.lessThan(root.get("createdDate"),
                            createdDate.plusDays(1).atStartOfDay()));
                }

                return cb.and(allPredicates.toArray(new Predicate[0]));
            }
        };
        Pageable pageable;
        if (isPage) {
            pageable = PageRequest.of(page - 1, size, Sort.by(sortlist));
        } else {
            pageable = PageRequest.of(0, Integer.MAX_VALUE, Sort.by(sortlist));
        }
        return ApiResponse.success(toRoomListResponses(this.roomRepository.findAll(spec, pageable).getContent()));
    }

    private List<RoomResponse> tRoomResponses(List<RoomEntity> roomEntities) {
        return roomEntities.stream().map(RoomResponse::fromEntity).collect(Collectors.toList());
    }

    private RoomListResponse toRoomListResponses(List<RoomEntity> roomEntities) {
        RoomRepository roomRepository = this.roomRepository;
        int totalRooms = (int) roomRepository.count();
        int availableRooms = roomRepository.countByStatusRoom(StatusRoom.AVAILABLE);
        int bookedRooms = roomRepository.countByStatusRoom(StatusRoom.BOOKED);
        int occupiedRooms = roomRepository.countByStatusRoom(StatusRoom.OCCUPIED);
        int maintenanceRooms = roomRepository.countByStatusRoom(StatusRoom.MAINTENANCE);
        int outOfServiceRooms = roomRepository.countByStatusRoom(StatusRoom.OUT_OF_SERVICE);
        int pendingRooms = roomRepository.countByStatusRoom(StatusRoom.PENDING);
        int cleaningRooms = roomRepository.countByStatusRoom(StatusRoom.CLEANING);
        int singleRooms = roomRepository.countByType(RoomType.SINGLE);
        int doubleRooms = roomRepository.countByType(RoomType.DOUBLE);
        int suiteRooms = roomRepository.countByType(RoomType.SUITE);
        int twinRooms = roomRepository.countByType(RoomType.TWIN);
        int familyRooms = roomRepository.countByType(RoomType.FAMILY);
        int deletedRooms = roomRepository.countByAction(Status.DELETED);
        int closeRooms = roomRepository.countByAction(Status.INACTIVE);

        return RoomListResponse.builder()
                .rooms(tRoomResponses(roomEntities))
                .totalRooms(totalRooms)
                .availableRooms(availableRooms)
                .bookedRooms(bookedRooms)
                .occupiedRooms(occupiedRooms)
                .maintenanceRooms(maintenanceRooms)
                .closeRooms(closeRooms)
                .outOfServiceRooms(outOfServiceRooms)
                .pendingRooms(pendingRooms)
                .singleRooms(singleRooms)
                .doubleRooms(doubleRooms)
                .suiteRooms(suiteRooms)
                .twinRooms(twinRooms)
                .familyRooms(familyRooms)
                .deletedRooms(deletedRooms)
                .cleaningRooms(cleaningRooms)
                .build();
    }
}
