package cn.chuchin.dao;

import cn.chuchin.pojo.Resume;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * @Description
 * @Author ChinHeng-Chu
 * @Date 2020-11-30 13:34
 */
public interface ResumeDao extends JpaRepository<Resume, Long> {

}

