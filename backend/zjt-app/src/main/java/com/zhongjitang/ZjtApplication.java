package com.zhongjitang;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = "com.zhongjitang")
@MapperScan("com.zhongjitang.**.mapper")
@EnableScheduling
public class ZjtApplication {

    public static void main(String[] args) {
        SpringApplication.run(ZjtApplication.class, args);
    }
}
