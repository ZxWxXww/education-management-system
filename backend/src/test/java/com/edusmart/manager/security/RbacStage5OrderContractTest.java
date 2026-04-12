package com.edusmart.manager.security;

import com.edusmart.manager.controller.admin.AdminOrderController;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.prepost.PreAuthorize;

import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class RbacStage5OrderContractTest {

    @Test
    void adminOrderWriteAndReadEndpointsMustUseOrderManagePermission() throws NoSuchMethodException {
        assertMethodPreAuthorizeContains(
                AdminOrderController.class,
                "page",
                new Class[]{com.edusmart.manager.dto.admin.AdminOrderPageQueryDTO.class},
                "finance:order:manage"
        );
        assertMethodPreAuthorizeContains(
                AdminOrderController.class,
                "detail",
                new Class[]{Long.class},
                "finance:order:manage"
        );
        assertMethodPreAuthorizeContains(
                AdminOrderController.class,
                "create",
                new Class[]{com.edusmart.manager.dto.admin.AdminOrderSaveDTO.class},
                "finance:order:manage"
        );
    }

    @Test
    void seedSecurityRoutesAndMenusMustContainOrderManagePermission() throws IOException {
        String seed = readRepoFile("seed-rbac.sql");
        String securityConfig = readRepoFile("backend/src/main/java/com/edusmart/manager/security/SecurityConfig.java");
        String adminRoutes = readRepoFile("frontend/src/router/routes/admin.js");
        String menus = readRepoFile("frontend/src/menus/index.js");

        assertThat(seed).contains("'finance:order:manage'");
        assertThat(securityConfig).contains("finance:order:manage");
        assertThat(adminRoutes).contains("permission: 'finance:order:manage'");
        assertThat(menus).contains("permission: 'finance:order:manage'");
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
