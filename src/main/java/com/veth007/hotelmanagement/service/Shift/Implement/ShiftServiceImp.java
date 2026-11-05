package com.veth007.hotelmanagement.service.Shift.Implement;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import jakarta.persistence.criteria.Predicate;
import com.veth007.hotelmanagement.constant.enums.Shift;
import com.veth007.hotelmanagement.exception.AlreadyExistException;
import com.veth007.hotelmanagement.exception.ApiResponse;
import com.veth007.hotelmanagement.exception.NotFoundException;
import com.veth007.hotelmanagement.model.entity.ShiftEntity;
import com.veth007.hotelmanagement.model.request.Shift.ShiftRequest;
import com.veth007.hotelmanagement.model.request.Shift.UpdateShiftRequest;
import com.veth007.hotelmanagement.model.response.Schedule.ShiftListResponse;
import com.veth007.hotelmanagement.model.response.Schedule.ShiftResponse;
import com.veth007.hotelmanagement.repository.ShiftRepository;
import com.veth007.hotelmanagement.service.Shift.ShiftService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ShiftServiceImp implements ShiftService {
    private final ShiftRepository shiftRepository;
    private static  final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("h:mma", Locale.ENGLISH);

    @Override
    public ApiResponse<ShiftResponse> createShift(ShiftRequest request) {

        // Validate time format
        LocalTime start;
        LocalTime end;
        try {
            start = LocalTime.parse(request.getStartingTime().toUpperCase().trim(), formatter);
            end = LocalTime.parse(request.getEndingTime().toUpperCase().trim(), formatter);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Invalid time format. Expected format: h:mmAM/PM (e.g. 8:00AM, 5:00PM)");
        }

        // Validate shift enum
        Shift shift;
        try {
            shift = Shift.valueOf(request.getShift().toUpperCase().trim());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid shift value: " + request.getShift());
        }

        // Validate start before end
        if (!start.isBefore(end)) {
            throw new IllegalArgumentException("Starting time must be before ending time");
        }

        ShiftEntity shiftEntity = ShiftEntity.builder()
                .dayWork(request.getDayWork())
                .dayOff(request.getDayOff())
                .startingTime(start)
                .endingTime(end)
                .shift(shift)
                .build();

        return ApiResponse.success(ShiftResponse.fromEntity(shiftRepository.save(shiftEntity)));
    }

    @Override
    public ApiResponse<List<ShiftListResponse>> getAllShift(int page, int size, boolean isPage) {
        try {
            List<ShiftEntity> shiftEntities;
            if (isPage) {
                Pageable pageable = PageRequest.of(Math.max(page, 0), size, Sort.by(Sort.Direction.ASC, "id"));
                shiftEntities = this.shiftRepository.findAll(pageable).getContent();
            } else {
                Pageable pageable = PageRequest.of(0, Integer.MAX_VALUE, Sort.by(Sort.Direction.ASC, "id"));
                shiftEntities = this.shiftRepository.findAll(pageable).getContent();
            }
            return ApiResponse.success(List.of(toShiftListResponse(shiftEntities)));
        } catch (Exception e) {
            throw new IllegalStateException("Failed to fetch shifts: " + e.getMessage());
        }
    }

    private ShiftListResponse toShiftListResponse(List<ShiftEntity> shiftEntities){
        int totalShifts = (int) shiftRepository.count();
        int totalWorkFromMondayToFriday = shiftRepository.countByDayWork("Monday-Friday");
        int totalWorkFromMondayToSaturday = shiftRepository.countByDayWork("Monday-Saturday");
        int totalWorkFromSundayToFriday = shiftRepository.countByDayWork("Sunday-Friday");
        int totalTimeFrom8AMTo5PM = shiftRepository.countByStartingTimeAndEndingTime(LocalTime.of(8, 0), LocalTime.of(17, 0));
        int totalTimeFrom9AMTo6PM = shiftRepository.countByStartingTimeAndEndingTime(LocalTime.of(9, 0), LocalTime.of(18, 0));
        int totalFullTime = shiftRepository.countByShift(Shift.FULLTIME);
        int totalPartTime = 0;
        int totalMorning = shiftRepository.countByShift(Shift.MORNING);
        int totalAfternoon = shiftRepository.countByShift(Shift.AFTERNOON);
        int totalEvening = shiftRepository.countByShift(Shift.NIGHT);
        totalPartTime = totalMorning + totalAfternoon + totalEvening;

        return ShiftListResponse.builder()
                .totalShifts(totalShifts)
                .totalWorkFromMondayToFriday(totalWorkFromMondayToFriday)
                .totalWorkFromMondayToSaturday(totalWorkFromMondayToSaturday)
                .totalWorkFromSundayToFriday(totalWorkFromSundayToFriday)
                .totalTimeFrom8AMTo5PM(totalTimeFrom8AMTo5PM)
                .totalTimeFrom9AMTo6PM(totalTimeFrom9AMTo6PM)
                .totalFullTime(totalFullTime)
                .totalPartTime(totalPartTime)
                .shifts(shiftEntities.stream().map(ShiftResponse::fromEntity).collect(Collectors.toList()))
                .build();
    }

    @Override
    public ApiResponse<ShiftResponse> getShiftById(Long id) {
        ShiftEntity shiftEntity = this.shiftRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Shift not found with id: " + id));
        try {
            return ApiResponse.success(ShiftResponse.fromEntity(shiftEntity));
        } catch (Exception e) {
            throw new IllegalStateException("Failed to fetch shift: " + e.getMessage());
        }
    }

    // public List<ShiftEntity> get_shifts_by_shift(Shift shift) {
    // try {
    // if (!this.shiftRepository.existsByShift(shift)) {
    // throw new RuntimeException("No shifts found with shift: " + shift);
    // }
    // List<ShiftEntity> shiftEntity = this.shiftRepository.findByShift(shift);
    // return shiftEntity;

    // } catch (Exception e) {
    // // TODO: handle exception
    // throw new IllegalStateException("Failed to fetch shifts by shift: " +
    // e.getMessage());
    // }
    // }

    public ApiResponse<ShiftResponse> updateShift(Long id, ShiftRequest request) {
        if (shiftRepository.existsByIdAndDeletedDateIsNotNull(id)
                || shiftRepository.existsByIdAndDeletedByIsNotNull(id)) {
            throw new AlreadyExistException("Shift is already deleted with id: " + id);
        }

        ShiftEntity existing = shiftRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Shift not found with id: " + id));

        boolean isUpdated = false;

        if (request.getDayWork() != null && !request.getDayWork().isBlank()) {
            existing.setDayWork(request.getDayWork());
            isUpdated = true;
        }
        if (request.getDayOff() != null && !request.getDayOff().isBlank()) {
            existing.setDayOff(request.getDayOff());
            isUpdated = true;
        }
        if (request.getStartingTime() != null && !request.getStartingTime().isBlank()) {
            try {
                existing.setStartingTime(parseFlexibleTime(request.getStartingTime()));
                isUpdated = true;
            } catch (Exception e) {
                throw new IllegalArgumentException(
                        "Invalid startingTime format. Expected format: h:mmAM/PM (e.g. 8:00AM)");
            }
        }
        if (request.getEndingTime() != null && !request.getEndingTime().isBlank()) {
            try {
                existing.setEndingTime(parseFlexibleTime(request.getEndingTime()));
                isUpdated = true;
            } catch (Exception e) {
                throw new IllegalArgumentException(
                        "Invalid endingTime format. Expected format: h:mmAM/PM (e.g. 5:00PM)");
            }
        }
        if (request.getShift() != null && !request.getShift().isBlank()) {
            try {
                existing.setShift(Shift.valueOf(request.getShift().toUpperCase().trim()));
                isUpdated = true;
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Invalid shift value: " + request.getShift());
            }
        }

        // Only update audit fields if at least one field was modified
        if (isUpdated) {
            String username = SecurityContextHolder.getContext().getAuthentication().getName();
            existing.setLastModifiedBy(username);
            existing.setLastModifiedDate(LocalDateTime.now());
        }

        // Validate start before end
        if (existing.getStartingTime() != null && existing.getEndingTime() != null
                && !existing.getStartingTime().isBefore(existing.getEndingTime())) {
            throw new IllegalArgumentException("Starting time must be before ending time");
        }

        try {
            return ApiResponse.success(ShiftResponse.fromEntity(shiftRepository.save(existing)));
        } catch (Exception e) {
            throw new IllegalStateException("Failed to update shift: " + e.getMessage());
        }
    }

    public ApiResponse<String> deleteShift(Long id) {
        ShiftEntity existing = shiftRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Shift not found with id: " + id));

        if (shiftRepository.existsByIdAndDeletedDateIsNotNull(id)
                || shiftRepository.existsByIdAndDeletedByIsNotNull(id)) {
            throw new AlreadyExistException("Shift is already deleted with id: " + id);
        }
        try {

            String username = SecurityContextHolder.getContext().getAuthentication().getName();
            existing.setDeletedBy(username);
            existing.setDeletedDate(LocalDateTime.now());
            shiftRepository.save(existing); // Soft delete by updating audit fields

            return ApiResponse.success("Shift with id " + id + " has been deleted successfully.");
        } catch (Exception e) {
            throw new IllegalStateException("Failed to delete shift: " + e.getMessage());
        }
    }

    @Override
    public ApiResponse<List<ShiftResponse>> list_deleted() {
        if (!shiftRepository.existsByDeletedDateIsNotNull() && !shiftRepository.existsByDeletedByIsNotNull()) {
            throw new NotFoundException("No deleted shifts found");
        }
        try {

            return ApiResponse.success(toResponses(shiftRepository.findByDeletedDateIsNotNull()));
        } catch (Exception e) {
            throw new IllegalStateException("Failed to fetch deleted shifts: " + e.getMessage());
        }
    }

    private ShiftEntity findOne(Long id) {
        ShiftEntity shiftEntity = shiftRepository.findByIdAndDeletedByIsNotNull(id);
        if (shiftEntity == null) {
            throw new NotFoundException("Shift not found with id: " + id);
        }
        if (!shiftRepository.existsByIdAndDeletedDateIsNotNull(id)) {
            throw new AlreadyExistException("Shift isn't deleted with id: " + id);
        }
        try {
            return shiftEntity;
        } catch (Exception e) {
            // TODO: handle exception
            throw new IllegalStateException("Failed to fetch shift: " + e.getMessage());
        }
    }

    @Override
    public ApiResponse<String> restoreShift(Long id) {
        ShiftEntity shiftEntity = this.findOne(id);
        try {
            shiftEntity.setDeletedBy(null);
            shiftEntity.setDeletedDate(null);
            this.shiftRepository.save(shiftEntity);
            return ApiResponse.success("Shift with id " + id + " has been restored successfully.");
        
        } catch (Exception e) {
            throw new IllegalStateException("Failed to restore shift: " + e.getMessage());
        }
    }

    @Override
    public ApiResponse<ShiftListResponse> searchShifts(Map<String, String> allParams, String sort, int page, int size, boolean isPage) {
        UpdateShiftRequest request = new UpdateShiftRequest();
        List<String> validationErrors = new ArrayList<>();

        allParams.forEach((key, value) -> {
            try {
                String enumKey = key.replaceAll("([a-z])([A-Z])", "$1_$2").toUpperCase();
                ShiftGroupBy groupBy = ShiftGroupBy.valueOf(enumKey);

                // Validate shift enum values
                if (enumKey.equals("SHIFT")) {
                    try {
                        Shift.valueOf(value.toUpperCase().trim());
                    } catch (IllegalArgumentException e) {
                        validationErrors.add(
                                "Invalid shift value: " + value + ". Valid values: MORNING, AFTERNOON, EVENING, etc.");
                        return;
                    }
                }

                // Validate time format
                if (enumKey.equals("STARTING_TIME") || enumKey.equals("ENDING_TIME")) {
                    try {
                        LocalTime.parse(value.toUpperCase().trim(),
                                DateTimeFormatter.ofPattern("h:mma", Locale.ENGLISH));
                    } catch (Exception e) {
                        try {
                            LocalTime.parse(value.trim());
                        } catch (Exception e2) {
                            validationErrors.add("Invalid " + enumKey.toLowerCase() + " format: " + value
                                    + ". Expected: HH:mm or h:mmAM/PM");
                            return;
                        }
                    }
                }

                groupBy.applyFilter(request, value);
            } catch (IllegalArgumentException e) {
                validationErrors.add("Unknown filter parameter: " + key);
            }

        });
        List<Sort.Order> orders = new ArrayList<>();
        if (sort != null && !sort.isBlank()) {
            for (String s : sort.split(",")) {
                String[] str = s.split(":");
                String field = str[0].trim();
                String direction = (str.length == 2) ? str[1].trim() : "asc";
                orders.add(new Sort.Order(
                        direction.equals("asc") || direction.equals("ASC") ? Direction.ASC : Direction.DESC, field));
            }
        }

        Specification<ShiftEntity> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            List<String> failedFilters = new ArrayList<>();

            if (request.getDayWork() != null && !request.getDayWork().isBlank()) {
                predicates.add(
                        cb.like(cb.lower(root.get("dayWork")), "%" + request.getDayWork().toLowerCase() + "%"));
            }
            if (request.getDayOff() != null && !request.getDayOff().isBlank()) {
                predicates
                        .add(cb.like(cb.lower(root.get("dayOff")), "%" + request.getDayOff().toLowerCase() + "%"));
            }
            if (request.getShift() != null && !request.getShift().isBlank()) {
                try {
                    predicates.add(
                            cb.equal(root.get("shift"), Shift.valueOf(request.getShift().toUpperCase().trim())));
                } catch (Exception e) {
                    failedFilters.add("shift=" + request.getShift());
                }
            }
            if (request.getStartingTime() != null && !request.getStartingTime().isBlank()) {
                try {
                    predicates
                            .add(cb.equal(root.get("startingTime"), parseFlexibleTime(request.getStartingTime())));
                } catch (Exception e) {
                    failedFilters.add("startingTime=" + request.getStartingTime());
                }
            }
            if (request.getEndingTime() != null && !request.getEndingTime().isBlank()) {
                try {
                    predicates.add(cb.equal(root.get("endingTime"), parseFlexibleTime(request.getEndingTime())));
                } catch (Exception e) {
                    failedFilters.add("endingTime=" + request.getEndingTime());
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

            if (!failedFilters.isEmpty()) {
                System.out.println("Invalid filters ignored: " + String.join(", ", failedFilters));
            }

            if (predicates.isEmpty()) {
                System.out.println("WARNING: No valid filters applied. Returning all records.");
                return cb.and(); // Returns true, matches all
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Pageable pageable;
        if (isPage) {
            pageable = PageRequest.of(page - 1, size, Sort.by(orders));
        } else {
            pageable = PageRequest.of(0, Integer.MAX_VALUE, Sort.by(orders));
        }

        return ApiResponse.success(toShiftListResponse(shiftRepository.findAll(spec, pageable).getContent()));
    }

    private LocalTime parseFlexibleTime(String timeStr) {
        timeStr = timeStr.toUpperCase().trim();
        try {
            return LocalTime.parse(timeStr, formatter);
        } catch (DateTimeParseException e) {
            // Fallback for standard formats like "08:00"
            return LocalTime.parse(timeStr);
        }
    }

    // Fetch all + convert to response with numbering
    private List<ShiftResponse> toResponses(List<ShiftEntity> entities) {
        List<ShiftResponse> responses = new ArrayList<>();
        for (int i = 0; i < entities.size(); i++) {
            ShiftResponse res = ShiftResponse.fromEntity(entities.get(i));
            responses.add(res);
        }
        return responses;
    }

}
