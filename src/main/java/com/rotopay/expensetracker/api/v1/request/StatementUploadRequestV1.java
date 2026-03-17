package com.rotopay.expensetracker.api.v1.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StatementUploadRequestV1 {

    private MultipartFile file;

    private String bankName;

    private String accountType;

    private UUID userId;



}
