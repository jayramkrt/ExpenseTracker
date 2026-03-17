package com.rotopay.expensetracker.service;

import com.rotopay.expensetracker.api.v1.mapper.StatementMapper;
import com.rotopay.expensetracker.api.v1.response.StatementResponseV1;
import com.rotopay.expensetracker.api.v1.request.StatementUploadRequestV1;
import com.rotopay.expensetracker.entity.BankStatement;
import com.rotopay.expensetracker.entity.ProcessingQueue;
import com.rotopay.expensetracker.api.common.exception.ResourceNotFoundException;
import com.rotopay.expensetracker.api.common.exception.UnauthorizedException;
import com.rotopay.expensetracker.repository.BankStatementRepository;
import com.rotopay.expensetracker.repository.ProcessingQueueRepository;
import com.rotopay.expensetracker.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class BankStatementService {

    private final StatementMapper statementMapper;
    private final ProcessingQueueRepository processingQueueRepository;
    private final BankStatementRepository bankStatementRepository;
    private final TransactionRepository transactionRepository;
    @Value("${pfa.file.upload-dir:./data/statements}")
    private String uploadDirectory;

    /**
     * Upload a bank statement or CC statement and initiate async processing
     * Validate file, stores it locally, and queues extraction job.
     * @param statementUploadRequestV1
     * @return
     */
    @Transactional
    public StatementResponseV1 uploadStatement(StatementUploadRequestV1 statementUploadRequestV1){
        log.info("Uploading statement :: {} for user :: {}", statementUploadRequestV1.getFile().getOriginalFilename(), statementUploadRequestV1.getUserId());

        validateFile(statementUploadRequestV1.getFile());

        try {
            Path uploadPath = Paths.get(uploadDirectory, statementUploadRequestV1.getUserId().toString());
            Files.createDirectories(uploadPath);

            //Save file with timestamp to avoid collisions
            String fileName = System.currentTimeMillis() + "_" + statementUploadRequestV1.getFile().getOriginalFilename();
            Path filePath = uploadPath.resolve(fileName);
            Files.write(filePath, statementUploadRequestV1.getFile().getBytes());

            // Create bank statement record
            //need to check for start time and end time for filter
            BankStatement bankStatement = BankStatement.builder().userId(statementUploadRequestV1.getUserId())
                    .fileName(statementUploadRequestV1.getFile().getOriginalFilename())
                    .bankName(statementUploadRequestV1.getBankName())
                    .accountType(statementUploadRequestV1.getAccountType())
                    .filePath(filePath.toString())
                    .processingStatus("pending")
                    .transactionCount(0)
                    .build();

            BankStatement savedBankStatement = bankStatementRepository.save(bankStatement);

            log.info("Statement created with ID:: {} and status :: {}", savedBankStatement.getId(), savedBankStatement.getProcessingStatus());

            // Queue async processing jobs
            queueProcessingJobs(savedBankStatement);
            return statementMapper.toResponse(savedBankStatement);
        } catch (IOException e) {
            log.error("Error while uploading file :: {}", statementUploadRequestV1.getFile().getOriginalFilename(), e);
            throw new RuntimeException("Failed to upload file :: " + e.getMessage());
        }
    }

    private void queueProcessingJobs(BankStatement bankStatement){
        ProcessingQueue processingQueueJob = ProcessingQueue.builder()
                .statementId(bankStatement.getId())
                .userId(bankStatement.getUserId())
                .jobType("extract_pdf")
                .maxRetries(3)
                .status("pending")
                .retryCount(0)
                .build();
        processingQueueRepository.save(processingQueueJob);

        log.debug("Queue PDF extraction job for statement :: {}", bankStatement.getId());
    }

    private void validateFile(MultipartFile file){
        if(file.isEmpty()){
            throw new IllegalArgumentException("File cannot be empty");
        }
        if ( !"application/pdf".equals(file.getContentType())){
            throw new IllegalArgumentException("Only PDF files are supported");
        }

        //Max size 50MB
        if (file.getSize() > 50 * 1024 * 1024) {
            throw new IllegalArgumentException("File size cannot exceed 50MB");
        }
    }

    public Page<StatementResponseV1> getUserStatements(UUID userId, Pageable pageable){
        log.debug("getting statements for userId :: {}", userId);

        Page<BankStatement> bankStatements = bankStatementRepository.findByUserIdOrderByUploadedAtDesc(userId, pageable);
        return bankStatements.map(statementMapper::toResponse);
    }


    /**
     * Get a specific statement by ID (with user authorization check).
     */
    @Transactional(readOnly = true)
    public StatementResponseV1 getStatement(UUID statementId, UUID userId){
        log.debug("fetching statement for id :: {} userId :: {}", statementId, userId);

        BankStatement bankStatement = bankStatementRepository.findById(statementId).orElseThrow(()->new ResourceNotFoundException("Statement not found: " + statementId));

        if(!bankStatement.getUserId().equals(userId)){
            throw new UnauthorizedException("User not authorized to access this statement");
        }

        return statementMapper.toResponse(bankStatement);
    }

    /**
     * Retry processing a failed statement.
     * Resets status to pending and requeues jobs.
     */
    @Transactional
    public StatementResponseV1 retryProcessing(UUID statementId, UUID userId) {
        log.info("Retrying statement processing: {} for user: {}", statementId, userId);

        BankStatement statement = bankStatementRepository.findById(statementId)
                .orElseThrow(() -> new ResourceNotFoundException("Statement not found: " + statementId));

        if (!statement.getUserId().equals(userId)) {
            throw new UnauthorizedException("User not authorized to retry this statement");
        }

        if (!statement.isFailed()) {
            throw new IllegalStateException("Only failed statements can be retried");
        }

        // Reset status
        statement.setProcessingStatus("pending");
        statement.setErrorMessage(null);
        BankStatement updated = bankStatementRepository.save(statement);

        // Requeue jobs
        queueProcessingJobs(updated);

        return statementMapper.toResponse(updated);
    }

    @Transactional
    public void deleteStatement(UUID statementId, UUID userId){
        log.info("Going to delete statement :: {} for userId :: {}", statementId, userId);

        BankStatement statement = bankStatementRepository.findById(statementId)
                .orElseThrow(() -> new ResourceNotFoundException("Statement not found: " + statementId));

        // Verify ownership
        if (!statement.getUserId().equals(userId)) {
            throw new UnauthorizedException("User not authorized to delete this statement");
        }

        transactionRepository.findByStatementIdOrderByTransactionDateDesc(statementId)
                .forEach(transactionRepository::delete);

        //delete file
        try {
            Files.deleteIfExists(Paths.get(statement.getFilePath()));
        }
        catch (IOException e){
            log.warn("Could not delete file: {}", statement.getFilePath(), e);
        }

        // Delete statement
        bankStatementRepository.delete(statement);
        log.info("Statement {} deleted successfully", statementId);
    }




}
