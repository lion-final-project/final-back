package com.example.finalproject.global.config;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import lombok.extern.slf4j.Slf4j;

/**
 * Spring Boot는 .env 파일을 자동으로 읽지 않습니다.
 * main() 실행 전에 .env를 읽어 시스템 프로퍼티로 넣어 두면
 * application-local.yml의 ${COOLSMS_API_KEY} 등이 정상 치환됩니다.
 */
@Slf4j
public final class EnvLoader {

    private static final String ENV_FILE = ".env";

    /**
     * 프로젝트 루트(또는 user.dir)의 .env를 읽어 시스템 프로퍼티로 설정.
     * 이미 환경변수/시스템 프로퍼티가 있으면 덮어쓰지 않음.
     */
    public static void loadDotEnv() {
        Path path = Paths.get(System.getProperty("user.dir"), ENV_FILE);
        if (!Files.isRegularFile(path)) {
            log.debug(".env not found at {}", path);
            return;
        }
        try {
            List<String> lines = Files.readAllLines(path);
            for (String line : lines) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }
                int eq = line.indexOf('=');
                if (eq <= 0) {
                    continue;
                }
                String key = line.substring(0, eq).trim();
                String value = line.substring(eq + 1).trim();
                if (value.startsWith("\"") && value.endsWith("\"")) {
                    value = value.substring(1, value.length() - 1);
                }
                if (System.getProperty(key) == null && System.getenv(key) == null) {
                    System.setProperty(key, value);
                    log.trace("Set system property from .env: {}", key);
                }
            }
        } catch (Exception e) {
            log.warn("Failed to load .env: {}", e.getMessage());
        }
    }
}
