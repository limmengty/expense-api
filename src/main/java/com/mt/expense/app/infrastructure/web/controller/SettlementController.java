package com.mt.expense.app.infrastructure.web.controller;

import com.mt.expense.app.application.command.RecordSettlementCommand;
import com.mt.expense.app.application.port.in.RecordSettlementUseCase;
import com.mt.expense.app.domain.model.Settlement;
import com.mt.expense.app.domain.vo.GroupId;
import com.mt.expense.app.domain.vo.Money;
import com.mt.expense.app.domain.vo.UserId;
import com.mt.expense.app.infrastructure.web.dto.request.RecordSettlementRequest;
import com.mt.expense.app.infrastructure.web.dto.response.SettlementResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/settlements")
@Tag(name = "Settlements", description = "Record cash settlements between group members")
public class SettlementController {

    private static final Logger log = LoggerFactory.getLogger(SettlementController.class);

    private final RecordSettlementUseCase recordSettlementUseCase;

    public SettlementController(RecordSettlementUseCase recordSettlementUseCase) {
        this.recordSettlementUseCase = recordSettlementUseCase;
    }

    @Operation(
            summary = "Record settlement",
            description = "Record that one user has paid another to settle a debt within a group")
    @PostMapping
    public ResponseEntity<SettlementResponse> recordSettlement(
            @Valid @RequestBody RecordSettlementRequest request) {

        log.info(
                "Recording settlement: {} → {} in group {} for {}",
                request.fromUserId(),
                request.toUserId(),
                request.groupId(),
                request.amount());

        RecordSettlementCommand command =
                new RecordSettlementCommand(
                        GroupId.of(request.groupId()),
                        UserId.of(request.fromUserId()),
                        UserId.of(request.toUserId()),
                        Money.of(request.amount(), request.resolvedCurrency()));

        Settlement settlement = recordSettlementUseCase.record(command);

        return ResponseEntity.status(HttpStatus.CREATED).body(SettlementResponse.from(settlement));
    }
}
