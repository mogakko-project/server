package com.example.mogakko.domain.meeting.controller;

import com.example.mogakko.domain.meeting.dto.CreateMeetingRequestDTO;
import com.example.mogakko.domain.meeting.dto.CreateMeetingResponseDTO;
import com.example.mogakko.domain.meeting.dto.MeetingDTO;
import com.example.mogakko.domain.meeting.service.MeetingService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class MeetingController {

    private final MeetingService meetingService;

    @PostMapping("/groups/{groupId}/meetings")
    public CreateMeetingResponseDTO createGroupMeeting(@PathVariable Long groupId, @RequestBody CreateMeetingRequestDTO createMeetingRequestDTO) {
        return meetingService.createMeeting(groupId, createMeetingRequestDTO);
    }

    @GetMapping("/groups/{groupId}/meetings")
    public List<MeetingDTO> getGroupMeetingList(@PathVariable Long groupId) {
        return meetingService.findMeetingListOfGroup(groupId);
    }

}