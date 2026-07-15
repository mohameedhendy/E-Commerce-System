package com.ecommerce.ecommerce_backend.service;

import com.auth0.jwt.exceptions.JWTVerificationException;
import com.ecommerce.ecommerce_backend.config.ApplicationProperties;
import com.ecommerce.ecommerce_backend.dao.LocalUserDao;
import com.ecommerce.ecommerce_backend.dao.VerificationTokenDAO;
import com.ecommerce.ecommerce_backend.dto.LoginBody;
import com.ecommerce.ecommerce_backend.dto.LoginResponse;
import com.ecommerce.ecommerce_backend.dto.PasswordResetBody;
import com.ecommerce.ecommerce_backend.dto.RegistrationBody;
import com.ecommerce.ecommerce_backend.exception.EmailFailureException;
import com.ecommerce.ecommerce_backend.exception.InvalidCredentialsException;
import com.ecommerce.ecommerce_backend.exception.InvalidTokenException;
import com.ecommerce.ecommerce_backend.exception.UserAlreadyExistException;
import com.ecommerce.ecommerce_backend.exception.UserNotVerifiedException;
import com.ecommerce.ecommerce_backend.model.LocalUser;
import com.ecommerce.ecommerce_backend.model.Role;
import com.ecommerce.ecommerce_backend.model.VerificationToken;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.Locale;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private static final String INVALID_PASSWORD_RESET_TOKEN =
            "Invalid or expired password reset token";

    private static final String INVALID_REFRESH_TOKEN =
            "Invalid or expired refresh token";

    private static final long
            VERIFICATION_EMAIL_RESEND_INTERVAL_MILLIS =
            60L * 60L * 1000L;

    private final LocalUserDao userDao;
    private final EncryptionService encryptionService;
    private final JWTService jwtService;
    private final EmailService emailService;
    private final VerificationTokenDAO verificationTokenDAO;
    private final ApplicationProperties applicationProperties;

    @Transactional(
            rollbackFor = EmailFailureException.class
    )
    public LocalUser registerUser(
            RegistrationBody registrationBody
    ) throws UserAlreadyExistException,
            EmailFailureException {

        String normalizedUsername =
                normalizeUsername(
                        registrationBody.getUsername()
                );

        String normalizedEmail =
                normalizeEmail(
                        registrationBody.getEmail()
                );

        boolean usernameExists =
                userDao.findByUsernameIgnoreCase(
                        normalizedUsername
                ).isPresent();

        boolean emailExists =
                userDao.findByEmailIgnoreCase(
                        normalizedEmail
                ).isPresent();

        if (usernameExists || emailExists) {
            throw new UserAlreadyExistException();
        }

        LocalUser user = new LocalUser();

        user.setRole(Role.USER);
        user.setUsername(normalizedUsername);
        user.setEmail(normalizedEmail);

        user.setFirstName(
                registrationBody
                        .getFirstName()
                        .trim()
        );

        user.setLastName(
                registrationBody
                        .getLastName()
                        .trim()
        );

        user.setPassword(
                encryptionService.encryptPassword(
                        registrationBody.getPassword()
                )
        );

        boolean emailVerificationEnabled =
                applicationProperties
                        .email()
                        .verification()
                        .enabled();

        if (emailVerificationEnabled) {

            VerificationToken verificationToken =
                    createVerificationToken(user);

            emailService.sendVerificationEmail(
                    verificationToken
            );

        } else {
            user.setEmailVerified(true);
        }

        return userDao.save(user);
    }

    @Transactional(
            rollbackFor = EmailFailureException.class
    )
    public LoginResponse loginUser(
            LoginBody loginBody
    ) throws UserNotVerifiedException,
            EmailFailureException {

        String normalizedUsername =
                normalizeUsername(
                        loginBody.getUsername()
                );

        LocalUser user = userDao
                .findByUsernameIgnoreCase(
                        normalizedUsername
                )
                .orElseThrow(
                        InvalidCredentialsException::new
                );

        boolean passwordMatches =
                encryptionService.verifyPassword(
                        loginBody.getPassword(),
                        user.getPassword()
                );

        if (!passwordMatches) {
            throw new InvalidCredentialsException();
        }

        if (user.isEmailVerified()) {
            return createLoginResponse(user);
        }

        boolean emailVerificationEnabled =
                applicationProperties
                        .email()
                        .verification()
                        .enabled();

        if (!emailVerificationEnabled) {
            throw new UserNotVerifiedException(false);
        }

        boolean resendVerificationEmail =
                shouldResendVerificationEmail(
                        user.getId()
                );

        if (resendVerificationEmail) {

            VerificationToken verificationToken =
                    createVerificationToken(user);

            verificationTokenDAO.save(
                    verificationToken
            );

            emailService.sendVerificationEmail(
                    verificationToken
            );
        }

        throw new UserNotVerifiedException(
                resendVerificationEmail
        );
    }

    @Transactional
    public boolean verifyUser(
            String token
    ) {

        String email;

        try {
            email = jwtService
                    .getVerificationEmail(token);

        } catch (JWTVerificationException ex) {
            return false;
        }

        Optional<VerificationToken> optionalToken =
                verificationTokenDAO
                        .findByToken(token);

        if (optionalToken.isEmpty()) {
            return false;
        }

        VerificationToken verificationToken =
                optionalToken.get();

        LocalUser user =
                verificationToken.getUser();

        if (!user.getEmail()
                .equalsIgnoreCase(email)) {

            return false;
        }

        if (user.isEmailVerified()) {
            return false;
        }

        user.setEmailVerified(true);

        userDao.save(user);

        verificationTokenDAO.deleteByUser(user);

        return true;
    }

    @Transactional
    public LoginResponse refreshAccessToken(
            String refreshToken
    ) throws InvalidTokenException {

        JWTService.RefreshTokenData tokenData;

        try {
            tokenData =
                    jwtService.getRefreshTokenData(
                            refreshToken
                    );

        } catch (JWTVerificationException ex) {

            throw new InvalidTokenException(
                    INVALID_REFRESH_TOKEN
            );
        }

        if (tokenData.username() == null
                || tokenData.version() == null) {

            throw new InvalidTokenException(
                    INVALID_REFRESH_TOKEN
            );
        }

        LocalUser user = userDao
                .findByUsernameIgnoreCase(
                        tokenData.username()
                )
                .orElseThrow(() ->
                        new InvalidTokenException(
                                INVALID_REFRESH_TOKEN
                        )
                );

        if (!user.isEmailVerified()) {
            throw new InvalidTokenException(
                    INVALID_REFRESH_TOKEN
            );
        }

        int updatedRows =
                userDao.rotateRefreshTokenVersion(
                        user.getId(),
                        tokenData.version()
                );

        if (updatedRows == 0) {
            throw new InvalidTokenException(
                    INVALID_REFRESH_TOKEN
            );
        }

        LocalUser rotatedUser = userDao
                .findById(user.getId())
                .orElseThrow(() ->
                        new InvalidTokenException(
                                INVALID_REFRESH_TOKEN
                        )
                );

        return createLoginResponse(
                rotatedUser
        );
    }

    public void forgotPassword(
            String email
    ) throws EmailFailureException {

        String normalizedEmail =
                normalizeEmail(email);

        Optional<LocalUser> optionalUser =
                userDao.findByEmailIgnoreCase(
                        normalizedEmail
                );

        if (optionalUser.isEmpty()) {
            return;
        }

        LocalUser user =
                optionalUser.get();

        String token =
                jwtService.generatePasswordResetJWT(
                        user
                );

        emailService.sendPasswordResetEmail(
                user,
                token
        );
    }

    @Transactional
    public void resetPassword(
            PasswordResetBody body
    ) throws InvalidTokenException {

        JWTService.PasswordResetTokenData tokenData;

        try {
            tokenData =
                    jwtService.getPasswordResetData(
                            body.getToken()
                    );

        } catch (JWTVerificationException ex) {

            throw new InvalidTokenException(
                    INVALID_PASSWORD_RESET_TOKEN
            );
        }

        if (tokenData.email() == null
                || tokenData.version() == null) {

            throw new InvalidTokenException(
                    INVALID_PASSWORD_RESET_TOKEN
            );
        }

        LocalUser user = userDao
                .findByEmailIgnoreCase(
                        tokenData.email()
                )
                .orElseThrow(() ->
                        new InvalidTokenException(
                                INVALID_PASSWORD_RESET_TOKEN
                        )
                );

        String encodedPassword =
                encryptionService.encryptPassword(
                        body.getPassword()
                );

        int updatedRows =
                userDao.updatePasswordIfResetVersionMatches(
                        user.getId(),
                        tokenData.version(),
                        encodedPassword
                );

        if (updatedRows == 0) {
            throw new InvalidTokenException(
                    INVALID_PASSWORD_RESET_TOKEN
            );
        }
    }

    private boolean shouldResendVerificationEmail(
            Long userId
    ) {

        Timestamp resendCutoff =
                new Timestamp(
                        System.currentTimeMillis()
                                - VERIFICATION_EMAIL_RESEND_INTERVAL_MILLIS
                );

        return verificationTokenDAO
                .findFirstByUser_IdOrderByIdDesc(
                        userId
                )
                .map(verificationToken ->
                        verificationToken
                                .getCreatedTimeStamp()
                                .before(resendCutoff)
                )
                .orElse(true);
    }

    private VerificationToken createVerificationToken(
            LocalUser user
    ) {

        VerificationToken verificationToken =
                new VerificationToken();

        verificationToken.setToken(
                jwtService.generateVerificationJWT(
                        user
                )
        );

        verificationToken.setCreatedTimeStamp(
                new Timestamp(
                        System.currentTimeMillis()
                )
        );

        verificationToken.setUser(user);

        user.getVerificationTokens()
                .add(verificationToken);

        return verificationToken;
    }

    private LoginResponse createLoginResponse(
            LocalUser user
    ) {

        String accessToken =
                jwtService.generateToken(user);

        String refreshToken =
                jwtService.generateRefreshToken(user);

        return new LoginResponse(
                accessToken,
                refreshToken
        );
    }

    private String normalizeUsername(
            String username
    ) {

        return username.trim();
    }

    private String normalizeEmail(
            String email
    ) {

        return email
                .trim()
                .toLowerCase(Locale.ROOT);
    }
}