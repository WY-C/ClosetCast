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

    private final List<Cloth> outer = List.of(
            Cloth.PUFFER_JACKET,
            Cloth.COAT,
            Cloth.FLEECE,
            Cloth.JACKET,
            Cloth.WINDBREAKER
    );

    private final List<Cloth> top = List.of(
            Cloth.SWEATER,
            Cloth.HOODIE,
            Cloth.SHIRT,
            Cloth.LONG_SLEEVE,
            Cloth.SHORT_SLEEVE
    );

    private final List<Cloth> bottom = List.of(
            Cloth.JEANS,
            Cloth.COTTON_PANTS,
            Cloth.SHORTS
    );

    @Transactional
    public SignUpResponseDto signUp(SignUpRequestDto signupDto) {
        if (memberRepository.existsByLoginId(signupDto.loginId())) {
            throw new MemberHandler(ErrorStatus.DUPLICATED_ID);
        }

        String encodedPassword = passwordEncoder.encode(signupDto.password());

        Member member = Member.builder()
                .name(signupDto.name())
                .loginId(signupDto.loginId())
                .password(encodedPassword)
                .preference(signupDto.preference())
                .tendencies(signupDto.tendencies())
                .build();

        memberRepository.save(member);

        return SignUpResponseDto.of(
                member.getName(), member.getLoginId(), member.getPassword(), member.getPreferences(), member.getTendencies(), member.getId()
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

        return SignInResponseDto.of(member.getId(),member.getName(), member.getLoginId(), token);
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

        if (requestDto.newPassword() != null) {
            if (!passwordEncoder.matches(requestDto.password(), member.getPassword())) {
                throw new MemberHandler(ErrorStatus.INVALID_ID_OR_PASSWORD);
            }

            String encodedPassword = passwordEncoder.encode(requestDto.newPassword());
            member.setPassword(encodedPassword);
        }
        if (requestDto.preference() != null) {
            member.setPreferences(requestDto.preference());
        }
        if (requestDto.tendencies() != null) {
            member.setTendencies(requestDto.tendencies());
        }
        if (requestDto.clothes() != null) {

            if (requestDto.clothes().stream().noneMatch(outer::contains)) {
                throw new MemberHandler(ErrorStatus.INVALID_CLOTHES);
            }

            if (requestDto.clothes().stream().noneMatch(top::contains)) {
                throw new MemberHandler(ErrorStatus.INVALID_CLOTHES);
            }

            if (requestDto.clothes().stream().noneMatch(bottom::contains)) {
                throw new MemberHandler(ErrorStatus.INVALID_CLOTHES);
            }

            member.setClothes(requestDto.clothes());
        }

        return MemberUpdateResponseDto.of(member.getId(), member.getPassword(), member.getPreferences(), member.getTendencies(), member.getClothes());
    }

    @Transactional
    public MemberDto deleteMemberById(Long memberId) {
        Member member = memberRepository.findById(memberId).orElseThrow(
                () -> new MemberHandler(ErrorStatus.MEMBER_NOT_FOUND)
        );

        MemberDto dto = MemberDto.of(member.getId(), member.getName(), member.getLoginId(), member.getPreferences(), member.getTendencies(), member.getClothes());
        memberRepository.deleteById(memberId);

        return dto;
    }
}
