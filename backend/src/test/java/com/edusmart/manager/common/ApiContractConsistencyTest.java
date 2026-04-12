package com.edusmart.manager.common;

import com.edusmart.manager.controller.HealthController;
import com.edusmart.manager.controller.admin.AdminBillController;
import com.edusmart.manager.controller.admin.AdminClassController;
import com.edusmart.manager.controller.admin.AdminCourseController;
import com.edusmart.manager.controller.admin.AdminOrderController;
import com.edusmart.manager.controller.admin.AdminUserController;
import com.edusmart.manager.controller.student.StudentAttendanceController;
import com.edusmart.manager.controller.student.StudentBillController;
import com.edusmart.manager.controller.student.StudentProfileController;
import com.edusmart.manager.service.admin.AdminClassService;
import com.edusmart.manager.service.admin.AdminCourseService;
import com.edusmart.manager.service.admin.AdminOrderService;
import com.edusmart.manager.service.admin.AdminUserService;
import com.edusmart.manager.service.student.StudentAttendanceService;
import com.edusmart.manager.service.student.StudentProfileService;
import org.junit.jupiter.api.Test;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.server.ResponseStatusException;

import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.assertThat;

class ApiContractConsistencyTest {

    @Test
    void contractWrappersShouldUseResultInsteadOfApiResponse() throws NoSuchMethodException {
        assertRawReturnType(HealthController.class, "health", new Class<?>[0], Result.class);
        assertRawReturnType(
                StudentBillController.class,
                "page",
                new Class<?>[]{com.edusmart.manager.dto.student.StudentBillPageQueryDTO.class},
                Result.class
        );
        assertRawReturnType(StudentBillController.class, "detail", new Class<?>[]{Long.class}, Result.class);
        assertRawReturnType(StudentBillController.class, "hourPackageSummary", new Class<?>[0], Result.class);
        assertRawReturnType(
                AdminBillController.class,
                "registerPayment",
                new Class<?>[]{Long.class, com.edusmart.manager.dto.admin.AdminBillPaymentSaveDTO.class},
                Result.class
        );
        assertRawReturnType(
                AdminBillController.class,
                "registerRefund",
                new Class<?>[]{Long.class, com.edusmart.manager.dto.admin.AdminBillRefundSaveDTO.class},
                Result.class
        );
        assertRawReturnType(
                AdminBillController.class,
                "paymentList",
                new Class<?>[]{Long.class},
                Result.class
        );

        assertExceptionHandlerReturnType(
                GlobalExceptionHandler.class,
                "handleStatus",
                new Class<?>[]{ResponseStatusException.class}
        );
        assertExceptionHandlerReturnType(
                GlobalExceptionHandler.class,
                "handleValidation",
                new Class<?>[]{MethodArgumentNotValidException.class}
        );
        assertExceptionHandlerReturnType(
                GlobalExceptionHandler.class,
                "handleOther",
                new Class<?>[]{Exception.class}
        );
    }

    @Test
    void controllerReadContractsShouldNotExposeEntityTypes() throws NoSuchMethodException {
        assertGenericReturnTypeDoesNotContain(
                AdminCourseController.class,
                "page",
                new Class<?>[]{com.edusmart.manager.dto.admin.CoursePageQueryDTO.class},
                "EduCourseEntity"
        );
        assertGenericReturnTypeDoesNotContain(
                AdminCourseController.class,
                "detail",
                new Class<?>[]{Long.class},
                "EduCourseEntity"
        );
        assertGenericReturnTypeDoesNotContain(
                AdminClassController.class,
                "page",
                new Class<?>[]{com.edusmart.manager.dto.admin.ClassPageQueryDTO.class},
                "EduClassEntity"
        );
        assertGenericReturnTypeDoesNotContain(
                AdminClassController.class,
                "detail",
                new Class<?>[]{Long.class},
                "EduClassEntity"
        );
        assertGenericReturnTypeDoesNotContain(
                AdminUserController.class,
                "detail",
                new Class<?>[]{Long.class},
                "EduUserEntity"
        );
        assertGenericReturnTypeDoesNotContain(
                AdminOrderController.class,
                "page",
                new Class<?>[]{com.edusmart.manager.dto.admin.AdminOrderPageQueryDTO.class},
                "EduOrderEntity"
        );
        assertGenericReturnTypeDoesNotContain(
                AdminOrderController.class,
                "detail",
                new Class<?>[]{Long.class},
                "EduOrderEntity"
        );
        assertGenericReturnTypeDoesNotContain(
                AdminBillController.class,
                "paymentList",
                new Class<?>[]{Long.class},
                "EduBillPaymentEntity"
        );
        assertGenericReturnTypeDoesNotContain(
                StudentAttendanceController.class,
                "page",
                new Class<?>[]{com.edusmart.manager.dto.student.StudentAttendancePageQueryDTO.class},
                "EduAttendanceRecordEntity"
        );
        assertGenericReturnTypeDoesNotContain(
                StudentAttendanceController.class,
                "detail",
                new Class<?>[]{Long.class},
                "EduAttendanceRecordEntity"
        );
        assertGenericReturnTypeDoesNotContain(
                StudentBillController.class,
                "hourPackageSummary",
                new Class<?>[0],
                "EduStudentHourPackageEntity"
        );
        assertGenericReturnTypeDoesNotContain(
                StudentProfileController.class,
                "detail",
                new Class<?>[]{Long.class},
                "EduStudentProfileEntity"
        );
    }

