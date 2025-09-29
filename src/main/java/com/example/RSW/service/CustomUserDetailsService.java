package com.example.RSW.service;

import com.example.RSW.repository.MemberRepository;
import com.example.RSW.vo.Member;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private  MemberRepository memberRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public CustomUserDetailsService(MemberRepository memberRepository, PasswordEncoder passwordEncoder) {
        this.memberRepository = memberRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UserDetails loadUserByUsername(String loginId) throws UsernameNotFoundException {
        Member member = memberRepository.getMemberByLoginId(loginId);

        if (member == null) {
            throw new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + loginId);
        }

        return User.builder()
                .username(member.getLoginId())
                .password(member.getLoginPw()) // DB에 암호화된 비밀번호 저장되어야 함
                .roles(member.getAuthName()) // 권한(Role) 매핑
                .build();
    }


}