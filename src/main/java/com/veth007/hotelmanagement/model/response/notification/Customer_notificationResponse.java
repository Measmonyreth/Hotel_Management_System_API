package com.veth007.hotelmanagement.model.response.notification;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.veth007.hotelmanagement.model.entity.Customer_notificationEntity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Customer_notificationResponse implements Serializable {
        private Long id;
        private Map<String, Object> customer;
        private String readAt;

        static public Customer_notificationResponse fromEnitty(Customer_notificationEntity entity) {
                Map<String, Object> customer = null;
                if (entity.getCustomer() != null) {
                        customer = new HashMap<>();
                        customer.put("id", entity.getCustomer().getId());
                        customer.put("name", Stream.of(
                                        entity.getCustomer().getFirstName(),
                                        entity.getCustomer().getLastName())
                                        .filter(name -> name != null && !name.isBlank())
                                        .collect(Collectors.joining(" ")));
                        customer.put("email", entity.getCustomer().getEmail());
                }

                return Customer_notificationResponse.builder()
                                .id(entity.getId())
                                .customer(customer)
                                .readAt(entity.getReadAt() != null ? entity.getReadAt().toString() : null)
                                .build();
        }
}
