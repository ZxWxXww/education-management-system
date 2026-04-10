package com.edusmart.manager.service;

import com.edusmart.manager.dto.UserCreateDTO;
import com.edusmart.manager.dto.UserQueryDTO;
import com.edusmart.manager.entity.EduUserEntity;
import com.edusmart.manager.common.PageData;

public interface UserService {
    Long createUser(UserCreateDTO dto);

    PageData<EduUserEntity> pageUsers(UserQueryDTO queryDTO);
}
