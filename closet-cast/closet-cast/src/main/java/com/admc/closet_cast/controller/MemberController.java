package com.admc.closet_cast.controller;

import com.admc.closet_cast.apiPayload.ApiResponse;
import com.admc.closet_cast.dto.*;
import com.admc.closet_cast.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/member")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<SignUpResponseDto>> signUp(@RequestBody SignUpRequestDto signupDto) {
        return ResponseEntity.ok(ApiResponse.onSuccess(memberService.signUp(signupDto)));
    }

    @PostMapping("/signin")
    public ResponseEntity<ApiResponse<SignInResponseDto>> signIn(@RequestBody SignInRequestDto signinDto) {
        return ResponseEntity.ok(ApiResponse.onSuccess(memberService.signIn(signinDto)));
    }

    @GetMapping("/read")
    public ResponseEntity<ApiResponse<List<MemberDto>>> findAllMembers() {
        return ResponseEntity.ok(ApiResponse.onSuccess(memberService.findAllMember()));
    }

    @GetMapping("/read/{memberId}")
    public ResponseEntity<ApiResponse<MemberDto>> findMemberById(@PathVariable("memberId") Long memberId) {
        return ResponseEntity.ok(ApiResponse.onSuccess(memberService.findMemberById(memberId)));
    }

    @PatchMapping("/update/{memberId}")
    public ResponseEntity<ApiResponse<MemberUpdateResponseDto>> updateMember(@PathVariable("memberId") Long memberId, @RequestBody MemberUpdateRequestDto memberUpdateRequestDto) {
        return ResponseEntity.ok(ApiResponse.onSuccess(memberService.updateMember(memberId, memberUpdateRequestDto)));
    }

    @DeleteMapping("/delete/{memberId}")
    public ResponseEntity<ApiResponse<MemberDto>> deleteMember(@PathVariable("memberId") Long memberId) {
        return ResponseEntity.ok(ApiResponse.onSuccess(memberService.deleteMemberById(memberId)));
    }
}
