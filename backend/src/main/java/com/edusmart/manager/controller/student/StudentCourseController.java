package com.edusmart.manager.controller.student;

import com.edusmart.manager.common.PageData;
import com.edusmart.manager.common.Result;
import com.edusmart.manager.dto.student.StudentCoursePageQueryDTO;
import com.edusmart.manager.entity.EduClassEntity;
import com.edusmart.manager.service.student.StudentCourseService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/student/courses")
@PreAuthorize("hasRole('STUDENT')")
public class StudentCourseController {
    private final StudentCourseService courseService;
    public StudentCourseController(StudentCourseService courseService) { this.courseService = courseService; }

    @PostMapping("/page")
    public Result<PageData<EduClassEntity>> page(@RequestBody StudentCoursePageQueryDTO queryDTO) {
        return Result.success(courseService.pageCourses(queryDTO));
    }

    @GetMapping("/classes/{id}")
    public Result<EduClassEntity> classDetail(@PathVariable Long id) {
        return Result.success(courseService.getClassDetail(id));
    }
}
