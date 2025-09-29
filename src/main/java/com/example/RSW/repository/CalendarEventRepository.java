package com.example.RSW.repository;

import com.example.RSW.vo.CalendarEvent;
import com.example.RSW.vo.ResultData;
import org.apache.ibatis.annotations.Mapper;

import java.time.LocalDate;
import java.util.List;

@Mapper
public interface CalendarEventRepository {
    int insert(int memberId, LocalDate eventDate, String title, int petId, String content);
    int update(int id, LocalDate eventDate, String title, String content);
    void delete(int id);
    List<CalendarEvent> findByMemberId(int memberId);
    List<CalendarEvent> findByPetId(int petId);

    CalendarEvent getEventsById(int id);

    List<CalendarEvent> getEventByDateAndPetId(LocalDate eventDate, int petId);
}
