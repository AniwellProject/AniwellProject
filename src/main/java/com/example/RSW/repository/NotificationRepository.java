package com.example.RSW.repository;

import com.example.RSW.vo.Notification;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;
import java.util.Optional;


@Mapper
public interface NotificationRepository {

    List<Notification> findByMemberIdOrderByRegDateDesc(@Param("memberId") int memberId);

    boolean existsByMemberIdAndTitleAndLink(@Param("memberId") int memberId, @Param("title") String title, @Param("link") String link);

    int save(Notification notification);

    Optional<Notification> findById(int notificationId);

    boolean existsByMemberIdAndRelTypeCodeAndRelId(int memberId, String relTypeCode, int relId);

    void delete(int memberId, String relTypeCode, int relId);

    void insert(int memberId, String relTypeCode, int relId);

    void insert(Notification notification);

    int countUnreadByMemberId(int loginedMemberId);

    void updateAllAsReadByMemberId(int loginedMemberId);

    void deleteById(int id, int memberId);

    int deleteByLinkAndTitle(int memberId, String link, String title);

    void deleteByIdOnlyId(int id);

    void deleteByMemberId(int memberId);

    int update(Notification notification);

    void deleteAllByMemberId(int loginedMemberId);

    void deleteByLink(String link);
}
