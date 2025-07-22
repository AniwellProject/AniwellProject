package com.example.RSW.repository;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.example.RSW.vo.Reply;

@Mapper
public interface ReplyRepository {

    public List<Reply> getForPrintReplies(int loginedMemberId, String relTypeCode, int relId);

    public void writeReply(int loginedMemberId, String body, String relTypeCode, int relId);

    public int getLastInsertId();

    public Reply getReply(int id);

    public void modifyReply(int id, String body);

    void deleteReply(int id);

    int getGoodRP(int replyId);

    int getBadRP(int replyId);

    void increaseGoodReactionPoint(int replyId);

    void decreaseGoodReactionPoint(int replyId);

    void increaseBadReactionPoint(int replyId);

    void decreaseBadReactionPoint(int replyId);
}