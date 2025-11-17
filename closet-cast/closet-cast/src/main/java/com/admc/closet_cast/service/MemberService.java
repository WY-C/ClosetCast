package com.admc.closet_cast.service;

import com.admc.closet_cast.apiPayload.exception.handler.MemberHandler;
import com.admc.closet_cast.apiPayload.form.status.ErrorStatus;
import com.admc.closet_cast.config.JwtProvider;
import com.admc.closet_cast.dto.*;
import com.admc.closet_cast.entity.Cloth;
import com.admc.closet_cast.entity.Member;
import com.admc.closet_cast.entity.Preference;
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
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class MemberService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;

    private final Map<String, Tendency> tendencyMap = Map.of(
            "더위를 많이 타요.", Tendency.HOT,
            "추위를 많이 타요.", Tendency.COLD
    );

    @Transactional
    public SignUpResponseDto signUp(SignUpRequestDto signupDto) {
        if (memberRepository.existsByLoginId(signupDto.loginId())) {
            throw new MemberHandler(ErrorStatus.DUPLICATED_ID);
        }

        String encodedPassword = passwordEncoder.encode(signupDto.password());
        List<Tendency> tendencies = new ArrayList<>();
        List<Preference> preferences = new ArrayList<>();

        for (String tendency : signupDto.tendencies()) {
            tendencies.add(tendencyMap.get(tendency));
        }

        for (String preference : signupDto.preference()) {
            preferences.add(Preference.fromString(preference));
        }

        Member member = Member.builder()
                .name(signupDto.name())
                .loginId(signupDto.loginId())
                .password(encodedPassword)
                .preference(preferences)
                .tendencies(tendencies)
                .build();

        memberRepository.save(member);

        return SignUpResponseDto.of(
                member.getName(), member.getLoginId(), member.getPassword(), member.getPreferences(), member.getTendencies()
        );
    }

    @Transactional
    public SignInResponseDto signIn(SignInRequestDto requestDto) {
        Member member = memberRepository.findByLoginId(requestDto.loginId())
                .orElseThrow(() -> new MemberHandler(ErrorStatus.INVALID_ID_OR_PASSWORD));

        if (!passwordEncoder.matches(requestDto.password(), member.getPassword())) {
            throw new MemberHandler(ErrorStatus.INVALID_ID_OR_PASSWORD);
        }

        String token = jwtProvider.createToken(member.getLoginId());

        return SignInResponseDto.of(String.valueOf(member.getId()), member.getName(), member.getLoginId(), token);
    }

    @Transactional(readOnly = true)
    public List<MemberDto> findAllMember() {
        List<Member> members = memberRepository.findAll();

        return members.stream()
                .map(member -> MemberDto.of(
                        member.getId(),
                        member.getName(),
                        member.getLoginId(),
                        member.getPreferences(),
                        member.getTendencies(),
                        member.getClothes())
                ).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public MemberDto findMemberById(Long memberId) {
        Member member = memberRepository.findById(memberId).orElseThrow(
                () -> new MemberHandler(ErrorStatus.MEMBER_NOT_FOUND)
        );

        return MemberDto.of(member.getId(), member.getName(), member.getLoginId(), member.getPreferences(), member.getTendencies(), member.getClothes());
    }

    @Transactional
    public MemberUpdateResponseDto updateMember(Long memberId, MemberUpdateRequestDto requestDto) {
        Member member = memberRepository.findById(memberId).orElseThrow(
                () -> new MemberHandler(ErrorStatus.MEMBER_NOT_FOUND)
        );

        if (requestDto.password() != null) {
            String encodedPassword = passwordEncoder.encode(requestDto.password());
            member.setPassword(encodedPassword);
        }
        if (requestDto.preference() != null) {
            List<Preference> preferences = new ArrayList<>();

            for  (String preference : requestDto.preference()) {
                preferences.add(Preference.fromString(preference));
            }
            member.setPreferences(preferences);
        }
        if (requestDto.tendencies() != null) {
            List<Tendency> tendencies = new ArrayList<>();

            for (String tendency : requestDto.tendencies()) {
                tendencies.add(tendencyMap.get(tendency));
            }
            member.setTendencies(tendencies);
        }
        if (requestDto.clothes() != null) {
            List<Cloth> clothes = new ArrayList<>();
            for (String cloth : requestDto.clothes()) {
                clothes.add(Cloth.valueOf(cloth));
            }
            member.setClothes(clothes);
        }

        return MemberUpdateResponseDto.of(member.getId(), member.getPassword(), member.getPreferences(), member.getTendencies(), member.getClothes());
    }

    @Transactional
    public MemberDto deleteMemberById(Long memberId) {
        Member member = memberRepository.findById(memberId).orElseThrow(
                () -> new MemberHandler(ErrorStatus.MEMBER_NOT_FOUND)
        );

        memberRepository.deleteById(memberId);

        return MemberDto.of(member.getId(), member.getName(), member.getLoginId(), member.getPreferences(), member.getTendencies(), member.getClothes());
    }
}
