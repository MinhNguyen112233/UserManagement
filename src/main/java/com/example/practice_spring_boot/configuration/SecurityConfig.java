package com.example.practice_spring_boot.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final String[] PUBLIC_ENDPOINTS = {"/users", "/auth/token", "/auth/introspect", "/auth/logout", "/auth/refresh"};

    //custom jwtDecoder
    private CustomJwtDecoder jwtDecoder;

    public SecurityConfig(CustomJwtDecoder jwtDecoder) {
        this.jwtDecoder = jwtDecoder;
    }

    //hàm này cho phép các method có quyền truy cập hay không
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
        httpSecurity.authorizeHttpRequests(request ->
                request.requestMatchers(HttpMethod.POST, PUBLIC_ENDPOINTS).permitAll()
                        //phải là scope admin thì mới vào được user
                        //do convert nên là SCOPE -> ROLE
                        //thực tế không phân quyền kiểu này
//                        .requestMatchers(HttpMethod.GET, "/users").hasAuthority("ROLE_ADMIN")
                        .anyRequest().authenticated());

        //decode token
        httpSecurity.oauth2ResourceServer(oauth2 ->
                oauth2.jwt(jwtConfigurer -> jwtConfigurer.decoder(jwtDecoder)
                        .jwtAuthenticationConverter(jwtAuthenticationConverter()))
                        .authenticationEntryPoint(new JwtAuthenticationEntryPoint()));

        //disable các csrf
        httpSecurity.csrf(AbstractHttpConfigurer :: disable);

        return httpSecurity.build();
    }

    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();

        //đứng từ trang web nào có thể truy cập
        config.addAllowedOrigin("http://localhost:8080");

        config.addAllowedMethod("*"); //cho phép tất cả method

        config.addAllowedHeader("*");

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        return new CorsFilter(source);
    }

    //cung cấp 1 jwtDecoder ở đây :
    //verify token
    //custom jwtDecoder
//    @Bean
//    public JwtDecoder jwtDecoder(){
//
//        SecretKey secretKey = new SecretKeySpec(SIGNER_KEY.getBytes(), "HS512");
//
//        //sử dụng nimbus để mã hóa token
//        return NimbusJwtDecoder.withSecretKey(secretKey)
//                .macAlgorithm(MacAlgorithm.HS512)
//                .build();
//    }

    //hàm này convert từ SCOPE SANG ROLE
    @Bean
    JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter jwtGrantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();
        //jwtGrantedAuthoritiesConverter.setAuthorityPrefix("ROLE_");
        // do đã thêm ROLE_ ở buildScope ở AuthenticationServiceImpl nên bỏ ROLE_
        jwtGrantedAuthoritiesConverter.setAuthorityPrefix("");
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(jwtGrantedAuthoritiesConverter);
        return converter;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(10);
    }
}