    @Test
    void serviceReadContractsShouldNotExposeEntityTypes() throws NoSuchMethodException {
        assertGenericReturnTypeDoesNotContain(
                AdminCourseService.class,
                "page",
                new Class<?>[]{com.edusmart.manager.dto.admin.CoursePageQueryDTO.class},
                "EduCourseEntity"
        );
        assertGenericReturnTypeDoesNotContain(
                AdminCourseService.class,
                "getById",
                new Class<?>[]{Long.class},
                "EduCourseEntity"
        );
        assertGenericReturnTypeDoesNotContain(
                AdminClassService.class,
                "page",
                new Class<?>[]{com.edusmart.manager.dto.admin.ClassPageQueryDTO.class},
                "EduClassEntity"
        );
        assertGenericReturnTypeDoesNotContain(
                AdminClassService.class,
                "getById",
                new Class<?>[]{Long.class},
                "EduClassEntity"
        );
        assertGenericReturnTypeDoesNotContain(
                AdminUserService.class,
                "getById",
                new Class<?>[]{Long.class},
                "EduUserEntity"
        );
        assertGenericReturnTypeDoesNotContain(
                AdminOrderService.class,
                "page",
                new Class<?>[]{com.edusmart.manager.dto.admin.AdminOrderPageQueryDTO.class},
                "EduOrderEntity"
        );
        assertGenericReturnTypeDoesNotContain(
                AdminOrderService.class,
                "getById",
                new Class<?>[]{Long.class},
                "EduOrderEntity"
        );
        assertGenericReturnTypeDoesNotContain(
                StudentAttendanceService.class,
                "pageAttendance",
                new Class<?>[]{com.edusmart.manager.dto.student.StudentAttendancePageQueryDTO.class},
                "EduAttendanceRecordEntity"
        );
        assertGenericReturnTypeDoesNotContain(
                StudentAttendanceService.class,
                "getAttendance",
                new Class<?>[]{Long.class},
                "EduAttendanceRecordEntity"
        );
        assertGenericReturnTypeDoesNotContain(
                com.edusmart.manager.service.student.StudentBillService.class,
                "getHourPackageSummary",
                new Class<?>[0],
                "EduStudentHourPackageEntity"
        );
        assertGenericReturnTypeDoesNotContain(
                StudentProfileService.class,
                "getCurrentProfile",
                new Class<?>[0],
                "EduStudentProfileEntity"
        );
    }

    private void assertExceptionHandlerReturnType(Class<?> type,
                                                  String methodName,
                                                  Class<?>[] parameterTypes) throws NoSuchMethodException {
        Method method = type.getMethod(methodName, parameterTypes);
        assertThat(method.getAnnotation(ExceptionHandler.class)).isNotNull();
        assertThat(method.getReturnType()).isEqualTo(Result.class);
    }

    private void assertRawReturnType(Class<?> type,
                                     String methodName,
                                     Class<?>[] parameterTypes,
                                     Class<?> expectedReturnType) throws NoSuchMethodException {
        Method method = type.getMethod(methodName, parameterTypes);
        assertThat(method.getReturnType()).isEqualTo(expectedReturnType);
    }

    private void assertGenericReturnTypeDoesNotContain(Class<?> type,
                                                       String methodName,
                                                       Class<?>[] parameterTypes,
                                                       String fragment) throws NoSuchMethodException {
        Method method = type.getMethod(methodName, parameterTypes);
        assertThat(method.getGenericReturnType().getTypeName()).doesNotContain(fragment);
    }
}
