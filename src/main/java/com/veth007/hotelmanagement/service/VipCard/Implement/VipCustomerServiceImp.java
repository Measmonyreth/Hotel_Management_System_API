package com.veth007.hotelmanagement.service.VipCard.Implement;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.hibernate.Hibernate;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;
import com.veth007.hotelmanagement.constant.enums.Status;
import com.veth007.hotelmanagement.constant.enums.Type_vip;
import com.veth007.hotelmanagement.constant.enums.VipStatus;
import com.veth007.hotelmanagement.exception.AlreadyExistException;
import com.veth007.hotelmanagement.exception.ApiResponse;
import com.veth007.hotelmanagement.exception.NotFoundException;
import com.veth007.hotelmanagement.model.entity.CustomerEntity;
import com.veth007.hotelmanagement.model.entity.VipCustomerEntity;
import com.veth007.hotelmanagement.model.request.VipCustomerRequest.UpdateVipCustomerRequest;
import com.veth007.hotelmanagement.model.request.VipCustomerRequest.VipCustmerRequestBase;
import com.veth007.hotelmanagement.model.request.VipCustomerRequest.VipCustomerRequest;
import com.veth007.hotelmanagement.model.response.VipCustomer.VipCustomerResponse;
import com.veth007.hotelmanagement.model.response.VipCustomer.VipListCustomerResponse;
import com.veth007.hotelmanagement.repository.CustomerRepository;
import com.veth007.hotelmanagement.repository.VipCustomerRepository;
import com.veth007.hotelmanagement.service.VipCard.VipCustomerService;

import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;

