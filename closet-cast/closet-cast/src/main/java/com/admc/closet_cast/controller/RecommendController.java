package com.admc.closet_cast.controller;

import com.admc.closet_cast.apiPayload.ApiResponse;
import com.admc.closet_cast.dto.RecommendDto;
import com.admc.closet_cast.service.RecommendService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/recommend")
@RequiredArgsConstructor
public class RecommendController {

    private final RecommendService recommendService;

    @GetMapping("/{memberId}")
    public ResponseEntity<ApiResponse<RecommendDto>> getRecommend(@PathVariable("memberId") Long memberId) throws Exception {
        return ResponseEntity.ok(ApiResponse.onSuccess(recommendService.getReply(memberId)));
    }
}
