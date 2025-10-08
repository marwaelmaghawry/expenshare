package xpenshare.controller;

import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.*;
import jakarta.validation.Valid;
import xpenshare.model.dto.group.*;
import xpenshare.service.GroupService;
import java.math.BigDecimal;
import java.util.Map;
import xpenshare.service.SettlementService;
import java.util.Optional;

import java.util.List;


@Controller("/api/groups")
public class GroupController {

    private final GroupService groupService;
    private final SettlementService settlementService;


    public GroupController(GroupService groupService, SettlementService settlementService) {
        this.groupService = groupService;
        this.settlementService = settlementService;

    }

    @Post
    public HttpResponse<GroupDto> createGroup(@Body @Valid CreateGroupRequest request) {
        return HttpResponse.created(groupService.createGroup(request));
    }


    @Get
    public HttpResponse<List<GroupDto>> getAllGroups() {
        return HttpResponse.ok(groupService.getAllGroups());
    }


    @Get("/{groupId}")
    public HttpResponse<GroupDto> getGroup(Long groupId) {
        return HttpResponse.ok(groupService.getGroup(groupId));
    }


    @Post("/{groupId}/members")
    public HttpResponse<Map<String, Object>> addMembers(@PathVariable Long groupId,
                                                        @Body AddMembersRequest request) {
        return HttpResponse.ok(groupService.addMembers(groupId, request));
    }

    @Get("/{groupId}/balances")
    public HttpResponse<GroupBalanceResponse> getGroupBalances(Long groupId) {
        return HttpResponse.ok(groupService.getGroupBalances(groupId));
    }

    @Get("/{groupId}/settlements")
    public HttpResponse<?> listGroupSettlements(
            @PathVariable Long groupId,
            @QueryValue Optional<String> status,
            @QueryValue Optional<Long> fromUserId,
            @QueryValue Optional<Long> toUserId,
            @QueryValue(defaultValue = "0") int page,
            @QueryValue(defaultValue = "20") int size
    ) {
        var result = settlementService.listGroupSettlements(groupId, status, fromUserId, toUserId, page, size);
        return HttpResponse.ok(result);
    }




}
