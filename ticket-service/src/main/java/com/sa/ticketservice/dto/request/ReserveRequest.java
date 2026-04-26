package com.sa.ticketservice.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReserveRequest {

    @NotNull
    private Long ticketTypeId;

    @NotNull
    @Min(1)
    private Integer quantity;
}
