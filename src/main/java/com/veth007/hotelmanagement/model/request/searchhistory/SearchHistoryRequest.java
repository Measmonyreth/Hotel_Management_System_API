package com.veth007.hotelmanagement.model.request.searchhistory;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SearchHistoryRequest implements Serializable {

    private String customerId;
    private String keyword;
}
