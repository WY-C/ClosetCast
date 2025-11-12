package com.admc.closet_cast.controller;

import com.admc.closet_cast.apiPayload.ApiResponse;
import com.admc.closet_cast.dto.*;
import com.admc.closet_cast.service.MemberService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/member")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    @Operation(summary = "회원가입", description = "회원가입을 합니다.")
    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<SignUpResponseDto>> signUp(@RequestBody SignUpRequestDto signupDto) {
        return ResponseEntity.ok(ApiResponse.onSuccess(memberService.signUp(signupDto)));
    }

    @Operation(summary = "로그인", description = "로그인을 합니다.")
    @PostMapping("/signin")
    public ResponseEntity<ApiResponse<SignInResponseDto>> signIn(@RequestBody SignInRequestDto signinDto) {
        return ResponseEntity.ok(ApiResponse.onSuccess(memberService.signIn(signinDto)));
    }

    @Operation(summary = "전체 사용자 조회", description = "전체 사용자를 조회합니다.")
    @GetMapping("/read")
    public ResponseEntity<ApiResponse<List<MemberDto>>> findAllMembers() {
        return ResponseEntity.ok(ApiResponse.onSuccess(memberService.findAllMember()));
    }

    @Operation(summary = "개별 사용자 조회", description = "member Id로 개별 사용자를 조회합니다.")
    @GetMapping("/read/{memberId}")
    public ResponseEntity<ApiResponse<MemberDto>> findMemberById(@PathVariable("memberId") Long memberId) {
        return ResponseEntity.ok(ApiResponse.onSuccess(memberService.findMemberById(memberId)));
    }

    @Operation(summary = "사용자 정보 수정", description = "사용자의 비밀번호, 경향, 선호 스타일, 보유 옷을 수정합니다.")
    @PatchMapping("/update/{memberId}")
    public ResponseEntity<ApiResponse<MemberUpdateResponseDto>> updateMember(@PathVariable("memberId") Long memberId, @RequestBody MemberUpdateRequestDto memberUpdateRequestDto) {
        return ResponseEntity.ok(ApiResponse.onSuccess(memberService.updateMember(memberId, memberUpdateRequestDto)));
    }

    @Operation(summary = "사용자 삭제", description = "사용자를 삭제합니다.")
    @DeleteMapping("/delete/{memberId}")
    public ResponseEntity<ApiResponse<MemberDto>> deleteMember(@PathVariable("memberId") Long memberId) {
        return ResponseEntity.ok(ApiResponse.onSuccess(memberService.deleteMemberById(memberId)));
    }
}
