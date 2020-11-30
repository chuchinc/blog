package cn.chuchin;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;

/**
 * @Description
 * @Author ChinHeng-Chu
 * @Date 2020-11-30 13:42
 */
@SpringBootApplication
@EntityScan("cn.chuchin.pojo")
public class ResumeApplication8080 {

    public static void main(String[] args) {
        SpringApplication.run(ResumeApplication8080.class,args);
    }
}

