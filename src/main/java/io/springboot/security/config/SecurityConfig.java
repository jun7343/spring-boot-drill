package io.springboot.security.config;

import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    private AuthenticationSuccessHandler authenticationSuccessHandler;


    public SecurityConfig(AuthenticationSuccessHandler authenticationSuccessHandler) {
        this.authenticationSuccessHandler = authenticationSuccessHandler;
    }

    // 정적파일(js, template, css, images 등) authentication 패스   * 설정 안 하게 된다면 Security filter 에 의해 정적파일이 호출이 안됨
    @Override
    public void configure(WebSecurity web) throws Exception {
        web.ignoring().requestMatchers(PathRequest.toStaticResources().atCommonLocations());
    }

    // 기본 Security 설정
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.authorizeRequests()
                .antMatchers("/", "/hello").permitAll()         // {'/', '/hello'} URL 은 인증 패스
                //.antMatchers(HttpMethod.GET, "/", "/hello")              // HttpMethod 기입 방식으로 상세히 구현
                //.antMatchers(HttpMethod.POST, "/", "/hello")
                .anyRequest().authenticated()                              // 나머지 request 는 인증 필요
            .and() 
                .formLogin()                                               // form login 방식 사용
                .loginPage("/login")                                       // login page URL 지정 (GET)
                .loginProcessingUrl("/login")                              // login processing 수행 할 URL 지정 (POST)
                .usernameParameter("username")                             // user name parameter 설정
                .passwordParameter("password")                             // password parameter 설정
                .successHandler(authenticationSuccessHandler)
                .defaultSuccessUrl("/")                                    // login 성공시 이동할 주소
                .failureUrl("/login?error")                                // login 실패시 이동할 주소
                //.failureHandler(new SimpleUrlAuthenticationFailureHandler()) // failure handler
                .permitAll()                                               // 위에 지정한 URL 은 전부 인증 필요 없음
            .and() 
                .logout()                                                  // logout 처리
                .logoutUrl("/logout")                                      // logout 처리 URL
                .logoutSuccessUrl("/")                                     // logout 성공시 이동할 URL
                .deleteCookies("JSESSIONID", "remember-me")                // logout 후 쿠키 삭제
                //.addLogoutHandler(new CompositeLogoutHandler())            // logout handler
                //.logoutSuccessHandler(new SimpleUrlLogoutSuccessHandler()) // logout 성공 후 handler
            .and()
                .rememberMe()
                .rememberMeParameter("remember")                           // remember-me parameter
                .tokenValiditySeconds(3600)                                // default 14일
                .alwaysRemember(false)                                     // remember-me 기능 활성화 되지 않아도 실행 여부
                //.userDetailsService(new AccountService())
            .and()
                .sessionManagement()                                        // Session 관리
                .maximumSessions(1)                                         // maximum session 갯수
                .maxSessionsPreventsLogin(true)                             // 동시 로그인 차단
                .expiredUrl("/login");                                      // session 만료시 이동할 URL
    }

    // PasswordEncoder Bean 등록
    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }
}
