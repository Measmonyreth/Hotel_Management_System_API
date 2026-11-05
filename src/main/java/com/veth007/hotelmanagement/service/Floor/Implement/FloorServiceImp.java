package com.veth007.hotelmanagement.service.Floor.Implement;

import com.veth007.hotelmanagement.repository.RoomRepository;
import com.veth007.hotelmanagement.service.Floor.FloorService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import com.veth007.hotelmanagement.config.HashIdUtil;
import com.veth007.hotelmanagement.constant.enums.Status;
import com.veth007.hotelmanagement.constant.enums.StatusFloor;
import com.veth007.hotelmanagement.constant.enums.StatusRoom;
import com.veth007.hotelmanagement.exception.AlreadyExistException;
import com.veth007.hotelmanagement.exception.ApiResponse;
import com.veth007.hotelmanagement.exception.NotFoundException;
import com.veth007.hotelmanagement.model.entity.FloorEntity;
import com.veth007.hotelmanagement.model.entity.Room.RoomEntity;
import com.veth007.hotelmanagement.model.request.FloorRequest.FloorRequest;
import com.veth007.hotelmanagement.model.request.FloorRequest.UpdateFloorRequest;
import com.veth007.hotelmanagement.model.response.Floor.FloorListResponse;
import com.veth007.hotelmanagement.model.response.Floor.FloorResponse;
import com.veth007.hotelmanagement.model.response.ValidationResponse;
import com.veth007.hotelmanagement.repository.FloorRepository;

import jakarta.persistence.criteria.Predicate;

import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import java.util.ArrayList;

@Service
@RequiredArgsConstructor
public class FloorServiceImp implements FloorService {
    private final RoomRepository roomRepository;
    private final FloorRepository floorRepository;

    @Override
    public ApiResponse<ValidationResponse> isFloorNameAvailable(String floorName) {
        String normalizedName = floorName.trim();
        boolean exists = floorRepository.existsByFloorNameIgnoreCase(normalizedName);
        return ApiResponse.ok(
                exists ? "Floor name already exists" : "Floor name is available",
                new ValidationResponse(!exists, exists ? "DUPLICATE" : "AVAILABLE", normalizedName));
    }

    @Override
    public ApiResponse<ValidationResponse> isFloorNumberAvailable(String floorNumber) {
        String normalizedNumber = floorNumber.trim();
        boolean exists = floorRepository.existsByFloorNumber(normalizedNumber);
        return ApiResponse.ok(
                exists ? "Floor number already exists" : "Floor number is available",
                new ValidationResponse(!exists, exists ? "DUPLICATE" : "AVAILABLE", normalizedNumber));
    }
    private final HashIdUtil hashIdUtil;

