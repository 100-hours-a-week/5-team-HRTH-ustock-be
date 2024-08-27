package com.hrth.ustock.service;

import com.hrth.ustock.dto.news.NewsResponseDto;
import com.hrth.ustock.entity.portfolio.Holding;
import com.hrth.ustock.entity.portfolio.News;
import com.hrth.ustock.exception.HoldingNotFoundException;
import com.hrth.ustock.repository.HoldingRepository;
import com.hrth.ustock.repository.NewsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class NewsService {

    private final NewsRepository newsRepository;
    private final HoldingRepository holdingRepository;

    public List<NewsResponseDto> findHoldingNews(Long userId) {
        List<Holding> holdings = holdingRepository.findAllByUserUserId(userId);

        if (holdings == null || holdings.isEmpty()) {
            throw new HoldingNotFoundException();
        }

        List<News> newsList = new LinkedList<>();

        for (Holding holding : holdings) {
            String stockCode = holding.getStock().getCode();
            newsList.addAll(newsRepository.findTop15ByStockCodeOrderByDateDesc(stockCode));
        }

        Collections.shuffle(newsList);
        int lastIndex = 15;
        if (newsList.size() < 15) {
            lastIndex = newsList.size()-1;
        }
        return newsList.subList(0, lastIndex).stream()
                .map(News::toResponseDto)
                .toList();
    }
}
