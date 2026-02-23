# Java 21 기반의 가벼운 이미지 사용
FROM openjdk:21-slim

# 작업 디렉토리 설정
WORKDIR /app

# 빌드 결과물 중 실행 가능한 jar 파일만 복사
# build/libs/ 내의 plain이 붙지 않은 jar를 타겟으로 합니다.
ARG JAR_FILE=build/libs/*[!plain].jar
COPY ${JAR_FILE} app.jar

# 한국 시간대 설정
ENV TZ=Asia/Seoul
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

# 배포 프로필(prod)을 적용하여 실행
ENTRYPOINT ["java", "-Dspring.profiles.active=prod", "-jar", "app.jar"]