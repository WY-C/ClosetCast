package com.admc.closet_cast.service;

import com.admc.closet_cast.apiPayload.exception.handler.MemberHandler;
import com.admc.closet_cast.apiPayload.form.status.ErrorStatus;
import com.admc.closet_cast.dto.SignUpRequestDto;
import com.admc.closet_cast.dto.SignUpResponseDto;
import com.admc.closet_cast.entity.Member;
import com.admc.closet_cast.entity.Tendency;
import com.admc.closet_cast.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class MemberService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    private final Map<String, Tendency> tendencyMap = Map.of(
            "더위를 많이 타요.", Tendency.HOT,
            "추위를 많이 타요.", Tendency.WARM
    );

    @Transactional
    public SignUpResponseDto signUp(SignUpRequestDto signupDto) {
        if (memberRepository.existsByLoginId(signupDto.loginId())) {
            throw new MemberHandler(ErrorStatus.DUPLICATED_ID);
        }

        String encodedPassword = passwordEncoder.encode(signupDto.password());
        List<Tendency> tendencies = new ArrayList<>();
        log.info(signupDto.tendencies().toString());

        for (String tendency : signupDto.tendencies()) {
            tendencies.add(tendencyMap.get(tendency));
        }

        log.info("tendencies: " + tendencies);

        Member member = Member.builder()
                .name(signupDto.name())
                .loginId(signupDto.loginId())
                .password(encodedPassword)
                .preference(signupDto.preference())
                .tendencies(tendencies)
                .build();

        memberRepository.save(member);

        return SignUpResponseDto.of(
                member.getName(), member.getLoginId(), member.getPassword(), member.getPreference(), member.getTendencies()
        );
    }
}
