<p align="center"> 
  <img src="images/ustock logo.webp" alt="U'STOCK Logo" width="80px" height="80px">
</p>
<h1 align="center"> U'STOCK </h1>
<h3 align="center"> 뉴스 중심의 차트 분석 서비스 U'STOCK의 backend repository </h3>
<h5 align="center"> 카카오테크부트캠프 클라우드 in JEJU 2팀 HRTH (2024-07-02 ~ 2024-10-11) </h5>

# TODO: 프로젝트 영상 추가
<p align="center"> 
  <img src="images/ustock demo.gif" alt="U'STOCK 시연 영상" height="282px" width="637">
</p>

<!-- 목차 -->
<h2 id="table-of-contents"> :book: 목차</h2>

<details open="open">
  <summary>목차</summary>
  <ol>
    <li><a href="#architecture"> ➤ 아키텍쳐</a></li>
    <li><a href="#stocks"> ➤ /v1/stocks</a></li>
    <li><a href="#portfolio"> ➤ /v1/portfolio</a></li>
    <li><a href="#news"> ➤ /v1/news</a></li>
    <li><a href="#scheduler"> ➤ /v1/scheduler</a></li>
    <li><a href="#game"> ➤ /v1/game</a></li>
    <li><a href="#teammates"> ➤ 팀 소개</a></li>
  </ol>
</details>

