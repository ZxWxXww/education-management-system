package com.edusmart.manager.security;

import com.edusmart.manager.controller.admin.AdminAttendanceExceptionController;
import com.edusmart.manager.controller.admin.AdminClassController;
import com.edusmart.manager.controller.admin.AdminCourseController;
import com.edusmart.manager.controller.admin.AdminLogController;
import com.edusmart.manager.controller.admin.AdminUserController;
import com.edusmart.manager.controller.teacher.TeacherAttendanceExceptionController;
import com.edusmart.manager.controller.teacher.TeacherClassController;
import com.edusmart.manager.controller.teacher.TeacherResourceController;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.prepost.PreAuthorize;

import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class RbacSemanticsContractTest {

    @Test
    void controllerWriteEndpointsMustUseManagePermissions() throws NoSuchMethodException {
        assertMethodPreAuthorizeContains(
                TeacherClassController.class,
                "create",
                new Class[]{com.edusmart.manager.dto.teacher.TeacherClassSaveDTO.class},
                "teacher:class:manage"
        );
        assertMethodPreAuthorizeContains(
                TeacherClassController.class,
                "update",
                new Class[]{Long.class, com.edusmart.manager.dto.teacher.TeacherClassSaveDTO.class},
                "teacher:class:manage"
        );
        assertMethodPreAuthorizeContains(
                TeacherClassController.class,
                "delete",
                new Class[]{Long.class},
                "teacher:class:manage"
        );

        assertMethodPreAuthorizeContains(
                TeacherResourceController.class,
                "create",
                new Class[]{com.edusmart.manager.dto.teacher.TeacherResourceSaveDTO.class},
                "resource:manage"
        );
        assertMethodPreAuthorizeContains(
                TeacherResourceController.class,
                "update",
                new Class[]{Long.class, com.edusmart.manager.dto.teacher.TeacherResourceSaveDTO.class},
                "resource:manage"
        );
        assertMethodPreAuthorizeContains(
                TeacherResourceController.class,
                "delete",
                new Class[]{Long.class},
                "resource:manage"
        );

        assertMethodPreAuthorizeContains(
                TeacherAttendanceExceptionController.class,
                "create",
                new Class[]{com.edusmart.manager.dto.teacher.TeacherAttendanceExceptionSaveDTO.class},
                "attendance:abnormal:manage"
        );
        assertMethodPreAuthorizeContains(
                TeacherAttendanceExceptionController.class,
                "update",
                new Class[]{Long.class, com.edusmart.manager.dto.teacher.TeacherAttendanceExceptionSaveDTO.class},
                "attendance:abnormal:manage"
        );
        assertMethodPreAuthorizeContains(
                TeacherAttendanceExceptionController.class,
                "delete",
                new Class[]{Long.class},
                "attendance:abnormal:manage"
        );

        assertMethodPreAuthorizeContains(
                AdminUserController.class,
                "create",
                new Class[]{com.edusmart.manager.dto.admin.AdminUserSaveDTO.class},
                "user:manage"
        );
        assertMethodPreAuthorizeContains(
                AdminUserController.class,
                "update",
                new Class[]{Long.class, com.edusmart.manager.dto.admin.AdminUserSaveDTO.class},
                "user:manage"
        );
        assertMethodPreAuthorizeContains(
                AdminUserController.class,
                "delete",
                new Class[]{Long.class},
                "user:manage"
        );

        assertMethodPreAuthorizeContains(
                AdminCourseController.class,
                "create",
                new Class[]{com.edusmart.manager.dto.admin.CourseSaveDTO.class},
                "course:manage"
        );
        assertMethodPreAuthorizeContains(
                AdminCourseController.class,
                "update",
                new Class[]{Long.class, com.edusmart.manager.dto.admin.CourseSaveDTO.class},
                "course:manage"
        );
        assertMethodPreAuthorizeContains(
                AdminCourseController.class,
                "delete",
                new Class[]{Long.class},
                "course:manage"
        );

        assertMethodPreAuthorizeContains(
                AdminClassController.class,
                "create",
                new Class[]{com.edusmart.manager.dto.admin.ClassSaveDTO.class},
                "class:manage"
        );
        assertMethodPreAuthorizeContains(
                AdminClassController.class,
                "update",
                new Class[]{Long.class, com.edusmart.manager.dto.admin.ClassSaveDTO.class},
                "class:manage"
        );
        assertMethodPreAuthorizeContains(
                AdminClassController.class,
                "delete",
                new Class[]{Long.class},
                "class:manage"
        );

        assertMethodPreAuthorizeContains(
                AdminAttendanceExceptionController.class,
                "create",
                new Class[]{com.edusmart.manager.dto.admin.AttendanceExceptionSaveDTO.class},
                "attendance:abnormal:manage"
        );
        assertMethodPreAuthorizeContains(
                AdminAttendanceExceptionController.class,
                "update",
                new Class[]{Long.class, com.edusmart.manager.dto.admin.AttendanceExceptionSaveDTO.class},
                "attendance:abnormal:manage"
        );
        assertMethodPreAuthorizeContains(
                AdminAttendanceExceptionController.class,
                "delete",
                new Class[]{Long.class},
                "attendance:abnormal:manage"
        );

        assertMethodPreAuthorizeContains(
                AdminLogController.class,
                "saveArchiveStrategy",
                new Class[]{com.edusmart.manager.dto.admin.AdminLogArchiveStrategyDTO.class},
                "setting:log:manage"
        );
    }

    @Test
    void frontendManagementRoutesAndMenusMustUseManagePermissions() throws IOException {
        String adminRoutes = readRepoFile("frontend/src/router/routes/admin.js");
        String teacherRoutes = readRepoFile("frontend/src/router/routes/teacher.js");
        String menus = readRepoFile("frontend/src/menus/index.js");

        assertThat(adminRoutes).contains("permission: 'user:manage'");
        assertThat(adminRoutes).contains("permission: 'course:manage'");
        assertThat(adminRoutes).contains("permission: 'class:manage'");
        assertThat(adminRoutes).contains("permission: 'attendance:abnormal:manage'");
        assertThat(adminRoutes).contains("permission: 'setting:log:manage'");
        assertThat(teacherRoutes).contains("permission: 'teacher:class:manage'");

        assertThat(menus).contains("permission: 'user:manage'");
        assertThat(menus).contains("permission: 'course:manage'");
        assertThat(menus).contains("permission: 'class:manage'");
        assertThat(menus).contains("permission: 'attendance:abnormal:manage'");
        assertThat(menus).contains("permission: 'setting:log:manage'");
        assertThat(menus).contains("permission: 'teacher:class:manage'");
    }

    @Test
    void seedAndSecurityConfigMustKnowNewManagePermissions() throws IOException {
        String seed = readRepoFile("seed-rbac.sql");
        String securityConfig = readRepoFile("backend/src/main/java/com/edusmart/manager/security/SecurityConfig.java");

        for (String permission : new String[]{
                "user:manage",
                "course:manage",
                "class:manage",
                "attendance:abnormal:manage",
                "setting:log:manage",
                "teacher:class:manage",
                "resource:manage"
        }) {
            assertThat(seed).contains("'" + permission + "'");
            assertThat(securityConfig).contains(permission);
        }
    }

    private void assertMethodPreAuthorizeContains(Class<?> type,
                                                  String methodName,
                                                  Class<?>[] parameterTypes,
                                                  String expectedPermission) throws NoSuchMethodException {
        Method method = type.getMethod(methodName, parameterTypes);
        PreAuthorize annotation = method.getAnnotation(PreAuthorize.class);
        assertThat(annotation)
                .as("%s#%s should declare @PreAuthorize", type.getSimpleName(), methodName)
                .isNotNull();
        assertThat(annotation.value()).contains(expectedPermission);
    }

    private String readRepoFile(String relativePath) throws IOException {
        Path repoRoot = Path.of("").toAbsolutePath().getParent();
        return Files.readString(repoRoot.resolve(relativePath));
    }
}
