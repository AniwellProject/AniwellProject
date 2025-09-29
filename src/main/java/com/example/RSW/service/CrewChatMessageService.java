package com.example.RSW.service;

import com.example.RSW.repository.CrewChatMessageRepository;
import com.example.RSW.vo.CrewChatMessage;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CrewChatMessageService {

    private final CrewChatMessageRepository crewChatMessageRepository;

    public CrewChatMessageService(CrewChatMessageRepository repo) {
        this.crewChatMessageRepository = repo;
    }

    // 채팅 저장
    public void saveMessage(CrewChatMessage message) {
        crewChatMessageRepository.insertMessage(message);
    }

    // 크루 ID로 채팅 가죠오기(해당 채팅방의 메세지 전체 호출)
    public List<CrewChatMessage> getMessagesByCrewId(int crewId) {
        return crewChatMessageRepository.getMessagesByCrewId(crewId);
    }
}

