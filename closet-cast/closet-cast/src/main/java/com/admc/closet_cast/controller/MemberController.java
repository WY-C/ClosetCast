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

    @GetMapping("/member")
    public ResponseEntity<ApiResponse<List<MemberDto>>> findAllMembers() {
        return ResponseEntity.ok(ApiResponse.onSuccess(memberService.findAllMember()));
    }
}
