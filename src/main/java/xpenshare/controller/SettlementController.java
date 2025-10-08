package xpenshare.controller;

import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.*;
import jakarta.validation.Valid;
import xpenshare.model.dto.settlement.CreateSettlementRequest;
import xpenshare.model.dto.settlement.SettlementDto;
import xpenshare.service.SettlementService;
import xpenshare.model.dto.settlement.SuggestSettlementsRequest;
import xpenshare.model.dto.settlement.SuggestSettlementsResponse;


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

    @Post("/{settlementId}/confirm")
    public HttpResponse<SettlementDto> confirmSettlement(@PathVariable Long settlementId) {
        return HttpResponse.ok(settlementService.confirmSettlement(settlementId));
    }

    @Post("/{settlementId}/cancel")
    public HttpResponse<SettlementDto> cancelSettlement(@PathVariable Long settlementId) {
        return HttpResponse.ok(settlementService.cancelSettlement(settlementId));
    }



    @Post("/groups/{groupId}/settlements/suggest")
    public HttpResponse<SuggestSettlementsResponse> suggestSettlements(
            @PathVariable Long groupId,
            @Body SuggestSettlementsRequest request) {
        return HttpResponse.ok(settlementService.suggestMinimalSettlements(groupId, request));
    }


}
