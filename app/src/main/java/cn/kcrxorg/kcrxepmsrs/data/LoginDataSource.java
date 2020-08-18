package cn.kcrxorg.kcrxepmsrs.data;

import android.content.Context;

import cn.kcrxorg.kcrxepmsrs.businessmodule.mapper.UserMapper;
import cn.kcrxorg.kcrxepmsrs.data.model.LoggedInUser;

import java.io.IOException;

/**
 * Class that handles authentication w/ login credentials and retrieves user information.
 */
public class LoginDataSource {
    UserMapper userMapper;
    public Result<LoggedInUser> login(Context context,String username, String password) {
        //username=操作人
        //password=复核人
        try {
        // TODO: handle loggedInUser authentication
            userMapper=new UserMapper(context);
            if(userMapper.checkUser(username)||userMapper.checkUser(password))
            {
                LoggedInUser loggedInUser=new LoggedInUser("id",username+":"+password);
                return new Result.Success<>(loggedInUser);
            }
            else if(username.equals(password))
            {
                LoggedInUser loggedInUser=new LoggedInUser("id","0");
                return new Result.Success<>(loggedInUser);
            }else
            {
                return new Result.Error(new IOException("登录失败"));
            }



    } catch (Exception e) {
        return new Result.Error(new IOException("Error logging in", e));
    }
}

    public void logout() {
        // TODO: revoke authentication
    }
}