    // create
    @Override
    @Transactional
    public ApiResponse<FloorResponse> createFloor(FloorRequest floorRequest) {
        try {
            String floorName = floorRequest.getFloorName().trim();
            String floorNumber = floorRequest.getFloorNumber().trim();
            floorRequest.setFloorName(floorName);
            floorRequest.setFloorNumber(floorNumber);

            if (floorRepository.existsByFloorNameIgnoreCase(floorName)) {
                throw new AlreadyExistException("Name of floor is already existed");
            }

            if (floorRepository.existsByFloorNumber(floorRequest.getFloorNumber())) {
                throw new AlreadyExistException("Floor Number is already existed");
            }

            FloorEntity floorEntity = FloorEntity.builder()
                    .floorName(floorRequest.getFloorName())
                    .floorNumber(floorRequest.getFloorNumber())
                    .description(floorRequest.getDescription())
                    .statusFloor(StatusFloor.OPEN)
                    .action(Status.ACTIVE)
                    .build();

            FloorEntity savedFloor = floorRepository.save(floorEntity);

            return ApiResponse.success(FloorResponse.mapToResponse(savedFloor));

        } catch (AlreadyExistException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Failed to create floor", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<FloorResponse> getFloorById(Long id) {
        FloorEntity floorEntity = floorRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Floor not found with id: " + id));
        long totalRooms = roomRepository.countByFloorId(floorEntity.getId());
        long openRooms = roomRepository.countByFloorIdAndStatusRoom(floorEntity.getId(), StatusRoom.AVAILABLE);

        return ApiResponse.success(FloorResponse.mapToResponse(floorEntity, totalRooms, openRooms));
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<FloorListResponse> getAllFloor(int page, int size, boolean isPage) {
        List<FloorEntity> floorEntities;
        if (isPage) {
            Pageable pageable = PageRequest.of(Math.max(page, 0), size, Sort.by(Sort.Direction.DESC, "id"));
            floorEntities = floorRepository.findAll(pageable).getContent();
        } else {
            Pageable pageable = PageRequest.of(0, Integer.MAX_VALUE, Sort.by(Sort.Direction.DESC, "id"));
            floorEntities = floorRepository.findAll(pageable).getContent();
        }

        return ApiResponse.success(toListResponse(floorEntities));
    }

    @Override
    @Transactional
    public ApiResponse<FloorResponse> update(Long id, UpdateFloorRequest floorRequest) {
        FloorEntity floorEntity = floorRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Floor not found with id: " + id));

        if (floorRepository.findByIdAndDeletedByIsNotNull(id) != null) {
            throw new AlreadyExistException("Cannot update a deleted floor");
        }

        StatusFloor statusFloor = StatusFloor.valueOf(floorRequest.getStatusFloor().toUpperCase());
        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        if (floorRequest.getFloorName() != null
                && floorRepository.findByFloorNameIgnoreCase(floorRequest.getFloorName().trim())
                        .filter(existing -> !existing.getId().equals(id))
                        .isPresent()) {
            throw new AlreadyExistException("Name of floor is already existed");
        }

        if (floorRequest.getFloorNumber() != null
                && floorRepository.findByFloorNumber(floorRequest.getFloorNumber().trim())
                        .filter(existing -> !existing.getId().equals(id))
                        .isPresent()) {
            throw new AlreadyExistException("Floor Number is already existed");
        }

        floorEntity.setFloorName(
                floorRequest.getFloorName() != null ? floorRequest.getFloorName() : floorEntity.getFloorName());
        floorEntity.setFloorNumber(floorRequest.getFloorNumber() != null ? floorRequest.getFloorNumber()
                : floorEntity.getFloorNumber());
        floorEntity.setDescription(floorRequest.getDescription() != null ? floorRequest.getDescription()
                : floorEntity.getDescription());
        floorEntity.setStatusFloor(statusFloor != null ? statusFloor : floorEntity.getStatusFloor());
        floorEntity.setLastModifiedBy(username != null ? username : floorEntity.getLastModifiedBy());
        floorEntity.setLastModifiedDate(LocalDateTime.now());

        floorRepository.save(floorEntity);
        return ApiResponse.success(FloorResponse.mapToResponse(floorEntity));
    }

    @Override
    public ApiResponse<String> delete(Long id) {
        FloorEntity floorEntity = floorRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Floor not found with id: " + id));

        if (floorEntity.getAction() == Status.DELETED) {
            throw new RuntimeException("Floor already deleted");
        }

        // if delete floor make sure that all room in this floor is deleted too, if not
        // throw exception
        if (Objects.nonNull(floorEntity.getRooms())
                && floorEntity.getRooms().stream().anyMatch(room -> room.getAction() != Status.DELETED)) {
            throw new RuntimeException(
                    "Cannot delete floor with active rooms. Please delete all rooms in this floor first.");
        }

        try {

            String username = SecurityContextHolder.getContext().getAuthentication().getName();
            floorEntity.setAction(Status.DELETED);
            floorEntity.setDeletedBy(username);
            floorEntity.setDeletedDate(LocalDateTime.now());
            floorEntity.setStatusFloor(StatusFloor.CLOSED);

           

            floorRepository.save(floorEntity);
            return ApiResponse.success("Floor deleted successfully");
        } catch (Exception e) {
            // TODO: handle exception
            throw new RuntimeException("Failed to delete floor", e);
        }

    }

    @Override
    public ApiResponse<List<FloorResponse>> list_deleted() {
        try {
            if (floorRepository.existsByDeletedByIsNotNull() && floorRepository.existsByDeletedDateIsNotNull()) {
                return ApiResponse.success(floorRepository.findByDeletedByIsNotNull().stream()
                        .map(FloorResponse::mapToResponse)
                        .toList());
            } else if (floorRepository.existsByDeletedDateIsNotNull()) {
                return ApiResponse.success(floorRepository.findByDeletedDateIsNotNull().stream()
                        .map(FloorResponse::mapToResponse)
                        .toList());
            } else {
                return ApiResponse.success(List.of());
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to list deleted floor", e);
        }
    }

    private FloorEntity findOne(Long id) {

        FloorEntity floorEntity = floorRepository.findByIdAndDeletedByIsNotNull(id);
        if (floorEntity == null) {
            throw new RuntimeException("Deleted floor not found with id: " + id);
        }
        return floorEntity;

    }

    @Override
    public ApiResponse<String> restore(Long id) {
        if (floorRepository.existsByIdAndDeletedByIsNull(id)) {
            throw new AlreadyExistException("Floor is not deleted");
        }

        FloorEntity floorEntity = findOne(id);

        try {

            floorEntity.setAction(Status.ACTIVE);
            floorEntity.setDeletedBy(null);
            floorEntity.setDeletedDate(null);
            floorEntity.setStatusFloor(StatusFloor.OPEN);
            floorRepository.save(floorEntity);
            return ApiResponse.success("Floor restored successfully");
        } catch (Exception e) {
            throw new RuntimeException("Failed to restore floor", e);
        }
    }

    @Override
    public ApiResponse<FloorListResponse> search_floor(Map<String, String> allParams, String sort,
            String search, int page, int size, boolean isPage) {
        try {
            UpdateFloorRequest floorRequest = new UpdateFloorRequest();
            allParams.forEach((key, value) -> {
                try {
                    FloorGroupBy floorGroupBy = FloorGroupBy.valueOf(key.toUpperCase());
                    floorGroupBy.applyFilter(floorRequest, value);
                } catch (Exception e) {
                    key = key.toUpperCase();
                    switch (key) {
                        case "STATUS_FLOOR" ->
                            floorRequest.setStatusFloor(value);
                        case "ACTION" ->
                            floorRequest.setAction(value);
                       case "CREATED_DATE" ->
                            floorRequest.setCreateDated(value);
                           
                    }
                }
            });
            List<Sort.Order> sorts = new ArrayList<>();
            if (sort != null && !sort.isBlank()) {
                for (String s : sort.split(",")) {
                    String[] str = s.split(":");

                    if (str.length != 2) {
                        throw new RuntimeException("Invalid sort format like: id:desc");
                    }

                    String direction = str[1].toUpperCase();
                    String field = str[0];

                    sorts.add(
                            new Sort.Order(direction.equals("ASC") ? Sort.Direction.ASC : Sort.Direction.DESC, field));
                }
            }

            Specification<FloorEntity> spec = (root, query, criteriaBuilder) -> {
                List<Predicate> predicates = new ArrayList<>();

                if (search != null && !search.isBlank()) {
                    List<Predicate> searchPredicates = new ArrayList<>();
                    String searchPattern = "%" + search.toLowerCase() + "%";

                    searchPredicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("floorName")), searchPattern));
                    searchPredicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("floorNumber")), searchPattern));
                    searchPredicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("description")), searchPattern));
                    predicates.add(criteriaBuilder.or(searchPredicates.toArray(new Predicate[0])));
                }

                if (floorRequest.getAction() != null && !floorRequest.getAction().isBlank()) {
                    try {
                        Status action = Status.valueOf(floorRequest.getAction().toUpperCase().trim());
                        predicates.add(criteriaBuilder.equal(root.get("action"), action));
                    } catch (IllegalArgumentException iae) {
                        return criteriaBuilder.disjunction();
                    }
                }

                if (floorRequest.getStatusFloor() != null && !floorRequest.getStatusFloor().isBlank()) {
                    try {
                        StatusFloor status = StatusFloor.valueOf(floorRequest.getStatusFloor().toUpperCase().trim());
                        predicates.add(criteriaBuilder.equal(root.get("statusFloor"), status));
                    } catch (IllegalArgumentException iae) {
                        return criteriaBuilder.disjunction();
                    }
                }

                if (floorRequest.getCreateDated() != null && !floorRequest.getCreateDated().isBlank()) {
                    DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                    LocalDate createdDate = LocalDate.parse(floorRequest.getCreateDated().trim(), dateTimeFormatter);
                    predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("createdDate"), createdDate.atStartOfDay()));
                    predicates.add(criteriaBuilder.lessThan(root.get("createdDate"), createdDate.plusDays(1).atStartOfDay()));
                }

                return predicates.isEmpty() ? criteriaBuilder.conjunction() : criteriaBuilder.and(predicates.toArray(new Predicate[0]));
            };

            Pageable pageable;
            if (isPage) {
                pageable = PageRequest.of(page - 1, size, Sort.by(sorts));
            } else {
                pageable = PageRequest.of(0, Integer.MAX_VALUE, Sort.by(sorts));
            }

            return ApiResponse
                    .success(toListResponse(this.floorRepository.findAll(spec, pageable).getContent()));
        } catch (Exception e) {
            throw new RuntimeException("Failed to filter floor", e);
        }
    }

    // private List<FloorResponse> toResponse(List<FloorEntity> entities) {
    // List<FloorResponse> responses = new ArrayList<>();
    // for (FloorEntity entity : entities) {
    // FloorResponse response = FloorResponse.mapToResponse(entity);
    // responses.add(response);
    // }
    // return responses;
    // }

    private FloorListResponse toListResponse(List<FloorEntity> flooreEntity) {
        FloorRepository floorRepository = this.floorRepository;
        RoomRepository roomRepository = this.roomRepository;
        int totalfloors = (int) floorRepository.count();
        int openfloors = (int) floorRepository.countByStatusFloor(StatusFloor.OPEN);
        int closefloors = (int) floorRepository.countByStatusFloor(StatusFloor.CLOSED);
        int reservedfloors = (int) floorRepository.countByStatusFloor(StatusFloor.RESTRICTED);
        int maintenancefloors = (int) floorRepository.countByStatusFloor(StatusFloor.MAINTENANCE);

        return FloorListResponse.mapToResponse(
                flooreEntity.stream().map(floor -> {
                    long totalRooms = roomRepository.countByFloorId(floor.getId());
                    long openRooms = roomRepository.countByFloorIdAndStatusRoom(floor.getId(),
                            StatusRoom.AVAILABLE);
                    return FloorResponse.mapToResponse(floor, totalRooms, openRooms);
                }).toList(),
                totalfloors,
                openfloors,
                closefloors,
                reservedfloors,
                maintenancefloors);
    }
}