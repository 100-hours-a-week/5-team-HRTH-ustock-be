package com.hrth.ustock.repository.game;

import com.hrth.ustock.entity.game.GameStockYearly;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface GameStockYearlyRepository extends JpaRepository<GameStockYearly, Long> {
    Optional<GameStockYearly> findByGameStockInfoIdAndYear(long stockId, int year);

    List<GameStockYearly> findAllByGameStockInfoId(long stockId);

    @Query(value = "select y from GameStockYearly y " +
            "join fetch y.gameNews n join fetch y.gameStockInfo i " +
            "where i.id in (:stockIdList)")
    List<GameStockYearly> findAllByGameStockInfoIdIn(List<Long> stockIdList);

    Optional<GameStockYearly> findByGameStockInfoId(Long id);

    @Query(value = "select y.price from GameStockYearly y " +
            "left join GameStockInfo i on y.gameStockInfo.id = i.id " +
            "where y.gameStockInfo.id = :id and y.year = :year")
    Optional<Integer> findPriceByGameStockInfoIdAndYear(Long id, int year);

    @Query(value = "select y from GameStockYearly y " +
            "join fetch y.gameStockInfo i " +
            "where i.id in (:stockIds) and y.year = :year " +
            "order by i.id")
    List<GameStockYearly> findPriceListByGameStockInfoIdAndYear(List<Long> stockIds, int year);
}
