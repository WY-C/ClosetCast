package com.admc.closet_cast.controller;

import com.admc.closet_cast.apiPayload.ApiResponse;
import com.admc.closet_cast.dto.SignUpRequestDto;
import com.admc.closet_cast.dto.SignUpResponseDto;
import com.admc.closet_cast.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/member")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<SignUpResponseDto>> signUp(@RequestBody SignUpRequestDto signupDto) {
        return ResponseEntity.ok(ApiResponse.onSuccess(memberService.signUp(signupDto)));
    }
}
