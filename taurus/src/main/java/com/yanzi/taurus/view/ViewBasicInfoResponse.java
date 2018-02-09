package com.yanzi.taurus.view;

import com.yanzi.common.controller.view.ViewResponseBase;
import com.yanzi.common.entity.user.PermissionInfo;
import com.yanzi.common.entity.user.UserInfo;
import com.yanzi.taurus.entity.UserCourseInfo;
import com.yanzi.taurus.entity.base.FriendInfo;


public class ViewBasicInfoResponse extends ViewResponseBase {
    private UserInfo basicInfo;

    private PermissionInfo permissionInfo;
    
    // 课程信息
    private UserCourseInfo userCourseInfo;

    // 驻留时间
    private long userAppDuration;
    
    private FriendInfo userFriendInfo;
    
    public UserInfo getBasicInfo() {
        return basicInfo;
    }

    public UserCourseInfo getUserCourseInfo() {
		return userCourseInfo;
	}

	public void setUserCourseInfo(UserCourseInfo userCourseInfo) {
		this.userCourseInfo = userCourseInfo;
	}

	public long getUserAppDuration() {
		return userAppDuration;
	}

	public void setUserAppDuration(long userAppDuration) {
		this.userAppDuration = userAppDuration;
	}

	public FriendInfo getUserFriendInfo() {
		return userFriendInfo;
	}

	public void setUserFriendInfo(FriendInfo userFriendInfo) {
		this.userFriendInfo = userFriendInfo;
	}

	public void setBasicInfo(UserInfo basicInfo) {
        this.basicInfo = basicInfo;
    }

    public PermissionInfo getPermissionInfo() {
        return permissionInfo;
    }

    public void setPermissionInfo(PermissionInfo permissionInfo) {
        this.permissionInfo = permissionInfo;
    }
}
