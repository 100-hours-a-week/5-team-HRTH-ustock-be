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
    <li><a href="#architecture"> ➤ 기술 스택</a></li>
    <li><a href="#architecture"> ➤ 디렉토리 구조</a></li>
    <li><a href="#api"> ➤ api 개요</a></li>
    <li><a href="#stocks"> ➤ /v1/stocks</a></li>
    <li><a href="#portfolio"> ➤ /v1/portfolio</a></li>
    <li><a href="#news"> ➤ /v1/news</a></li>
    <li><a href="#scheduler"> ➤ /v1/scheduler</a></li>
    <li><a href="#game"> ➤ /v1/game</a></li>
    <li><a href="#teammates"> ➤ 팀 소개</a></li>
  </ol>
</details>

![-----------------------------------------------------](https://raw.githubusercontent.com/andreasbm/readme/master/assets/lines/rainbow.png)

<!-- 아키텍쳐 -->
<h2 id="architecture"> :small_orange_diamond: 아키텍쳐</h2>

# DevOps 구조도 추가 예정 - tree 위 or 아래에 추가

<!-- 기술 스택 -->
<h2 id="architecture"> :small_orange_diamond: 기술 스택</h2>

| 분류 | 기술 |
| :------: | --- |
| Language | <img src="https://img.shields.io/badge/Java-007396?style=for-the-badge&logo=Java&logoColor=white"> |
|Framework|<img src="https://img.shields.io/badge/Spring-6DB33F?style=for-the-badge&logo=Spring&logoColor=white"> <img src="https://img.shields.io/badge/Spring%20boot-6DB33F?style=for-the-badge&logo=Spring%20boot&logoColor=white"> <img src="https://img.shields.io/badge/Spring%20Security-6DB33F?style=for-the-badge&logo=Spring%20Security&logoColor=white"> <img src="https://img.shields.io/badge/gradle-02303A?style=for-the-badge&logo=gradle&logoColor=white">|
|DB|<img src="https://img.shields.io/badge/MySQL-4479A1?style=for-the-badge&logo=MySQL&logoColor=white"> <img src="https://img.shields.io/badge/AWS%20RDS-527FFF?style=for-the-badge&logo=amazonrds&logoColor=white"> <img src="https://img.shields.io/badge/Hibernate-59666C?style=for-the-badge&logo=Hibernate&logoColor=white"> <img src="https://img.shields.io/badge/JPQL-6DB33F?style=for-the-badge&logo=JPQL&logoColor=white"> <img src="https://img.shields.io/badge/redis-B71C1C?style=for-the-badge&logo=redis&logoColor=white"> <img src="https://img.shields.io/badge/AWS%20ElastiCache-C925D1?style=for-the-badge&logo=amazonelasticache&logoColor=white">|
|Server|<img src="https://img.shields.io/badge/AWS%20EC2-FF9900?style=for-the-badge&logo=amazonec2&logoColor=white"> <img src="https://img.shields.io/badge/nginx-green?style=for-the-badge&logo=nginx&logoColor=white">|
|DevOps|<img src="https://img.shields.io/badge/docker-0066ff?style=for-the-badge&logo=docker&logoColor=white"> <img src="https://img.shields.io/badge/docker%20compose-0099ff?style=for-the-badge&logo=docker_compose&logoColor=white"> <img src="https://img.shields.io/badge/Github%20Actions-2088FF?style=for-the-badge&logo=githubactions&logoColor=white">|

<!-- BE 디렉토리 구조 -->
<h2 id="architecture"> :small_orange_diamond: 디렉토리 구조</h2>

```
  .
  └── src
      └── main
          └── java
              └── com
                  └── hrth
                      └── ustock
                          ├── UstockApplication.java
                          ├── config
                          ├── controller
                          │   ├── api
                          │   ├── common
                          │   ├── game
                          │   └── main
                          ├── dto
                          │   ├── game
                          │   │   ├── ai
                          │   │   ├── hint
                          │   │   ├── interim
                          │   │   ├── redis
                          │   │   ├── result
                          │   │   ├── stock
                          │   │   └── user
                          │   ├── main
                          │   │   ├── chart
                          │   │   ├── holding
                          │   │   ├── news
                          │   │   ├── portfolio
                          │   │   └── stock
                          │   └── oauth2
                          ├── entity
                          │   ├── game
                          │   └── main
                          ├── exception
                          │   ├── common
                          │   ├── domain
                          │   │   ├── chart
                          │   │   ├── game
                          │   │   ├── portfolio
                          │   │   ├── stock
                          │   │   └── user
                          │   ├── kisapi
                          │   └── redis
                          ├── jwt
                          ├── oauth2
                          ├── repository
                          │   ├── game
                          │   └── main
                          ├── service
                          │   ├── auth
                          │   ├── cron
                          │   ├── game
                          │   └── main
                          └── util
```

![-----------------------------------------------------](https://raw.githubusercontent.com/andreasbm/readme/master/assets/lines/rainbow.png)

<!--  -->
<h2 id="api"> :small_orange_diamond: api 개요</h2>

<p align="justify">
main
/stocks
/portfolio <img src="images/springsecurity.png" height="40px" width="40px"/>
/news

game
/game

</p>

![-----------------------------------------------------](https://raw.githubusercontent.com/andreasbm/readme/master/assets/lines/rainbow.png)
