package com.example.finalproject.global.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.annotation.Profile;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.Statement;

/**
 * 로컬 프로파일에서 DataSource 첫 연결 시 PostGIS 확장을 생성합니다.
 * Hibernate가 stores 등 GEOGRAPHY 컬럼이 있는 테이블을 만들기 전에 확장이 있어야 합니다.
 */
@Slf4j
@Component
@Profile("local")
public class PostgisDataSourceInitializer implements BeanPostProcessor, Ordered {

    @Override
    public Object postProcessAfterInitialization(@NonNull Object bean, @NonNull String beanName) throws BeansException {
        if (!(bean instanceof DataSource) || bean instanceof PostgisEnsuringDataSource) {
            return bean;
        }
        return new PostgisEnsuringDataSource((DataSource) bean);
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }

    private static final class PostgisEnsuringDataSource extends org.springframework.jdbc.datasource.AbstractDataSource {
        private final DataSource delegate;
        private volatile boolean initialized;

        PostgisEnsuringDataSource(DataSource delegate) {
            this.delegate = delegate;
        }

        @Override
        public Connection getConnection() throws java.sql.SQLException {
            ensurePostgis();
            return delegate.getConnection();
        }

        @Override
        public Connection getConnection(String username, String password) throws java.sql.SQLException {
            ensurePostgis();
            return delegate.getConnection(username, password);
        }

        private void ensurePostgis() {
            if (initialized) {
                return;
            }
            synchronized (this) {
                if (initialized) {
                    return;
                }
                try (Connection conn = delegate.getConnection();
                     Statement st = conn.createStatement()) {
                    st.execute("CREATE EXTENSION IF NOT EXISTS postgis");
                    log.info("PostGIS extension ensured.");
                } catch (Exception e) {
                    log.warn("Could not create PostGIS extension (tables with GEOGRAPHY may fail): {}", e.getMessage());
                }
                initialized = true;
            }
        }
    }
}
