package com.example.finalproject;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import org.springframework.test.context.bean.override.mockito.MockitoBean;
import com.example.finalproject.global.config.LocalDataInitializer;

@SpringBootTest
class FinalProjectApplicationTests {

    @MockitoBean
    private LocalDataInitializer localDataInitializer;

    @Test
    void contextLoads() {
    }

}
