package com.hrth.ustock.controller.api;

import com.hrth.ustock.dto.main.chart.ChartResponseDto;
import com.hrth.ustock.dto.main.stock.AllMarketResponseDto;
import com.hrth.ustock.dto.main.stock.SkrrrCalculatorRequestDto;
import com.hrth.ustock.dto.main.stock.SkrrrCalculatorResponseDto;
import com.hrth.ustock.dto.main.stock.StockResponseDto;
import com.hrth.ustock.exception.common.ExceptionResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Tag(name = "Stock", description = "종목 관련 API")
public interface StockApi {

    @Operation(
            summary = "종목 순위 리스트",
            description = "시가총액, 거래량, 등락율 순 종목 리스트 반환"
    )
    @Parameter(
            name = "order",
            description = "top, trade: 거래량 / capital: 시가총액 / change: 등락율"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200"),
            @ApiResponse(
                    responseCode = "400",
                    description = "순위 요청이 올바르지 않습니다.",
                    content = @Content(schema = @Schema(implementation = ExceptionResponse.class))),
            @ApiResponse(
                    responseCode = "500",
                    description = "JSON->Object 역직렬화에 실패했습니다",
                    content = @Content(schema = @Schema(implementation = ExceptionResponse.class))),
            @ApiResponse(
                    responseCode = "500",
                    description = "서버에 오류가 발생하였습니다.",
                    content = @Content(schema = @Schema(implementation = ExceptionResponse.class))),
            @ApiResponse(
                    responseCode = "503",
                    description = "API 요청에 실패했습니다",
                    content = @Content(schema = @Schema(implementation = ExceptionResponse.class)))
    })
    ResponseEntity<List<StockResponseDto>> stockList(
            @RequestParam String order
    );

    @Operation(
            summary = "코스피, 코스닥 지수",
            description = "현재 코스피, 코스닥 지수 반환"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200"),
            @ApiResponse(
                    responseCode = "404",
                    description = "시장 정보를 조회하는데 실패하였습니다.",
                    content = @Content(schema = @Schema(implementation = ExceptionResponse.class))),
            @ApiResponse(
                    responseCode = "500",
                    description = "Object->JSON 직렬화에 실패했습니다.",
                    content = @Content(schema = @Schema(implementation = ExceptionResponse.class))),
            @ApiResponse(
                    responseCode = "500",
                    description = "JSON->Object 역직렬화에 실패했습니다.",
                    content = @Content(schema = @Schema(implementation = ExceptionResponse.class))),
            @ApiResponse(
                    responseCode = "500",
                    description = "서버에 오류가 발생하였습니다.",
                    content = @Content(schema = @Schema(implementation = ExceptionResponse.class))),
            @ApiResponse(
                    responseCode = "503",
                    description = "API 요청에 실패했습니다",
                    content = @Content(schema = @Schema(implementation = ExceptionResponse.class)))
    })
    ResponseEntity<AllMarketResponseDto> marketInformation();

    @Operation(
            summary = "종목 검색",
            description = "종목 검색결과 반환"
    )
    @Parameter(
            name = "query",
            description = "종목명 입력 or 종목 코드 입력"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200"),
            @ApiResponse(
                    responseCode = "404",
                    description = "해당 종목을 찾을 수 없습니다.",
                    content = @Content(schema = @Schema(implementation = ExceptionResponse.class))),
            @ApiResponse(
                    responseCode = "500",
                    description = "서버에 오류가 발생하였습니다.",
                    content = @Content(schema = @Schema(implementation = ExceptionResponse.class)))
    })
    ResponseEntity<List<StockResponseDto>> searchStock(
            @RequestParam String query
    );

    @Operation(
            summary = "종목 조회",
            description = "종목코드 기반 특정 종목 조회결과 반환"
    )
    @Parameter(
            name = "code",
            description = "종목 코드 입력"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200"),
            @ApiResponse(
                    responseCode = "404",
                    description = "해당 종목을 찾을 수 없습니다.",
                    content = @Content(schema = @Schema(implementation = ExceptionResponse.class))),
            @ApiResponse(
                    responseCode = "404",
                    description = "현재가 정보를 조회하는데 실패하였습니다.",
                    content = @Content(schema = @Schema(implementation = ExceptionResponse.class))),
            @ApiResponse(
                    responseCode = "500",
                    description = "서버에 오류가 발생하였습니다.",
                    content = @Content(schema = @Schema(implementation = ExceptionResponse.class)))
    })
    ResponseEntity<StockResponseDto> findStockByCode(
            @PathVariable String code
    );

    @Operation(
            summary = "차트 조회",
            description = "특정 종목 차트결과 반환(일, 주, 월봉 확인가능)"
    )
    @Parameters({
            @Parameter(
                    name = "code",
                    description = "종목 코드",
                    required = true),
            @Parameter(
                    name = "period",
                    description = "차트 유형 1: 일봉 2: 주봉 3: 월봉",
                    required = true)
    })
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200"),
            @ApiResponse(
                    responseCode = "400",
                    description = "올바르지 않은 조회 기간입니다.",
                    content = @Content(schema = @Schema(implementation = ExceptionResponse.class))),
            @ApiResponse(
                    responseCode = "404",
                    description = "차트 정보를 찾지 못하였습니다.",
                    content = @Content(schema = @Schema(implementation = ExceptionResponse.class))),
            @ApiResponse(
                    responseCode = "404",
                    description = "해당 종목을 찾을 수 없습니다.",
                    content = @Content(schema = @Schema(implementation = ExceptionResponse.class))),
            @ApiResponse(
                    responseCode = "500",
                    description = "JSON->Object 역직렬화에 실패했습니다",
                    content = @Content(schema = @Schema(implementation = ExceptionResponse.class))),
            @ApiResponse(
                    responseCode = "500",
                    description = "서버에 오류가 발생하였습니다.",
                    content = @Content(schema = @Schema(implementation = ExceptionResponse.class)))
    })
    ResponseEntity<List<ChartResponseDto>> getStockChart(
            @PathVariable String code,
            @RequestParam int period
    );

    @Operation(
            summary = "스껄계산기",
            description = "특정 종목의 특정 날짜 현재가 기준 계산 결과 반환"
    )
    @Parameters({
            @Parameter(
                    name = "code",
                    description = "종목 코드",
                    required = true
            ),
            @Parameter(
                    name = "price",
                    description = "requestDto의 요소, 1~9,999,999,999,999원",
                    schema =
                    @Schema(
                            minimum = "1",
                            maximum = "9999999999999"
                    )
            ),
            @Parameter(
                    name = "date",
                    description = "requestDto의 요소, \"2014/01/01\"~현재 날짜",
                    schema =
                    @Schema(
                            description = "과거 날짜",
                            minimum = "2014/01/01",
                            maximum = "$Current date"
                    )
            )
    })
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200"),
            @ApiResponse(
                    responseCode = "400",
                    description = "잘못된 날짜 양식입니다.",
                    content = @Content(schema = @Schema(implementation = ExceptionResponse.class))),
            @ApiResponse(
                    responseCode = "400",
                    description = "입력값이 허용 범위를 초과하였습니다.",
                    content = @Content(schema = @Schema(implementation = ExceptionResponse.class))),
            @ApiResponse(
                    responseCode = "400",
                    description = "해당 종목이 상장되지 않은 날짜입니다.",
                    content = @Content(schema = @Schema(implementation = ExceptionResponse.class))),
            @ApiResponse(
                    responseCode = "400",
                    description = "해당 금액으로는 구매할 수 없습니다.",
                    content = @Content(schema = @Schema(implementation = ExceptionResponse.class))),
            @ApiResponse(
                    responseCode = "404",
                    description = "현재가 정보를 조회하는데 실패하였습니다.",
                    content = @Content(schema = @Schema(implementation = ExceptionResponse.class))),
            @ApiResponse(
                    responseCode = "500",
                    description = "서버에 오류가 발생하였습니다.",
                    content = @Content(schema = @Schema(implementation = ExceptionResponse.class))),
            @ApiResponse(
                    responseCode = "503",
                    description = "API 요청에 실패했습니다",
                    content = @Content(schema = @Schema(implementation = ExceptionResponse.class)))
    })
    ResponseEntity<SkrrrCalculatorResponseDto> skrrrCalculator(
            @PathVariable String code,
            @RequestBody SkrrrCalculatorRequestDto requestDto
    );
}
