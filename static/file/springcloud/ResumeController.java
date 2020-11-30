package cn.chuchin.controller;

import cn.chuchin.pojo.Resume;
import cn.chuchin.service.ResumeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Description
 * @Author ChinHeng-Chu
 * @Date 2020-11-30 13:39
 */
@RestController
@RequestMapping("/resume")
public class ResumeController {

    @Autowired
    private ResumeService resumeService;

    @GetMapping("/openstate/{userId}")
    public Integer findDefaultResumeByUserId(@PathVariable Long userId) {
        Resume resume = resumeService.findDefaultResumeByUserId(userId);
        System.out.println(resume.getIsOpenResume());
        return resume.getIsOpenResume();
    }
}