@Service
@RequestMapping("/api")
@RequiredArgsConstructor
public class VipCustomerServiceImp implements VipCustomerService{
    private final VipCustomerRepository repository;
    private final CustomerRepository customerRepository;
    private static  final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.ENGLISH);
    LocalDate valideFrom;
    LocalDate valideUntil;

    private String generateBookingCode() {
        String datePart = LocalDate.now().format(DateTimeFormatter.ofPattern("yyMMdd"));
        String prefix = "VIP_CARD" + datePart;

        String lastCode = repository.findLastCodeByPrefix(prefix);

        int nextSeq = 1;
        if (lastCode != null) {
            int lastSeq = Integer.parseInt(lastCode.substring(prefix.length()));
            nextSeq = lastSeq + 1;
        }
        return prefix + String.format("%05d", nextSeq);

    }

    private <T extends VipCustmerRequestBase> void validateCustomer(T request) {
        if (customerRepository.existsByIdAndDeletedDateIsNotNull(request.getCustomerId())) {
            throw new AlreadyExistException("Customer with id: " + request.getCustomerId() + " is deleted");
        }
        if (!customerRepository.existsByIdAndStatus(request.getCustomerId(), Status.ACTIVE)) {
            throw new NotFoundException("Customer with id: " + request.getCustomerId() + " is not active");
        }
    }

    @Override
    public ApiResponse<VipCustomerResponse> create(VipCustomerRequest request) {

        try {
            valideFrom = LocalDate.parse(request.getValidFrom().toLowerCase().trim(), dateTimeFormatter);
            valideUntil = LocalDate.parse(request.getValidUntil().toLowerCase().trim(), dateTimeFormatter);
        } catch (DateTimeParseException e) {
            // TODO: handle exception
            throw new RuntimeException("Incorrect format date time. Expected format: yyyy-MM-dd");
        }

        if (valideFrom.isAfter(valideUntil)) {
            throw new RuntimeException("Valid from must be before valid until");
        }

        CustomerEntity customerEntity = customerRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new NotFoundException("Customer not found with id: " + request.getCustomerId()));

        this.validateCustomer(request);
        if (repository.existsByCustomerIdAndStatus(request.getCustomerId(), VipStatus.ACTIVE)) {
            throw new AlreadyExistException("VipCustomer with this customer is already exist");
        }

        try {

            VipCustomerEntity vipCustomerEntity = VipCustomerEntity.builder()
                    .code(generateBookingCode())
                    .typeVip(Type_vip.valueOf(request.getType_vip()))
                    .points(request.getPoints() != null ? request.getPoints():0.0)
                    .totalSpent(request.getTotalSpent()!= null ? request.getTotalSpent():0.0)
                    .validFrom(valideFrom)
                    .validUntil(valideUntil)
                    .percentage_discount(request.getPercentage_discount())
                    .status(VipStatus.ACTIVE)
                    .customer(customerEntity)
                    .build();

            VipCustomerEntity saved = repository.save(vipCustomerEntity);
            // Initialize lazy relationships
            Hibernate.initialize(saved.getCustomer());
            Hibernate.initialize(saved.getDiscounts());
            return ApiResponse.success(VipCustomerResponse.fromEntity(saved));

        } catch (Exception e) {
            // TODO: handle exception
            throw new RuntimeException("Error when create VipCustomer: " + e.getMessage());
        }
    }

    @Override
    public ApiResponse<VipCustomerResponse> update(Long id, UpdateVipCustomerRequest request) {
        validateCustomer(request);
        try {
            valideFrom = request.getValidFrom() != null
                    ? LocalDate.parse(request.getValidFrom().toLowerCase().trim(), dateTimeFormatter)
                    : null;
            valideUntil = request.getValidUntil() != null
                    ? LocalDate.parse(request.getValidUntil().toLowerCase().trim(), dateTimeFormatter)
                    : null;
        } catch (DateTimeParseException e) {
            // TODO: handle exception
            throw new RuntimeException("Incorrect format date time. Expected format: yyyy-MM-dd");
        }

        if (valideFrom != null && valideUntil != null && valideFrom.isAfter(valideUntil)) {
            throw new RuntimeException("Valid from must be before valid until");
        }
        VipCustomerEntity entity = this.repository.findById(id).orElseThrow(
                () -> new NotFoundException("VipCustomer not found with id: " + id));
        try {

            String username = SecurityContextHolder.getContext().getAuthentication().getName();

            entity.setCode(request.getCode() != null ? request.getCode() : entity.getCode());
            entity.setTypeVip(
                    request.getType_vip() != null ? Type_vip.valueOf(request.getType_vip()) : entity.getTypeVip());
            entity.setPoints(request.getPoints() != null ? request.getPoints() : entity.getPoints());
            entity.setTotalSpent(request.getTotalSpent() != null ? request.getTotalSpent() : entity.getTotalSpent());
            entity.setValidFrom(valideFrom != null ? valideFrom : entity.getValidFrom());
            entity.setValidUntil(valideUntil != null ? valideUntil : entity.getValidUntil());
            entity.setPercentage_discount(request.getPercentage_discount() >= 0 ? request.getPercentage_discount()
                    : entity.getPercentage_discount());
            entity.setStatus(request.getStatus() != null ? VipStatus.valueOf(request.getStatus().toUpperCase())
                    : entity.getStatus());
            entity.setLastModifiedBy(username);
            entity.setLastModifiedDate(LocalDateTime.now());
            VipCustomerEntity saved = this.repository.save(entity);
            Hibernate.initialize(saved.getCustomer());
            Hibernate.initialize(saved.getDiscounts());
            return ApiResponse.success(VipCustomerResponse.fromEntity(saved));
        } catch (Exception e) {
            // TODO: handle exception

            throw new RuntimeException("Error when update VipCustomer: " + e.getMessage());
        }
    }

    @Transactional(readOnly = true)
    public ApiResponse<VipListCustomerResponse> get_all(int page, int size, boolean isPage) {
        try {
            List<VipCustomerEntity> entity;
            if (isPage) {
                Pageable pageable = PageRequest.of(Math.max(page, 0), size, Sort.by(Sort.Direction.DESC, "createdDate"));
                entity = this.repository.findAll(pageable).getContent();
            } else {
                Pageable pageable = PageRequest.of(0, Integer.MAX_VALUE, Sort.by(Sort.Direction.DESC, "createdDate"));
                entity = this.repository.findAll(pageable).getContent();
            }
            // Initialize lazy relationships before transaction ends
            entity.forEach(vip -> {
                Hibernate.initialize(vip.getCustomer());
                Hibernate.initialize(vip.getDiscounts());
            });
            return ApiResponse.success(vipListCustomerResponse(entity));
        } catch (Exception e) {
            // TODO: handle exception
            throw new RuntimeException("Error when get all VipCustomer: " + e.getMessage());
        }
    }

    @Transactional(readOnly = true)
    public ApiResponse<VipCustomerResponse> get_by_id(Long id) {
        VipCustomerEntity entity = this.repository.findById(id).orElseThrow(
                () -> new NotFoundException("VipCustomer not found id: " + id));
        // Initialize lazy relationships
        Hibernate.initialize(entity.getCustomer());
        Hibernate.initialize(entity.getDiscounts());
        try {

            return ApiResponse.success(VipCustomerResponse.fromEntity(entity));
        } catch (Exception e) {
            // TODO: handle exception
            throw new RuntimeException("Error when get VipCustomer by vip customer id: " + e.getMessage());
        }
    }

        @Transactional(readOnly = true)
    public ApiResponse<VipCustomerResponse> get_for_me(Long id) {
        VipCustomerEntity entity = this.repository.findByCustomerId(id).orElseThrow(
                () -> new NotFoundException("VipCustomer not found id: " + id));
        // Initialize lazy relationships
        Hibernate.initialize(entity.getCustomer());
        Hibernate.initialize(entity.getDiscounts());
        try {

            return ApiResponse.success(VipCustomerResponse.fromEntity(entity));
        } catch (Exception e) {
            // TODO: handle exception
            throw new RuntimeException("Error when get VipCustomer by vip customer id: " + e.getMessage());
        }
    }

    @Transactional(readOnly = true)
    public ApiResponse<VipCustomerResponse> get_by_cusId(Long cus_id) {
        VipCustomerEntity entity = this.repository.findByCustomerId(cus_id).orElseThrow(
                () -> new NotFoundException("VipCustomer not found with customer id: " + cus_id));
        try {

            // Initialize lazy relationships
            Hibernate.initialize(entity.getCustomer());
            Hibernate.initialize(entity.getDiscounts());
            return ApiResponse.success(VipCustomerResponse.fromEntity(entity));
        } catch (Exception e) {
            // TODO: handle exception
            throw new RuntimeException("Error when get VipCustomer by customer id: " + e.getMessage());
        }
    }

    // public List<VipCustomerEntity> get_by_status(Status status) {
    // try {
    // if (!this.repository.existsByStatus(status)) {
    // throw new RuntimeException("No VipCustomer found with status: " + status);
    // }

    // List<VipCustomerEntity> entity = this.repository.findByStatus(status);

    // return entity.stream().toList();
    // } catch (Exception e) {
    // // TODO: handle exception
    // throw new RuntimeException("Error when get VipCustomer by status: " +
    // e.getMessage());
    // }
    // }

    public ApiResponse<String> delete(Long id) {
        if (this.repository.existsByIdAndDeletedDateIsNotNull(id)
                ) {
            if(this.repository.existsByIdAndStatus(id, VipStatus.EXPIRED)){
                throw new AlreadyExistException("VipCustomer with id: " + id + " is already expired");
            }
            throw new AlreadyExistException("VipCustomer with id: " + id + " is already deleted");
        }

        VipCustomerEntity entity = this.repository.findById(id).orElseThrow(
                () -> new NotFoundException("VipCustomer not found with id: " + id));
        try {

            String username = SecurityContextHolder.getContext().getAuthentication().getName();

            entity.setDeletedBy(username);
            entity.setStatus(VipStatus.DELETED);
            entity.setDeletedDate(LocalDateTime.now());
            VipCustomerEntity saved = this.repository.save(entity);
            // Initialize lazy relationships
            Hibernate.initialize(saved.getCustomer());
            Hibernate.initialize(saved.getDiscounts());
            return ApiResponse.success("VipCustomer deleted successfully");
        } catch (Exception e) {
            throw new RuntimeException("Error when delete VipCustomer: " + e.getMessage());
        }
    }

    public VipCustomerEntity findOne(Long id) {
        if (this.repository.existsByIdAndDeletedDateIsNull(id)) {
            throw new AlreadyExistException("VipCustomer isn't deleted");
        }
        VipCustomerEntity entity = this.repository.findByIdAndDeletedDateIsNotNull(id).orElseThrow(
                () -> new NotFoundException("VipCustomer not found"));
        // Initialize lazy relationships
        Hibernate.initialize(entity.getCustomer());
        Hibernate.initialize(entity.getDiscounts());
        try {

            return entity;
        } catch (Exception e) {
            // TODO: handle exception
            throw new RuntimeException("Error when find VipCustomer: " + e.getMessage());
        }
    }

    @Transactional
    public ApiResponse<String> restore(Long id) {
        VipCustomerEntity entity = this.findOne(id);
        try {

            entity.setDeletedBy(null);
            entity.setDeletedDate(null);
            entity.setStatus(VipStatus.ACTIVE);
            VipCustomerEntity saved = this.repository.save(entity);
            // Initialize lazy relationships
            Hibernate.initialize(saved.getCustomer());
            Hibernate.initialize(saved.getDiscounts());
            return ApiResponse.success("VipCustomer restored successfully");
        } catch (Exception e) {
            // TODO: handle exception
            throw new RuntimeException("Error when restore VipCustomer: " + e.getMessage());
        }
    }

    @Transactional(readOnly = true)
    public ApiResponse<VipListCustomerResponse> search_filter(Map<String, String> allParams, String sort, String search,
            int page, int size, boolean isPage) {
        try {
            UpdateVipCustomerRequest filter = new UpdateVipCustomerRequest();
            allParams.forEach((key, value) -> {
                try {
                    VipCustomerGroupBy groupBy = VipCustomerGroupBy.valueOf(key.toUpperCase());
                    groupBy.applyFilter(filter, value);
                } catch (Exception e) {
                    key = key.toUpperCase();
                    switch (key) {
                        case "CODE" -> filter.setCode(value);
                        case "POINTS" -> filter.setPoints(Integer.parseInt(value));
                        case "TYPE_VIP" -> filter.setType_vip(value);
                        case "STATUS" -> filter.setStatus(value);
                        case "CREATED_DATE" -> filter.setCreatedDate(value);
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

                    sorts.add(new Sort.Order(direction.equals("ASC") ? Sort.Direction.ASC : Sort.Direction.DESC, field));
                }
            }

            Specification<VipCustomerEntity> spec = (root, query, cb) -> {
                List<Predicate> predicates = new ArrayList<>();

                if (search != null && !search.isBlank()) {
                    List<Predicate> searchPredicates = new ArrayList<>();
                    String searchPattern = "%" + search.toLowerCase() + "%";

                    searchPredicates.add(cb.like(cb.lower(root.get("code")), searchPattern));

                    var customerJoin = root.join("customer", jakarta.persistence.criteria.JoinType.LEFT);
                    searchPredicates.add(cb.like(cb.lower(customerJoin.get("firstName")), searchPattern));
                    searchPredicates.add(cb.like(cb.lower(customerJoin.get("lastName")), searchPattern));
                    predicates.add(cb.or(searchPredicates.toArray(new Predicate[0])));
                }

                if (filter.getCode() != null && !filter.getCode().isBlank()) {
                    predicates.add(cb.like(cb.lower(root.get("code")), "%" + filter.getCode().toLowerCase() + "%"));
                }

                if (filter.getPoints() != null) {
                    predicates.add(cb.equal(root.get("points"), filter.getPoints()));
                }

                if (filter.getType_vip() != null && !filter.getType_vip().isBlank()) {
                    predicates.add(cb.equal(root.get("typeVip"), Type_vip.valueOf(filter.getType_vip().toUpperCase())));
                }

                if (filter.getStatus() != null && !filter.getStatus().isBlank()) {
                    predicates.add(cb.equal(root.get("status"), VipStatus.valueOf(filter.getStatus().toUpperCase())));
                }

                if (filter.getCreatedDate() != null && !filter.getCreatedDate().isBlank()) {
                    DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                    LocalDate createdDate = LocalDate.parse(filter.getCreatedDate().trim(), dateTimeFormatter);
                    predicates.add(cb.greaterThanOrEqualTo(root.get("createdDate"), createdDate.atStartOfDay()));
                    predicates.add(cb.lessThan(root.get("createdDate"), createdDate.plusDays(1).atStartOfDay()));
                }

                return predicates.isEmpty() ? cb.conjunction() : cb.and(predicates.toArray(new Predicate[0]));
            };

            Pageable pageable;
            if (isPage) {
                pageable = PageRequest.of(page - 1, size, Sort.by(sorts));
            } else {
                pageable = PageRequest.of(0, Integer.MAX_VALUE, Sort.by(sorts));
            }

            List<VipCustomerEntity> results = this.repository.findAll(spec, pageable).getContent();
            results.forEach(vip -> {
                Hibernate.initialize(vip.getCustomer());
                Hibernate.initialize(vip.getDiscounts());
            });
            return ApiResponse.success(vipListCustomerResponse(results));
        } catch (Exception e) {
            throw new RuntimeException("Error when search VipCustomer: " + e.getMessage());
        }
    }

    private VipListCustomerResponse vipListCustomerResponse(List<VipCustomerEntity> vipCustomerEntities) {
        int totalVipCustomers = (int) repository.count();
        int ActiveVipCustomers = (int) repository.countByStatus(VipStatus.ACTIVE);
        int expiredVipCustomers =  (int) repository.countByStatus(VipStatus.EXPIRED);
        int DeletedVipCustomers = (int) repository.countByDeletedDateIsNotNull();
        int silver = repository.countByTypeVipAndDeletedDateIsNullAndStatusIsNot(Type_vip.SILVER, VipStatus.EXPIRED);
        int gold = repository.countByTypeVipAndDeletedDateIsNullAndStatusIsNot(Type_vip.GOLD, VipStatus.EXPIRED);
        int platinum = repository.countByTypeVipAndDeletedDateIsNullAndStatusIsNot(Type_vip.PLATINUM, VipStatus.EXPIRED);
        int diamond = repository.countByTypeVipAndDeletedDateIsNullAndStatusIsNot(Type_vip.DIAMOND, VipStatus.EXPIRED);

        return VipListCustomerResponse.builder()
                .totalVipCustomers(totalVipCustomers)
                .activeVipCustomers(ActiveVipCustomers)
                .expiredVipCustomers(expiredVipCustomers)
                .deletedVipCustomers(DeletedVipCustomers)
                .silver(silver)
                .gold(gold)
                .platinum(platinum)
                .diamond(diamond)
                .vipCustomerResponses(vipCustomerEntities.stream().map(VipCustomerResponse::fromEntity).toList())
                .build();
    }
}
