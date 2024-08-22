package com.hrth.ustock.service;

import com.hrth.ustock.dto.news.NewsResponseDto;
import com.hrth.ustock.entity.portfolio.Holding;
import com.hrth.ustock.repository.HoldingRepository;
import com.hrth.ustock.repository.NewsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NewsService {

    private final NewsRepository newsRepository;
    private final HoldingRepository holdingRepository;

    public List<NewsResponseDto> findHoldingNews(Long userId) {
        List<Holding> holdings = holdingRepository.findAllByUserUserId(userId);

        List<String> codes = new ArrayList<>();
        List<NewsResponseDto> newsList = new ArrayList<>();

        for (Holding holding : holdings) {
            codes.add(holding.getStock().getCode());
        }

        newsRepository.findTop15ByStockCodeInOrderByDateDesc(codes).forEach(news -> {
            newsList.add(news.toResponseDto());
        });

        return newsList;
    }
}
