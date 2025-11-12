package com.admc.closet_cast.controller;

import com.admc.closet_cast.apiPayload.ApiResponse;
import com.admc.closet_cast.dto.RecommendDto;
import com.admc.closet_cast.service.RecommendService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/recommend")
@RequiredArgsConstructor
public class RecommendController {

    private final RecommendService recommendService;

    @Operation(summary = "옷 추천 받기", description = "LLM으로부터 옷 추천을 받아옵니다.")
    @GetMapping("/{memberId}")
    public ResponseEntity<ApiResponse<RecommendDto>> getRecommend(@PathVariable("memberId") Long memberId) throws Exception {
        return ResponseEntity.ok(ApiResponse.onSuccess(recommendService.getReply(memberId)));
    }
}
