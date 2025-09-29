package com.example.RSW.service;

import com.example.RSW.repository.CrewChatMessageRepository;
import com.example.RSW.repository.CrewMemerRepository;
import com.example.RSW.vo.WalkCrewMember;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CrewMemerService {

    @Autowired
    private CrewMemerRepository crewMemerRepository;

    public boolean isCrewMember(int crewId, int loginedMemberId) {
        return crewMemerRepository.isCrewMember(crewId, loginedMemberId);
    }

    public WalkCrewMember getCrewMemberById(int crewId, int memberId) {
        return crewMemerRepository.getCrewMemberById(crewId, memberId);
    }
}
