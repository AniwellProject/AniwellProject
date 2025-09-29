package com.example.RSW.repository;

import com.example.RSW.vo.WalkCrewMember;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CrewMemerRepository {
    boolean isCrewMember(int crewId, int memberId);

    WalkCrewMember getCrewMemberById(int crewId, int memberId);
}
