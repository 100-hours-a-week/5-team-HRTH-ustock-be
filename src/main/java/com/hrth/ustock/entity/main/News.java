package com.hrth.ustock.entity.main;

import com.hrth.ustock.dto.main.news.NewsEmbedDto;
import com.hrth.ustock.dto.main.news.NewsResponseDto;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class News {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "news_id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "stock_code")
    private Stock stock;

    @Column(name = "news_date")
    private String date;

    private String title;

    private String url;

    private String publisher;

    public NewsEmbedDto toEmbedDto() {
        return NewsEmbedDto.builder()
                .title(this.title)
                .url(this.url)
                .build();
    }

    public NewsResponseDto toResponseDto() {
        return NewsResponseDto.builder()
                .title(this.title)
                .url(this.url)
                .publisher(this.publisher)
                .date(this.date)
                .code(this.stock.getCode())
                .name(this.stock.getName())
                .build();
    }
}
