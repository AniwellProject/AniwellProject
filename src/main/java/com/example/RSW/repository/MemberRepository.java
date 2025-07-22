package com.example.RSW.repository;

import org.apache.ibatis.annotations.Mapper;

import com.example.RSW.vo.Member;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;

@Mapper
public interface MemberRepository {

    int doJoin(String loginId, String loginPw, String name, String nickname, String cellphone,
               String email, String address, String authName, int authLevel);

    public Member getMemberById(int id);

    public int getLastInsertId();

    public Member getMemberByLoginId(String loginId);

    public Member getMemberByNameAndEmail(String name, String email);

    public void modify(int loginedMemberId, String loginPw, String name, String nickname, String cellphone,
                       String email, String photo);

    public void modifyWithoutPw(int loginedMemberId, String name, String nickname, String cellphone, String email, String photo, String address);

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

}