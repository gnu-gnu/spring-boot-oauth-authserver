# Spring boot OAuth Authorization server example

1. authorization code, refresh token, password grant에 대해서 token 발급 기능이 구현되어 있습니다.
1. request token 요청 주소는 http://localhost:9099/apps/oauth/authorize 이며  access token 요청 주소는 http://localhost:9099/apps/oauth/token 입니다.
1. redirect_uri는 AuthClientDetailsService.java 를 참조하여 수정 가능합니다. POSTMAN을 통한 OAUTH 테스트를 권장합니다.
1. 각 사용자별로 추가 정보가 다르게 설정되어 있습니다. 엔드포인트의 인증을 위해 사용할 username과 password는 gnu/pass, noh/pass, jee/pass 입니다.
1. 전체 flow에 대한 단위테스트는 /test/java/AuthServerApplicationTests.java 에 구현되어 있습니다.
1. 기본적으로 in-memory store를 사용하도록 되어 있으나, VM args에 -Dspring.profiles.active=redis 를 넣어서 redis store를 사용할 수 있습니다. 접속 정보는 application.properties에 작성되어 있습니다.