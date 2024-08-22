package com.hrth.ustock.service;

public interface StockServiceConst {
    // 코스피 & 코스닥
    String KOSPI_CODE = "0001";
    String KOSDAQ_CODE = "1001";
    String MARKET_CURRENT_PRICE = "bstp_nmix_prpr";
    String CHANGE_FROM_PREVIOUS_MARKET = "bstp_nmix_prdy_vrss";
    String CHANGE_RATE_FROM_PREVIOUS_MARKET = "bstp_nmix_prdy_ctrt";

    // 종목 순위
    String STOCK_NAME = "hts_kor_isnm";
    String STOCK_CODE = "mksc_shrn_iscd";
    String CHANGE_STOCK_CODE = "stck_shrn_iscd";
    String STOCK_CURRENT_PRICE = "stck_prpr";
    String CHANGE_FROM_PREVIOUS_STOCK = "prdy_vrss";
    String CHANGE_RATE_FROM_PREVIOUS_STOCK = "prdy_ctrt";
}
