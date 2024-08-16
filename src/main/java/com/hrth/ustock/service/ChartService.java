package com.hrth.ustock.service;

import com.hrth.ustock.repository.ChartRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ChartService {
    private final ChartRepository chartRepository;

}
