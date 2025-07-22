package com.example.RSW.repository;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ReactionPointRepository {

    public int getSumReactionPoint(int memberId, String relTypeCode, int relId);

    public int addGoodReactionPoint(int memberId, String relTypeCode, int relId);

    public int addBadReactionPoint(int memberId, String relTypeCode, int relId);

    public void deleteReactionPoint(int memberId, String relTypeCode, int relId);

    boolean existsByMemberIdAndRelTypeCodeAndRelId(int memberId, String relTypeCode, int relId);

    void delete(int memberId, String relTypeCode, int relId);

    void insert(int memberId, String relTypeCode, int relId);

    int getReactionPoint(String relTypeCode, int relId, int point);


}