package com.agonylua.smartKitchen.dto;

import java.io.File;

public class UserProfileDTO {
    private String nickname;
    private File avatarFile;


    public UserProfileDTO(String nickname) {
        this.nickname = nickname;
    }

    public UserProfileDTO(File avatarFile) {
        this.avatarFile = avatarFile;
    }

    public UserProfileDTO(String nickname, File avatarFile) {
        this.nickname = nickname;
        this.avatarFile = avatarFile;
    }

    public String getNickname() {
        return nickname;
    }

    public File getAvatarFile() {
        return avatarFile;
    }
}