![-----------------------------------------------------](https://raw.githubusercontent.com/andreasbm/readme/master/assets/lines/rainbow.png)

<!-- 아키텍쳐 -->
<h2 id="architecture"> :small_orange_diamond: 아키텍쳐 소개</h2>
<p align="justify">
📦5-HRTH-ustock-be
 ┣ 📂src
 ┃ ┣ 📂main
 ┃ ┃ ┣ 📂java
 ┃ ┃ ┃ ┗ 📂com
 ┃ ┃ ┃ ┃ ┗ 📂hrth
 ┃ ┃ ┃ ┃ ┃ ┗ 📂ustock
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📂config
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜AppConfig.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜CorsMvcConfig.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜RedisRepositoryConfig.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜SecurityConfig.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┗ 📜SentryConfiguration.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📂controller
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜CronController.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜HealthController.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜NewsController.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜PortfolioController.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜StockController.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┗ 📜UserController.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📂dto
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📂chart
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜ChartDto.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┗ 📜ChartResponseDto.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📂holding
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜HoldingEmbedDto.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┗ 📜HoldingRequestDto.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📂news
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜NewsEmbedDto.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜NewsRequestDto.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┗ 📜NewsResponseDto.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📂oauth2
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜CustomOAuth2User.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜GoogleResponse.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜OAuth2Response.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜UserOauthDto.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┗ 📜UserResponseDto.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📂portfolio
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜PortfolioEmbedDto.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜PortfolioListDto.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜PortfolioRequestDto.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜PortfolioResponseDto.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┗ 📜PortfolioUpdateDto.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┗ 📂stock
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜MarketResponseDto.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜SkrrrCalculatorRequestDto.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜SkrrrCalculatorResponseDto.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜StockDto.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜StockListDTO.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┗ 📜StockResponseDto.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📂entity
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📂portfolio
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜Chart.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜Holding.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜News.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜Portfolio.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┗ 📜Stock.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┗ 📜User.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📂exception
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜ChartNotFoundException.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜CurrentNotFoundException.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜HoldingNotFoundException.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜InputNotValidException.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜PortfolioNotFoundException.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜StockNotFoundException.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜StockNotPublicException.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┗ 📜UserNotFoundException.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📂jwt
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜CustomLogoutFilter.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜JWTFilter.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┗ 📜JWTUtil.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📂oauth2
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜CustomSuccessHandler.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┗ 📜OAuth2FailureHandler.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📂repository
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜ChartRepository.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜HoldingRepository.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜NewsRepository.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜PortfolioRepository.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜StockRepository.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┗ 📜UserRepository.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📂service
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📂cron
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┗ 📜StockCronService.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜CustomOAuth2UserService.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜NewsService.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜PortfolioService.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜StockService.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┗ 📜StockServiceConst.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📂util
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜DateConverter.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜KisApiAuthManager.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜RedisJsonManager.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜RedisTTLCalculator.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┗ 📜TimeDelay.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┗ 📜UstockApplication.java
 ┃ ┃ ┗ 📂resources
 ┃ ┃ ┃ ┣ 📂static
 ┃ ┃ ┃ ┣ 📂template
 ┃ ┃ ┃ ┗ 📜application.yml
 ┣ 📜.gitignore
 ┣ 📜Dockerfile
 ┣ 📜HELP.md
 ┣ 📜README.md
 ┣ 📜build.gradle
 ┣ 📜gradlew
 ┣ 📜gradlew.bat
 ┗ 📜settings.gradle
</p>
<p align="justify">
  .
  ├── Dockerfile
  ├── HELP.md
  ├── README.md
  ├── build.gradle
  ├── gradle
  │   └── wrapper
  │       ├── gradle-wrapper.jar
  │       └── gradle-wrapper.properties
  ├── gradlew
  ├── gradlew.bat
  ├── images
  │   ├── README.md
  │   └── ustock logo.webp
  ├── settings.gradle
  └── src
      └── main
          └── java
              └── com
                  └── hrth
                      └── ustock
                          ├── UstockApplication.java
                          ├── config
                          │   ├── AppConfig.java
                          │   ├── CorsMvcConfig.java
                          │   ├── RedisRepositoryConfig.java
                          │   ├── SchedulerConfig.java
                          │   ├── SecurityConfig.java
                          │   ├── SentryConfiguration.java
                          │   └── SwaggerConfig.java
                          ├── controller
                          │   ├── api
                          │   │   ├── GameApi.java
                          │   │   ├── NewsApi.java
                          │   │   ├── PortfolioApi.java
                          │   │   ├── StockApi.java
                          │   │   └── UserApi.java
                          │   ├── common
                          │   │   ├── CronController.java
                          │   │   ├── HealthController.java
                          │   │   └── UserController.java
                          │   ├── game
                          │   │   └── GameController.java
                          │   └── main
                          │       ├── NewsController.java
                          │       ├── PortfolioController.java
                          │       └── StockController.java
                          ├── dto
                          │   ├── game
                          │   │   ├── ai
                          │   │   │   ├── GameAiSelectDto.java
                          │   │   │   └── GameAiStockDto.java
                          │   │   ├── hint
                          │   │   │   ├── GameHintRequestDto.java
                          │   │   │   └── GameHintResponseDto.java
                          │   │   ├── interim
                          │   │   │   └── GameInterimResponseDto.java
                          │   │   ├── redis
                          │   │   │   ├── GameHintCheckDto.java
                          │   │   │   ├── GameHoldingsInfoDto.java
                          │   │   │   ├── GameStocksRedisDto.java
                          │   │   │   └── GameUserInfoDto.java
                          │   │   ├── result
                          │   │   │   ├── GameRankingDto.java
                          │   │   │   ├── GameResultChartDto.java
                          │   │   │   ├── GameResultNewsDto.java
                          │   │   │   ├── GameResultResponseDto.java
                          │   │   │   ├── GameResultStockDto.java
                          │   │   │   └── GameYearlyResultDto.java
                          │   │   ├── stock
                          │   │   │   ├── GameStockInfoResponseDto.java
                          │   │   │   └── GameTradeRequestDto.java
                          │   │   └── user
                          │   │       └── GameUserResponseDto.java
                          │   ├── main
                          │   │   ├── chart
                          │   │   │   ├── ChartDto.java
                          │   │   │   └── ChartResponseDto.java
                          │   │   ├── holding
                          │   │   │   ├── HoldingEmbedDto.java
                          │   │   │   └── HoldingRequestDto.java
                          │   │   ├── news
                          │   │   │   ├── NewsEmbedDto.java
                          │   │   │   └── NewsResponseDto.java
                          │   │   ├── portfolio
                          │   │   │   ├── PortfolioEmbedDto.java
                          │   │   │   ├── PortfolioListDto.java
                          │   │   │   ├── PortfolioRequestDto.java
                          │   │   │   ├── PortfolioResponseDto.java
                          │   │   │   └── PortfolioUpdateDto.java
                          │   │   └── stock
                          │   │       ├── AllMarketResponseDto.java
                          │   │       ├── MarketResponseDto.java
                          │   │       ├── SkrrrCalculatorRequestDto.java
                          │   │       ├── SkrrrCalculatorResponseDto.java
                          │   │       └── StockResponseDto.java
                          │   └── oauth2
                          │       ├── CustomOAuth2User.java
                          │       ├── GoogleResponse.java
                          │       ├── OAuth2Response.java
                          │       ├── UserOauthDto.java
                          │       └── UserResponseDto.java
                          ├── entity
                          │   ├── game
                          │   │   ├── GameActing.java
                          │   │   ├── GameHint.java
                          │   │   ├── GameNews.java
                          │   │   ├── GameResult.java
                          │   │   ├── GameStockIndustry.java
                          │   │   ├── GameStockInfo.java
                          │   │   ├── GameStockYearly.java
                          │   │   ├── HintLevel.java
                          │   │   └── PlayerType.java
                          │   └── main
                          │       ├── Chart.java
                          │       ├── Holding.java
                          │       ├── News.java
                          │       ├── Portfolio.java
                          │       ├── Stock.java
                          │       └── User.java
                          ├── exception
                          │   ├── common
                          │   │   ├── CustomException.java
                          │   │   ├── CustomExceptionType.java
                          │   │   ├── ExceptionResponse.java
                          │   │   └── GlobalExceptionHandler.java
                          │   ├── domain
                          │   │   ├── chart
                          │   │   │   ├── ChartException.java
                          │   │   │   └── ChartExceptionType.java
                          │   │   ├── game
                          │   │   │   ├── GameException.java
                          │   │   │   └── GameExceptionType.java
                          │   │   ├── portfolio
                          │   │   │   ├── PortfolioException.java
                          │   │   │   └── PortfolioExceptionType.java
                          │   │   ├── stock
                          │   │   │   ├── StockException.java
                          │   │   │   └── StockExceptionType.java
                          │   │   └── user
                          │   │       ├── UserException.java
                          │   │       └── UserExceptionType.java
                          │   ├── kisapi
                          │   │   ├── KisApiException.java
                          │   │   └── KisApiExceptionType.java
                          │   └── redis
                          │       ├── RedisException.java
                          │       └── RedisExceptionType.java
                          ├── jwt
                          │   ├── CustomLogoutFilter.java
                          │   ├── JWTFilter.java
                          │   └── JWTUtil.java
                          ├── oauth2
                          │   ├── CustomSuccessHandler.java
                          │   └── OAuth2FailureHandler.java
                          ├── repository
                          │   ├── UserRepository.java
                          │   ├── game
                          │   │   ├── GameHintRepository.java
                          │   │   ├── GameNewsRepository.java
                          │   │   ├── GameResultRepository.java
                          │   │   ├── GameStockInfoRepository.java
                          │   │   └── GameStockYearlyRepository.java
                          │   └── main
                          │       ├── ChartBatchRepository.java
                          │       ├── ChartRepository.java
                          │       ├── HoldingRepository.java
                          │       ├── NewsRepository.java
                          │       ├── PortfolioRepository.java
                          │       └── StockRepository.java
                          ├── service
                          │   ├── auth
                          │   │   ├── CustomOAuth2UserService.java
                          │   │   └── CustomUserService.java
                          │   ├── cron
                          │   │   └── StockCronService.java
                          │   ├── game
                          │   │   ├── GameAiService.java
                          │   │   ├── GameInfoConst.java
                          │   │   ├── GamePlayService.java
                          │   │   └── GameRankingService.java
                          │   └── main
                          │       ├── NewsService.java
                          │       ├── PortfolioService.java
                          │       ├── StockService.java
                          │       └── StockServiceConst.java
                          └── util
                              ├── DateConverter.java
                              ├── KisApiAuthManager.java
                              ├── RedisJsonManager.java
                              └── TimeDelay.java
</p>

![-----------------------------------------------------](https://raw.githubusercontent.com/andreasbm/readme/master/assets/lines/rainbow.png)

<!--  -->
<h2 id="architecture"> :small_orange_diamond: 아키텍쳐 소개</h2>

<p align="justify"> 

</p>

![-----------------------------------------------------](https://raw.githubusercontent.com/andreasbm/readme/master/assets/lines/rainbow.png)
