package com.example.practice_spring_boot.service;

import com.example.practice_spring_boot.dto.request.AuthenticationRequest;
import com.example.practice_spring_boot.dto.request.IntrospectRequest;
import com.example.practice_spring_boot.dto.request.LogoutRequest;
import com.example.practice_spring_boot.dto.request.RefreshRequest;
import com.example.practice_spring_boot.dto.response.AuthenticationResponse;
import com.example.practice_spring_boot.dto.response.IntrospectResponse;
import com.example.practice_spring_boot.exception.AppException;
import com.example.practice_spring_boot.exception.ErrorCode;
import com.example.practice_spring_boot.model.InvalidatedToken;
import com.example.practice_spring_boot.model.User;
import com.example.practice_spring_boot.repository.InvalidatedTokenRepository;
import com.example.practice_spring_boot.repository.UserRepository;
import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.text.ParseException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.StringJoiner;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthenticationServiceImpl implements AuthenticationService {

    UserRepository userRepository;

    InvalidatedTokenRepository invalidatedTokenRepository;

    @NonFinal
    //@Value("${jwt.signerkey}")
    protected static String SIGNER_KEY = "aEFhQeBzN72y5oG0saJUv5Ez+INXBynF8T4+L8BdklZ3OvJpQdaoSTWrPf6eJz+i";

    protected long VALID_DURATION = 5;
    protected long REFRESH_DURATION = 120;

    // hàm kiếm tra authen password
    @Override
    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        var user = userRepository.findByUsername(request.getUsername()).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(10);
        // kiểm tra xem password của request có giống của user không ?
        boolean authenticate =  passwordEncoder.matches(request.getPassword(), user.getPassword());
        if (!authenticate) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        //nếu authenticate thành công thì tạo token
        var token = generateToken(user); // tạo token từ username lấy từ request
        return AuthenticationResponse.builder()
                .token(token)
                .authenticated(true)
                .build();
    }

    //hàm verify jwt
    @Override
    public IntrospectResponse introspect(IntrospectRequest request) throws JOSEException, ParseException {
        var token = request.getToken();

        boolean invalid = true;
        //verify khóa truyền vào
        try{
            verifyToken(token, false);
        }catch(AppException e){
            invalid = false;
        }

        return IntrospectResponse.builder()
                .valid(invalid)
                .build();

    }

    //tạo một jwt
    @Override
    public String generateToken(User user) {
        JWSHeader header = new JWSHeader(JWSAlgorithm.HS512);

        //body : nội dung quan trọng trong một jwt
        JWTClaimsSet jwtClaimsSet = new JWTClaimsSet.Builder()
                .subject(user.getUsername())
                .issuer("minhcoixin")
                .issueTime(new Date())
                .expirationTime(new Date(Instant.now().plus(VALID_DURATION, ChronoUnit.SECONDS).toEpochMilli()))
                .jwtID(UUID.randomUUID().toString())
                .claim("scope", buildScope(user)) // custom claim theo ý muốn
                .build();

        Payload payload = new Payload(jwtClaimsSet.toJSONObject());
        JWSObject jwsObject = new JWSObject(header, payload);

        //chữ kí token
        try {
            jwsObject.sign(new MACSigner(SIGNER_KEY.getBytes()));
            return jwsObject.serialize();
        } catch (JOSEException e) {
            log.error("Cannot create token", e);
            throw new RuntimeException(e);
        }
    }

    //hàm logout
    @Override
    public void logout(LogoutRequest request) throws ParseException, JOSEException {
        try {
            var signToken = verifyToken(request.getToken(), true);

            String jit = signToken.getJWTClaimsSet().getJWTID();
            Date expiryTime = signToken.getJWTClaimsSet().getExpirationTime();

            InvalidatedToken invalidatedToken =
                    InvalidatedToken.builder().id(jit).expiryTime(expiryTime).build();

            invalidatedTokenRepository.save(invalidatedToken);
        } catch (AppException exception) {
            log.info("Token already expired");
        }
    }

    @Override
    public AuthenticationResponse refresh(RefreshRequest request) throws ParseException, JOSEException {
        //kiểm tra hiệu lực của token
        var signedToken = verifyToken(request.getToken(), true);

        var jit = signedToken.getJWTClaimsSet().getJWTID();

        var expiryTime = signedToken.getJWTClaimsSet().getExpirationTime();

        InvalidatedToken invalidatedToken = InvalidatedToken.builder()
                .id(jit)
                .expiryTime(expiryTime)
                .build();

        invalidatedTokenRepository.save(invalidatedToken);

        var username = signedToken.getJWTClaimsSet().getSubject();

        var user = userRepository.findByUsername(username).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        var token = generateToken(user); // tạo token từ username lấy từ request
        return AuthenticationResponse.builder()
                .token(token)
                .authenticated(true)
                .build();
    }

    //hàm verifyToken
    //thêm cờ isRefresh : nếu true thì sẽ là verify cho token refresh
    //còn false thì sẽ là verify cho introspect
    private SignedJWT verifyToken(String token, boolean isRefresh) throws JOSEException, ParseException {
        JWSVerifier verifier = new MACVerifier(SIGNER_KEY.getBytes());

        SignedJWT signedJWT = SignedJWT.parse(token);

        Date expityTime = (isRefresh)
                ? new Date( signedJWT.getJWTClaimsSet().getIssueTime().toInstant().plus(REFRESH_DURATION, ChronoUnit.SECONDS).toEpochMilli())
        : signedJWT.getJWTClaimsSet().getExpirationTime();

        var verified = signedJWT.verify(verifier);

        if(!(verified && expityTime.after(new Date()))){
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        //kiểm tra xem cái token đó có trong bảng InvalidatedToken không
        //nếu có thì có nghĩa là token đó đã logout và phải chặn token đó lại ko cho authen nữa
        if(invalidatedTokenRepository.existsById(signedJWT.getJWTClaimsSet().getJWTID())){
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        return signedJWT;
    }

    //hàm lấy ra các role và đẩy vào scope(sinh ra một string chứa các role)

    private String buildScope(User user) {
        //scope cách nhau bởi dấu cách
        StringJoiner stringJoiner = new StringJoiner(" ");
        if (!CollectionUtils.isEmpty(user.getRoles()))
            user.getRoles().forEach(role -> {
                stringJoiner.add("ROLE_" + role.getName());
                if (!CollectionUtils.isEmpty(role.getPermissions()))
                    role.getPermissions().forEach(permission -> stringJoiner.add(permission.getName()));
            });

        return stringJoiner.toString();
    }

}
