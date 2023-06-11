package com.board;

import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class OAuth2SucessHandler extends SimpleUrlAuthenticationSuccessHandler {

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
       
    	System.out.println("*************** 구글 인증 통과 ***************");
    	System.out.println("response" + response.toString() );
        //사용 정보 중 아이디(이메일),이름, 권한 정보 가져 오기
 
		setDefaultTargetUrl("/");
        super.onAuthenticationSuccess(request, response, authentication);
       
    }
}