package xpenshare.controller;

import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.*;
import jakarta.validation.Valid;
import xpenshare.model.dto.group.*;
import xpenshare.service.GroupService;

@Controller("/api/groups")
public class GroupController {

    private final GroupService groupService;

    public GroupController(GroupService groupService) {
        this.groupService = groupService;
    }

    @Post
    public HttpResponse<GroupDto> createGroup(@Body @Valid CreateGroupRequest request) {
        return HttpResponse.created(groupService.createGroup(request));
    }

    @Get("/{groupId}")
    public HttpResponse<GroupDto> getGroup(Long groupId) {
        return HttpResponse.ok(groupService.getGroup(groupId));
    }

    @Post("/{groupId}/members")
    public HttpResponse<GroupDto> addMembers(Long groupId, @Body @Valid AddMembersRequest request) {
        return HttpResponse.ok(groupService.addMembers(groupId, request));
    }
}
