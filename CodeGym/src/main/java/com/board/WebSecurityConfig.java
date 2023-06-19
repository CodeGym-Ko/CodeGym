package com.board;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.web.SecurityFilterChain;

import com.board.service.UserDetailsServiceImpl;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@EnableWebSecurity
@Configuration
public class WebSecurityConfig {

	private final AuthSuccessHandler authSuccessHandler; //의존성 주입
	private final AuthFailureHandler authFailureHandler;
	private final UserDetailsServiceImpl userDetailsService;
	private final OAuth2SucessHandler oauth2SucessHandler;
    private final OAuth2FailureHandler oauth2FailureHandler;
//    private final ClientRegistrationRepository clientRegistrationRepository;
	
	//스프링시큐리티에서 암호화 관련 객체를 가져다가 스프링빈으로 등록 
	@Bean
	BCryptPasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}
	
	//스프링시큐리티 적용 제외 대상 설정 스프링빈 등록
	@Bean
	WebSecurityCustomizer webSecurityCustomizer() {
		
		return (web)->web.ignoring().requestMatchers("/img/**","/css/**","/profile/**", "/static/**");
	}
	
	// 로그인 처리
	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		
		// 폼 로그인
		http.formLogin(login -> login
                .usernameParameter("userid")   
                //.loginPage("/user/login") // 인증이 필요한 URL에 접근하면 가는 페이지
                .loginPage("/") 
                .successHandler(authSuccessHandler)  
                .failureHandler(authFailureHandler)
                .failureUrl("/user/login")	); // 실패시 이동할 페이지
		
		// auth2 로그인
		http
    	.oauth2Login(login -> login
            .loginPage("/")// 인증이 필요한 URL에 접근하면 가는 페이지
            .failureUrl("/user/login")	// 실패시 이동할 페이지
            .successHandler(oauth2SucessHandler)
            .failureHandler(oauth2FailureHandler));
		
		http
        .rememberMe(me -> me
                .key("something")
                .alwaysRemember(false) 
                .tokenValiditySeconds(3600 * 24 * 7)
                .rememberMeParameter("remember-me")
                .userDetailsService(userDetailsService)
                .authenticationSuccessHandler(authSuccessHandler));
		
		//접근권한 설정(권한 부여 : Authorization) -> 접근 권한 줄거면 사용
//				http
//					.authorizeHttpRequests()
//					.requestMatchers("/").permitAll();
////					.requestMatchers("/board/**").hasAnyAuthority("USER","MASTER")
////					.requestMatchers("/master/**").hasAnyAuthority("MASTER")
////					.anyRequest().authenticated(); // 모든 요청에 인증된 사용자만 접근 허용한다 -> 인증 x 리다이렉트
//				 
//				web
//			        .ignoring()
//			        .requestMatchers("/static/**");
		//세션 설정
        http
            .sessionManagement(management -> management
                    .maximumSessions(1)  // 로그인은 1개만 가능
                    .maxSessionsPreventsLogin(false)  // 다른 로그인 시도 -> 기존 로그인
                    .expiredUrl("/"));  // 세션이 만료된 경우 리다이렉트 될 url
				
		
		
      //로그 아웃
        http
            .logout(logout -> logout
                    .logoutUrl("/user/logout")
                    .logoutSuccessUrl("/")
                    .invalidateHttpSession(true)
                    .deleteCookies("JSESSIONID")
                    
                    //.permitAll() // 이거 하면 에러남
            		);
        
        
      //CSRF, CORS 공격 보안 비활성화
        http
           .csrf((csrf) -> csrf.disable())
           .cors((cors) -> cors.disable());
        
		System.out.println("스프링 시큐리티 설정 완료 !!!");
		
		// http.build()를 호출하여 HttpSecurity 객체를 SecurityFilterChain으로 빌드하고 반환
		return http.build();
		
		// 필터 체인은 Spring Security의 인증 및 권한 부여에 적용
	}
}