package com.example.demo.controller;

import com.example.demo.dto.RankingGlobalResponse;
import com.example.demo.dto.RankingDiarioResponse;
import com.example.demo.service.RankingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ranking")
@RequiredArgsConstructor
public class RankingController {

    private final RankingService rankingService;

    @GetMapping("/global")
    @PreAuthorize("permitAll()")
    public ResponseEntity<RankingGlobalResponse> getRankingGlobal() {
        return ResponseEntity.ok(rankingService.getRankingGlobal());
    }

    @GetMapping("/diario")
    @PreAuthorize("permitAll()")
    public ResponseEntity<RankingDiarioResponse> getRankingDiario() {
        return ResponseEntity.ok(rankingService.getRankingDiario());
    }
}