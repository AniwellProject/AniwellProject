package com.example.RSW.repository;

import org.apache.ibatis.annotations.Mapper;

import com.example.RSW.vo.Member;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;

@Mapper
public interface MemberRepository {

    int doJoin(@Param("loginId") String loginId, @Param("loginPw") String loginPw, @Param("name") String name, @Param("nickname") String nickname,
               @Param("cellphone") String cellphone,
               @Param("email") String email,
               @Param("address") String address,
               @Param("authName") String authName,
               @Param("authLevel") int authLevel);

    Member getMemberById(int id);

    int getLastInsertId();

    Member getMemberByLoginId(String loginId);

    Member getMemberByNameAndEmail(@Param("name") String name, @Param("email") String email);

    int modify(
            @Param("loginedMemberId") int loginedMemberId,
            @Param("loginPw") String loginPw,
            @Param("name") String name,
            @Param("nickname") String nickname,
            @Param("cellphone") String cellphone,
            @Param("email") String email,
            @Param("photo") String photo,
            @Param("address") String address
    );

    int modifyWithoutPw(
            @Param("loginedMemberId") int loginedMemberId,
            @Param("name") String name,
            @Param("nickname") String nickname,
            @Param("cellphone") String cellphone,
            @Param("email") String email,
            @Param("photo") String photo,
            @Param("address") String address
    );

    void withdraw(int id);

    List<Member> findAll();

    void updateAuthLevel(int memberId, int authLevel);

    List<Member> getForPrintMembersWithCert(@Param("searchType") String searchType,
                                            @Param("searchKeyword") String searchKeyword);

    String getNicknameById(int loginedMemberId);

    void updateVetCertInfo(int memberId, String fileName, int approved);

    int countByAuthLevel(int level);

    List<Member> findByAuthLevel(int i);

    // 소셜 회원 조회
    Member getMemberBySocial(@Param("socialProvider") String socialProvider,
                             @Param("socialId") String socialId);

    // 소셜 회원 가입
    int doJoinBySocial(@Param("loginId") String loginId,
                       @Param("loginPw") String loginPw,
                       @Param("socialProvider") String socialProvider,
                       @Param("socialId") String socialId,
                       @Param("name") String name,
                       @Param("nickname") String nickname,
                       @Param("email") String email);

    Member findBySocialProviderAndSocialId(@Param("socialProvider") String socialProvider,
                                           @Param("socialId") String socialId);


    Member findByEmail(String email);

    void insert(Member member);

    List<Integer> getAllMemberIds();

    int updateUidById(@Param("id") int id, @Param("uid") String uid);

    Member findByUid(String uid);

    void updateSocialInfo(Member emailMember);

    Member getMemberByNickname(String nickname);

    Member getMemberByEmail(String email);

    Member getMemberByCellphone(String cellphone);
}