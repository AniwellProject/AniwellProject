package com.example.RSW.repository;

import com.example.RSW.vo.LitterEvent;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Mapper
public interface LitterEventRepository {
    void insert(LitterEvent e);

    List<LitterEvent> findRecentByPet(@Param("petId") Long petId,
                                      @Param("limit") int limit);

    // ✅ 추가: 하루치 리스트
    List<LitterEvent> findByPetAndDate(@Param("petId") Long petId,
                                       @Param("from") LocalDateTime from,
                                       @Param("to") LocalDateTime to);

    // ✅ 추가: 하루 요약
    List<Map<String, Object>> countByPetAndDateGroupByType(@Param("petId") Long petId,
                                                           @Param("from") LocalDateTime from,
                                                           @Param("to") LocalDateTime to);
}
