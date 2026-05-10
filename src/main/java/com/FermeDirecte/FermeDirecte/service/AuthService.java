package com.FermeDirecte.FermeDirecte.service;

import com.FermeDirecte.FermeDirecte.dto.auth.AuthResponse;
import com.FermeDirecte.FermeDirecte.dto.auth.ForgotPasswordRequest;
import com.FermeDirecte.FermeDirecte.dto.auth.LoginRequest;
import com.FermeDirecte.FermeDirecte.dto.auth.RegisterRequest;
import com.FermeDirecte.FermeDirecte.dto.auth.ResetPasswordRequest;
import com.FermeDirecte.FermeDirecte.entity.SellerProfile;
import com.FermeDirecte.FermeDirecte.entity.User;
import com.FermeDirecte.FermeDirecte.enums.Role;
import com.FermeDirecte.FermeDirecte.exception.BadRequestException;
import com.FermeDirecte.FermeDirecte.exception.DuplicateResourceException;
import com.FermeDirecte.FermeDirecte.exception.ResourceNotFoundException;
import com.FermeDirecte.FermeDirecte.repository.SellerProfileRepository;
import com.FermeDirecte.FermeDirecte.repository.UserRepository;
import com.FermeDirecte.FermeDirecte.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.UUID;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final SellerProfileRepository sellerProfileRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;

    @Transactional
    public AuthResponse register(RegisterRequest request) {

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Email déjà utilisé");
        }

        User user = User.builder()
                .email(request.getEmail())
                .motDePasse(passwordEncoder.encode(request.getMotDePasse()))
                .prenom(request.getPrenom())
                .nom(request.getNom())
                .telephone(request.getTelephone())
                .role(request.getRole())
                .actif(true)
                .build();

        userRepository.save(user);

        // Si l'utilisateur est un vendeur ou admin, créer automatiquement son profil vendeur
        if (request.getRole() == Role.SELLER || request.getRole() == Role.ADMIN) {
            String nomBoutique = request.getRole() == Role.ADMIN 
                ? "Administration FermeDirecte" 
                : request.getPrenom() + " " + request.getNom();
            
            SellerProfile sellerProfile = SellerProfile.builder()
                    .user(user)
                    .nomBoutique(nomBoutique)
                    .description(request.getRole() == Role.ADMIN 
                        ? "Boutique officielle de l'administration" 
                        : "Bienvenue dans ma boutique !")
                    .note(0.0)
                    .build();
            
            sellerProfileRepository.save(sellerProfile);
            user.setSellerProfile(sellerProfile);
        }

        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getEmail());
        String accessToken = jwtService.generateAccessToken(userDetails);
        String refreshToken = jwtService.generateRefreshToken(userDetails);

        user.setRefreshToken(refreshToken);
        userRepository.save(user);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .userId(user.getId())
                .email(user.getEmail())
                .role(user.getRole())
                .build();
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getMotDePasse()
                )
        );

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur introuvable"));

        if (!user.getActif()) {
            throw new BadRequestException("Compte désactivé");
        }

        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getEmail());
        String accessToken = jwtService.generateAccessToken(userDetails);
        String refreshToken = jwtService.generateRefreshToken(userDetails);

        user.setRefreshToken(refreshToken);
        userRepository.save(user);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .userId(user.getId())
                .email(user.getEmail())
                .role(user.getRole())
                .build();
    }

    @Transactional
    public AuthResponse refresh(String refreshToken) {

        User user = userRepository.findByRefreshToken(refreshToken)
                .orElseThrow(() -> new BadRequestException("Refresh token invalide"));

        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getEmail());

        if (jwtService.isTokenExpired(refreshToken)) {
            throw new BadRequestException("Refresh token expiré");
        }

        String newAccessToken = jwtService.generateAccessToken(userDetails);
        String newRefreshToken = jwtService.generateRefreshToken(userDetails);

        user.setRefreshToken(newRefreshToken);
        userRepository.save(user);

        return AuthResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .userId(user.getId())
                .email(user.getEmail())
                .role(user.getRole())
                .build();
    }

    @Transactional
    public void logout(String refreshToken) {
        userRepository.findByRefreshToken(refreshToken).ifPresent(user -> {
            user.setRefreshToken(null);
            userRepository.save(user);
        });
    }

    // -------------------------------------------------------
    // Réinitialisation de mot de passe
    // -------------------------------------------------------

    /**
     * Étape 1 : l'utilisateur soumet son email.
     * On génère un token UUID valable 1 heure et on le stocke en BDD.
     * En production, ce token serait envoyé par email.
     * Ici on le retourne directement dans la réponse (simulation).
     */
    @Transactional
    public String forgotPassword(ForgotPasswordRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("Aucun compte associé à cet email"));

        String token = UUID.randomUUID().toString();
        user.setResetToken(token);
        user.setResetTokenExpiry(LocalDateTime.now().plusHours(1));
        userRepository.save(user);

        // En production : envoyer un email avec le lien contenant le token
        // emailService.sendResetLink(user.getEmail(), token);

        // Pour la démo / Postman : on retourne le token directement
        return token;
    }

    /**
     * Étape 2 : l'utilisateur soumet le token + nouveau mot de passe.
     */
    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        User user = userRepository.findByResetToken(request.getToken())
                .orElseThrow(() -> new BadRequestException("Token invalide ou déjà utilisé"));

        if (user.getResetTokenExpiry() == null ||
                LocalDateTime.now().isAfter(user.getResetTokenExpiry())) {
            throw new BadRequestException("Token expiré. Veuillez refaire une demande.");
        }

        user.setMotDePasse(passwordEncoder.encode(request.getNouveauMotDePasse()));
        user.setResetToken(null);
        user.setResetTokenExpiry(null);
        userRepository.save(user);
    }
}