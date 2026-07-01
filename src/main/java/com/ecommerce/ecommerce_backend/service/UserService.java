package com.ecommerce.ecommerce_backend.service;

import com.ecommerce.ecommerce_backend.dao.LocalUserDao;
import com.ecommerce.ecommerce_backend.dao.VerificationTokenDAO;
import com.ecommerce.ecommerce_backend.dto.LoginBody;
import com.ecommerce.ecommerce_backend.dto.PasswordResetBody;
import com.ecommerce.ecommerce_backend.dto.RegistrationBody;
import com.ecommerce.ecommerce_backend.exception.EmailFailureException;
import com.ecommerce.ecommerce_backend.exception.EmailNotFoundException;
import com.ecommerce.ecommerce_backend.exception.UserAlreadyExistException;
import com.ecommerce.ecommerce_backend.exception.UserNotVerifiedException;
import com.ecommerce.ecommerce_backend.model.LocalUser;
import com.ecommerce.ecommerce_backend.model.Role;
import com.ecommerce.ecommerce_backend.model.VerificationToken;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.ecommerce.ecommerce_backend.exception.InvalidTokenException;
import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    private LocalUserDao userDao;

    private EncryptionService encryptionService;

    private JWTService jwtService;

    private EmailService emailService;

    private VerificationTokenDAO verificationTokenDAO;

    @Value("${app.email.verification.enabled:false}")
    private boolean emailVerificationEnabled;

    public UserService(LocalUserDao localUserDao, EncryptionService encryptionService, JWTService jwtService
            , EmailService emailService, VerificationTokenDAO verificationTokenDAO) {
        this.userDao = localUserDao;
        this.encryptionService = encryptionService;
        this.jwtService = jwtService;
        this.emailService = emailService;
        this.verificationTokenDAO = verificationTokenDAO;
    }

    public LocalUser registerUser(RegistrationBody registrationBody) throws UserAlreadyExistException, EmailFailureException {
        if (userDao.findByEmailIgnoreCase(registrationBody.getEmail()).isPresent()
                || userDao.findByUsernameIgnoreCase(registrationBody.getUsername()).isPresent()) {
            throw new UserAlreadyExistException();
        }

        LocalUser user = new LocalUser();
        user.setRole(Role.USER);
        user.setEmail(registrationBody.getEmail());
        user.setUsername(registrationBody.getUsername());
        user.setFirstName(registrationBody.getFirstName());
        user.setLastName(registrationBody.getLastName());
        user.setPassword(encryptionService.encryptPassword(registrationBody.getPassword()));

        if (emailVerificationEnabled) {
            VerificationToken verificationToken = createVerificationToken(user);
            emailService.sendVerificationEmail(verificationToken);
        } else {
            user.setEmailVerified(true);
        }

        return userDao.save(user);
    }

    public String loginUser(LoginBody loginBody) throws UserNotVerifiedException, EmailFailureException {
        Optional<LocalUser> opUser = userDao.findByUsernameIgnoreCase(loginBody.getUsername());
        if (opUser.isPresent()) {
            LocalUser user = opUser.get();
            if (encryptionService.verifyPassword(loginBody.getPassword(), user.getPassword())) {
                if (user.isEmailVerified()) {
                    return jwtService.generateToken(user);
                } else {
                    if (!emailVerificationEnabled) {
                        throw new UserNotVerifiedException(false);
                    }

                    List<VerificationToken> verificationTokens =
                            verificationTokenDAO.findByUser_IdOrderByIdDesc(user.getId());

                    boolean resend = verificationTokens.isEmpty() ||
                            verificationTokens.get(0).getCreatedTimeStamp()
                                    .before(new Timestamp(System.currentTimeMillis() - (60 * 60 * 1000)));

                    if (resend) {
                        VerificationToken verificationToken = createVerificationToken(user);
                        verificationTokenDAO.save(verificationToken);
                        emailService.sendVerificationEmail(verificationToken);
                    }

                    throw new UserNotVerifiedException(resend);
                }
            }
        }
        return null;
    }

    private VerificationToken createVerificationToken(LocalUser user) {
        VerificationToken verificationToken = new VerificationToken();
        verificationToken.setToken(jwtService.generateVerificationJWT(user));
        verificationToken.setCreatedTimeStamp(new Timestamp(System.currentTimeMillis()));
        verificationToken.setUser(user);
        user.getVerificationTokens().add(verificationToken);
        return verificationToken;
    }

    @Transactional
    public boolean verifyUser(String token) {
        String email;

        try {
            email = jwtService.getVerificationEmail(token);
        } catch (JWTVerificationException ex) {
            return false;
        }

        Optional<VerificationToken> opToken = verificationTokenDAO.findByToken(token);

        if (opToken.isPresent()) {
            VerificationToken verificationToken = opToken.get();
            LocalUser user = verificationToken.getUser();

            if (!user.getEmail().equalsIgnoreCase(email)) {
                return false;
            }

            if (!user.isEmailVerified()) {
                user.setEmailVerified(true);
                userDao.save(user);
                verificationTokenDAO.deleteByUser(user);
                return true;
            }
        }

        return false;
    }

    public void forgotPassword(String email) throws EmailNotFoundException, EmailFailureException {
        Optional<LocalUser> opUser = userDao.findByEmailIgnoreCase(email);
        if (opUser.isPresent()) {
            LocalUser user = opUser.get();
            String token = jwtService.generatePasswordResetJWT(user);
            emailService.sendPasswordResetEmail(user, token);
        } else {
            throw new EmailNotFoundException();
        }
    }

    public void resetPassword(PasswordResetBody body) throws InvalidTokenException {
        String email;

        try {
            email = jwtService.getResetPasswordEmail(body.getToken());
        } catch (JWTVerificationException ex) {
            throw new InvalidTokenException("Invalid or expired password reset token");
        }

        Optional<LocalUser> opUser = userDao.findByEmailIgnoreCase(email);

        if (opUser.isEmpty()) {
            throw new InvalidTokenException("Invalid or expired password reset token");
        }

        LocalUser user = opUser.get();
        user.setPassword(encryptionService.encryptPassword(body.getPassword()));
        userDao.save(user);
    }
}
