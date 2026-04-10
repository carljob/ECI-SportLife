package com.sportlife.dtos.response;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ApiErrorResponse {
    private String code;
    private String message;
    private LocalDateTime timestamp;
}

