package cn.kcrxorg.kcrxepms.businessmodule.cmdinfo;

public class UserCommand extends BaseCommand {
    User[] userInfoList;

    public User[] getUserInfoList() {
        return userInfoList;
    }

    public void setUserInfoList(User[] userInfoList) {
        this.userInfoList = userInfoList;
    }
}
