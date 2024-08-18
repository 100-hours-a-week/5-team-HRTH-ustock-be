package com.hrth.ustock.service;

import com.hrth.ustock.dto.stock.StockDTO;
import com.hrth.ustock.entity.portfolio.Stock;
import com.hrth.ustock.exception.StockNotFoundException;
import com.hrth.ustock.repository.StockRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StockService {
    private final StockRepository stockRepository;

    // API 6 - 종목 검색
    public List<StockDTO> findByStockName(String str) {
        List<Stock> list = stockRepository.findAllByNameContaining(str);
        // TODO: 현재가, 등락율 추가
        if (list == null || list.isEmpty()) {
            throw new StockNotFoundException();
        }
        List<StockDTO> stockList = new ArrayList<>();
        for (Stock stock : list) {
            stockList.add(stock.toDTO());
        }
        return stockList;
    }
}
