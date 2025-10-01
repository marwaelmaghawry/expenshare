package xpenshare.controller;

import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.*;
import jakarta.validation.Valid;
import xpenshare.model.dto.settlement.CreateSettlementRequest;
import xpenshare.model.dto.settlement.SettlementDto;
import xpenshare.service.SettlementService;

@Controller("/api/settlements")
public class SettlementController {

    private final SettlementService settlementService;

    public SettlementController(SettlementService settlementService) {
        this.settlementService = settlementService;
    }

    @Post
    public HttpResponse<SettlementDto> createSettlement(@Body @Valid CreateSettlementRequest request) {
        return HttpResponse.created(settlementService.createSettlement(request));
    }
}
