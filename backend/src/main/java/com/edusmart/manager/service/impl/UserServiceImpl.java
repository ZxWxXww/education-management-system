package com.edusmart.manager.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.edusmart.manager.common.PageData;
import com.edusmart.manager.dto.UserCreateDTO;
import com.edusmart.manager.dto.UserQueryDTO;
import com.edusmart.manager.entity.EduUserEntity;
import com.edusmart.manager.mapper.EduUserMapper;
import com.edusmart.manager.service.UserService;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {
    private final EduUserMapper userMapper;

    public UserServiceImpl(EduUserMapper userMapper) {
        this.userMapper = userMapper;
    }

    @Override
    public Long createUser(UserCreateDTO dto) {
        EduUserEntity entity = new EduUserEntity();
        entity.setUsername(dto.getUsername());
        entity.setRealName(dto.getRealName());
        entity.setPhone(dto.getPhone());
        entity.setEmail(dto.getEmail());
        entity.setStatus(1);
        // TODO: 接入真实密码加密和角色绑定逻辑
        entity.setPasswordHash("TODO_ENCODED_PASSWORD");
        userMapper.insert(entity);
        return entity.getId();
    }

    @Override
    public PageData<EduUserEntity> pageUsers(UserQueryDTO queryDTO) {
        Page<EduUserEntity> page = userMapper.selectPage(
                new Page<>(queryDTO.getCurrent(), queryDTO.getSize()),
                null
        );
        return new PageData<>(page.getCurrent(), page.getSize(), page.getTotal(), page.getRecords());
    }
}
