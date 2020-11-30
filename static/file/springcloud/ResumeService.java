package cn.chuchin.service;

import cn.chuchin.pojo.Resume;

/**
 * @Description
 * @Author ChinHeng-Chu
 * @Date 2020-11-30 13:35
 */
public interface ResumeService {

    Resume findDefaultResumeByUserId(Long userId);

}

