package com.example.RSW.repository;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.example.RSW.vo.Qna;

import java.util.List;

@Mapper
public interface QnaRepository {

    List<Qna> getFaqList();

    Qna getQnaById(int id);

    void saveUserQna(Qna qna);

    List<Qna> getUserQnaByMemberId(int memberId);

    void markQnaAsDeleted(int id);

    void updateQna(int id, String title, String body, boolean isSecret);


    int updateIsAnswered(@Param("isAnswered") boolean isAnswered, @Param("id") int id);

    List<Qna> findAll();

    Qna findById(int id);

    void update(@Param("id") int id, @Param("title") String title, @Param("body") String body);

    void delete(@Param("id") int id);

    void modify(int id, String title, String body);

    void insert(int memberId, String title, String body, boolean isSecret, boolean isFaq);

    List<Qna> getSelectedQna();

    List<Qna> getMyAnsweredQna(int loginedMemberId);

    List<Qna> findWithoutAnswer();

    void setFalse(int qnaId);
}