package com.example.demo.service;

import com.example.demo.dto.RankingGlobalResponse;
import com.example.demo.dto.RankingDiarioResponse;

public interface RankingService {
    RankingGlobalResponse getRankingGlobal();
    RankingDiarioResponse getRankingDiario();
}