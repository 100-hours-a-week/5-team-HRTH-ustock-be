package com.hrth.ustock.service.main;

import com.hrth.ustock.dto.main.news.NewsResponseDto;
import com.hrth.ustock.entity.main.Holding;
import com.hrth.ustock.entity.main.News;
import com.hrth.ustock.exception.domain.portfolio.PortfolioException;
import com.hrth.ustock.repository.main.HoldingRepository;
import com.hrth.ustock.repository.main.NewsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import static com.hrth.ustock.exception.domain.portfolio.PortfolioExceptionType.NO_HOLDING;

@Service
@RequiredArgsConstructor
public class NewsService {
    public static final int MAX_NEWS_LIMIT = 15;

    private final NewsRepository newsRepository;
    private final HoldingRepository holdingRepository;

    public List<NewsResponseDto> findHoldingNews(Long userId) {
        List<Holding> holdings = holdingRepository.findAllByUserUserId(userId);

        if (holdings == null || holdings.isEmpty())
            throw new PortfolioException(NO_HOLDING);

        List<News> newsList = new LinkedList<>();

        for (Holding holding : holdings) {
            String stockCode = holding.getStock().getCode();
            newsList.addAll(newsRepository.findTop15ByStockCodeOrderByDateDesc(stockCode));
        }

        Collections.shuffle(newsList);
        int lastIndex = (newsList.size() < MAX_NEWS_LIMIT) ? newsList.size() - 1 : MAX_NEWS_LIMIT;

        return newsList.subList(0, lastIndex).stream()
                .map(News::toResponseDto)
                .sorted(Comparator.comparing(NewsResponseDto::getDate).reversed())
                .toList();
    }
}
