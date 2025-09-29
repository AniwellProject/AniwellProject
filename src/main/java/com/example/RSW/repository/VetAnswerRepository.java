package com.example.RSW.repository;

import com.example.RSW.vo.VetAnswer;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;


@Mapper
public interface VetAnswerRepository {
    void write(@Param("qnaId") int qnaId,
               @Param("memberId") int memberId,
               @Param("vetName") String vetName,
               @Param("answer") String answer);

    List<VetAnswer> getByQnaId(int qnaId);

    VetAnswer getById(int id);

    void update(int id, String answer);

    void delete(int id);

    int getQnaIdByAnswerId(@Param("answerId") int answerId);

    VetAnswer findByQnaId(int qnaId);

    int insert(Map<String, Object> param);

}

