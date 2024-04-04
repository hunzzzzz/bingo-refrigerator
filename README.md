![header](https://capsule-render.vercel.app/api?type=waving&height=300&color=87CEEB&text=bingo-jango&section=header&animation=fadeIn)


## 📑 개요

- **개발 기간** : 24.02.26 ~ 24.04.05 
- **프로젝트 이름** : bingo-jango
- **프로젝트 설명 :** '같은 공간'(집, 셰어하우스 등)에 거주하며, '같은 냉장고'를 사용하는 인원 간에 다음과 같은 기능을 제공하는 웹 애플리케이션입니다.

# 🧊 bingo-jango: 우리 집 냉장고를 관리해보자!
## 주요기능


 - 냉장고 내 공유되는 식품들의 종류, 수량, 유통기한 등을 관리


 - 초대 코드 또는 비밀번호의 이용을 통해 '같은 냉장고'를 사용하는 인원들을 초대 가능
   

 - 수량이 부족한 식품에 대한 구매 여부를 냉장고 소속 인원들의 투표를 통해 결정(한 명의 반대라도 있으면 공동 구매가 거부됨)


 - 채팅방으로 냉장고 소속 인원들간 소통 가능


 - 이메일을 아이디로 회원가입하는 형태이며 카카오톡, 구글, 네이버 연동 가능 
## ⏰ Time Table...

![](.github/images/lawn-240313.png)

## 👨‍👩‍👧‍👦 Built With...
|                             허훈                             |                            성진호                             |                            한정민                             |                            박규희                             |                            주형근                             |
|:----------------------------------------------------------:|:----------------------------------------------------------:|:----------------------------------------------------------:|:----------------------------------------------------------:|:----------------------------------------------------------:|
| ![](https://avatars.githubusercontent.com/u/152062846?v=4) | ![](https://avatars.githubusercontent.com/u/151836318?v=4) | ![](https://avatars.githubusercontent.com/u/149580488?v=4) | ![](https://avatars.githubusercontent.com/u/152257506?v=4) | ![](https://avatars.githubusercontent.com/u/152126338?v=4) |
|          [@hunzzzzz](https://github.com/hunzzzzz)          |           [@WiGenie](https://github.com/WiGenie)           |         [@jeongminy](https://github.com/jeongminy)         |          [@qordpsem](https://github.com/qordpsem)          |         [@cresent10](https://github.com/cresent10)         |
|               `리더`, `같이구매, 투표 CRUD`, `서버 배포`               |                   `부리더` `회원 CRUD`, `채팅`                    |            `팀원` `냉장고 CRUD`, `인증 및 인가`, `소셜 로그인`            |               `팀원` `냉장고, 음식 CRUD`, `프로필 사진`                |             `팀원` `회원 CRUD`, `이메일 인증`            |

## ⚒️ 개발 도구 및 환경

### 협업
![Slack](https://img.shields.io/badge/Slack-4A154B?style=for-the-badge&logo=slack&logoColor=white)
![GitHub](https://img.shields.io/badge/github-%23121011.svg?style=for-the-badge&logo=github&logoColor=white)

### Back-End
![IntelliJ IDEA](https://img.shields.io/badge/IntelliJIDEA-000000.svg?style=for-the-badge&logo=intellij-idea&logoColor=white)
![Kotlin](https://img.shields.io/badge/kotlin-%237F52FF.svg?style=for-the-badge&logo=kotlin&logoColor=white)
![SpringBoot](https://img.shields.io/badge/springboot-%236DB33F.svg?style=for-the-badge&logo=spring&logoColor=white)

### 서버 배포
![Redis](https://img.shields.io/badge/redis-%23DD0031.svg?style=for-the-badge&logo=redis&logoColor=white)
![Linux](https://img.shields.io/badge/Linux-FCC624?style=for-the-badge&logo=linux&logoColor=black)
![AWS](https://img.shields.io/badge/AWS-%23FF9900.svg?style=for-the-badge&logo=amazon-aws&logoColor=white)

### ✔️ **의사결정**
<div>
  <details>
    <summary><strong>Redis와 데이터베이스 서버 분리</strong></summary>
    <div markdown="1"> 

    [문제 상황]

    하나의 SpringBoot 백엔드 서버에 Redis와 데이터베이스를 구축하고,

    고가용성 확보 및 시스템 장애에 유연하게 대응하기 위해 Snapshot을 활용하여

    동일 조건, 동일 환경의 백엔드 서버를 총 3개를 구축 (Scale Out) 했지만,

    그 과정에서 **Redis와 데이터베이스도 수평 확장이 되어 데이터 불일치성 문제 발생**

     [결정]

    Stateful한 상태를 유지해야 하는 데이터베이스와 Redis를 각각 독립적인 서버로 구축하고,

    private IP를 활용하여 SpringBoot 서버와 데이터를 주고받도록 수정하였다.
    </div>
  </details>
</div>

<div>
  <details>
    <summary><strong>User 와 Social User 관리</strong></summary>
    <div markdown="1"> 

    [문제 상황]

    일반로그인의 user 와 소셜로그인의 user 의 DB 저장 시, 분리할 것인지 합칠 것 인지에 대한 고민이었다.

    - 합칠 경우 (user 테이블만 생성하자)
        - 장점: user에 대한 통합 관리가 가능하다
        - 단점: 넘어오는 데이터에 따라서 속성 값이 없을 경우 null 이 존재하는 경우가 많아짐.
    - 분리할 경우 (user, social user 테이블 두 개를 만들자)
        - 장점: 구글, 네이버, 카카오에 따라서 유저에 따라 분리된 관리가 가능하다.
        - 단점: user의 통합 관리가 힘들다.

     [결정]

    굳이 user를 따로 관리해야 하나 싶었고, 

    따로 관리 하게 되면 차후 수정사항이 생기면 여러곳을 수정해야 하는 번거로움이 생길 것 같았다.

    → user 테이블에 합치고 통합 관리하도록 정했다.
    </div>
  </details>
</div>

<div>
  <details>
    <summary><strong>서버 Scale-Out 에 따른 메시지 큐 추가</strong></summary>
    <div markdown="1"> 

      [문제 상황]

    기존의 웹소켓 채팅 기능이 서버가 스케일 아웃 됨에 따라 이용자가 다른 인스턴스로 요청할 경우 실시간 전송이 어려워지는 문제

     [결정]

    기존의 웹소켓이 실시간 채팅을 연결하는 Pub/Sub 형식과 마찬가지로 각 서버가 메시지 큐를 구독하고, 한 서버에서 채팅을 발행하면 이를 메시지 큐에서 받아 다시 각 서버에 발행하고 서버에서 받아 처리하여 클라이언트에게 반환하는 방식을 채용.

    RabbitMQ, Kafka, Redis Pub/Sub 중 Redis Pub/Sub을 선정.

    채팅 내역을 db에 저장하는 방식이고, 추후 알림 기능 구현시 자세한 알림 대신 단순 푸시 알림 처리를 고려 중이므로 고성능이나 정확성을 요하는 기타 후보군에 비해 유지보수가 편한 점을 채택. 레디스로 로그인의 토큰이나 캐시 처리에도 사용할 가능성을 고려함.
    </div>
  </details>
</div>


### ✔️ 트러블 슈팅

<div>
  <details>
    <summary><strong>SSH Key 네이밍 규칙 에러</strong></summary>
    <div markdown="1"> 

    [문제 상황]

    백엔드 인스턴스 내에서 SSH Key를 생성하고, Public Key를 GitHub 상에 등록하여

    서버 ↔ GitHub 과의 SSH 연결을 시도하였다. 이 때, **SSH Key의 Public Key의 이름을** 

    **bingo.pub 으로 지정**하였는데, git pull 시 access denied 라는 에러 메시지를 출력하며

    SSH 연결이 제대로 작동되지 않았다. 

    [원인]

    **GitHub에서 지원하는 Public Key 네이밍 규칙에 어긋나는 Public Key 이름을 사용함**

    [해결]

    **SSH key의 이름을 id_rsa.pub으로 변경**

    </div>
  </details>
</div>

<div>
  <details>
    <summary><strong>Spring Security `/logout`</strong></summary>
    <div markdown="1"> 

    [문제 상황]

    로그아웃 기능의 오류가 발생했다.
    
    [원인]
    
    spring security 에도 `/logout` 자체 기능(예약어 처럼) 이 있었고,
    내가 직접 커스텀 해서 구현한 `/logout` 이 있었는데,
    이 두 가지가 중복되어 겹쳐서 오류가 생긴 것으로 확인되었다.

    [해결]

    내가 직접 구현한 커스텀의 매핑을 `/logout`→ `/users/logout` 으로 변경하니 해결됨.

    </div>
  </details>
</div>

<div>
  <details>
    <summary><strong>OAuth2 가져오는 데이터 타입 오류</strong></summary>
    <div markdown="1"> 

    [문제 상황]

    카카오 로그인 시, email 속성을 가져오지 못해서 오류 발생

    [원인]

    구글, 카카오, 네이버 제각각 보내주는 데이터 값이나 모양이 다 다르므로,

    가져올 때도 그에 맞춰서 가져와 줘야한다는 것을 깨닳았다.

    [해결]

    https://developers.kakao.com/tool/rest-api/open/get/v2-user-me 를 참고해서
    카카오가 보내주는 데이터 모양을 확인하고, `KakaoUserAccountResponse` 를 추가해서 그 안에 email 속성을 넣어서 해당 클래스에서 꺼내오는 것으로 해결했다.

    </div>
  </details>
</div>

<div>
  <details>
    <summary><strong>WebSocket 통신의 인증·인가 처리</strong></summary>
    <div markdown="1"> 

    [문제 상황]

    채팅 발송 기능 확인을 위해 프론트에서의 웹소켓 연결 시도 중 인증·인가를 판단하는 AuthenticationFilter에 의해 거부됨.

    [원인]

    기존의 HTTP 요청과 마찬가지로 컨트롤러에 @AuthenticationPrincipal 어노테이션을 통해 이용자를 판단하려 했으나, 웹소켓에서 적용되지 않았기 때문에 거부당한 것.

    [해결]

    ChannelInterceptor를 상속받은 클래스에서 preSend를 override하여 웹소켓 연결 요청시 들어오는 헤더의 값을 추출하여 메소드 내에서 validate 과정을 거침. 이를 저장해두고, 연결 성공 이후발행하는 채팅의 헤더에 담긴 유저의 정보를 userPrincipal로 취급하여 처리.

    </div>
  </details>
</div>

<div>
  <details>
    <summary><strong>Twilio API 기능 에러</strong></summary>
    <div markdown="1"> 

    [문제 상황]

    Twilio 쪽의 기능을 만드는 중 userService쪽에서 에러가 발생함.

    [원인]

    너무 많은 API를 userService에서 처리하려 하여 에러가 많이 발생하였고, 이로 인해 userService에서 새로운 service를 만들어야 하는지, 아니면 완전히 다른 패키지에서 처리해야 하는지에 대한 고민이 발생함.

    [해결]

    새로운 패키지를 만들어 그쪽에서 Twilio의 SMS 서비스 관련 작업을 처리하여 에러를 해결함.

    </div>
  </details>
</div>

<div>
  <details>
    <summary><strong>결과 값 여러 개 도출</strong></summary>
    <div markdown="1"> 

    [문제 상황]

    회원 탈퇴 로직 / STAFF 권한 위임 로직 에서

    IncorrectResultSizeDataAccessException: Query did not return a unique result: 2 results were returned 오류가 발생함.

    [원인]

    냉장고가 여러 대인 사용자는 userId 당 여러 회원을 가지므로, 여러 개의 값을 반환하게 됨.

    [해결]

    오류를 해결하기 위해 findByUserId 대신 findByUserAndRefrigerator를 사용함.

    </div>
  </details>
</div>
