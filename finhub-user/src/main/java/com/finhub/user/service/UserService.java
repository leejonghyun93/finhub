package com.finhub.user.service;

import com.finhub.user.dto.request.LoginRequest;
import com.finhub.user.dto.request.ReissueRequest;
import com.finhub.user.dto.request.SignupRequest;
import com.finhub.user.dto.response.LoginResponse;
import com.finhub.user.dto.response.TokenResponse;
import com.finhub.user.dto.response.UserInfoResponse;

public interface UserService {

    void signup(SignupRequest request);

    LoginResponse login(LoginRequest request);

    void logout(String email);

    TokenResponse reissue(ReissueRequest request);

    UserInfoResponse getMyInfo(String email);
}
