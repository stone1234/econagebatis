package com.econage.core.db.mybatis.entity;

import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDateTime;

/*
* 对entity类，强制要求带有4个跟踪字段，并由框架自动处理
* */
public abstract class BaseEntity implements Serializable,Cloneable {
    private LocalDateTime createDate;
    private String createUser;
    private LocalDateTime modDate;
    private String modUser;

    public LocalDateTime getCreateDate() {
        return createDate;
    }

    public void setCreateDate(LocalDateTime createDate) {
        this.createDate = createDate;
    }

    public String getCreateUser() {
        return createUser;
    }

    public void setCreateUser(String createUser) {
        this.createUser = createUser;
    }

    public LocalDateTime getModDate() {
        return modDate;
    }

    public void setModDate(LocalDateTime modDate) {
        this.modDate = modDate;
    }

    public String getModUser() {
        return modUser;
    }

    public void setModUser(String modUser) {
        this.modUser = modUser;
    }

    @Override
    public BaseEntity clone() throws CloneNotSupportedException {
        return (BaseEntity)super.clone();
    }
}
