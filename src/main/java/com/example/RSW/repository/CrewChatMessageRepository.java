package com.example.RSW.repository;

import com.example.RSW.vo.CrewChatMessage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface CrewChatMessageRepository {
    void insertMessage(CrewChatMessage message);
    List<CrewChatMessage> getMessagesByCrewId(@Param("crewId") int crewId);
}
