package com.FermeDirecte.FermeDirecte.service;

import com.FermeDirecte.FermeDirecte.dto.address.*;
import com.FermeDirecte.FermeDirecte.entity.Address;
import com.FermeDirecte.FermeDirecte.entity.User;
import com.FermeDirecte.FermeDirecte.exception.BadRequestException;
import com.FermeDirecte.FermeDirecte.exception.ResourceNotFoundException;
import com.FermeDirecte.FermeDirecte.repository.AddressRepository;
import com.FermeDirecte.FermeDirecte.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AddressService {

    private final AddressRepository addressRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<AddressResponse> getMesAdresses(String email) {
        User user = getUser(email);

        return addressRepository.findByUser_Id(user.getId())
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public AddressResponse ajouter(AddressRequest request, String email) {
        User user = getUser(email);

        // ✅ Si adresse principale → désactiver les autres
        if (Boolean.TRUE.equals(request.getPrincipal())) {
            addressRepository.findByUser_Id(user.getId())
                    .forEach(a -> {
                        a.setPrincipal(false);
                        addressRepository.save(a);
                    });
        }

        Address address = Address.builder()
                .user(user)
                .prenom(request.getPrenom())
                .nom(request.getNom())
                .rue(request.getRue())
                .ville(request.getVille())
                .codePostal(request.getCodePostal())
                .pays(request.getPays())
                .gouvernorat(request.getGouvernorat())
                .telephone(request.getTelephone())
                .instructions(request.getInstructions())
                .principal(Boolean.TRUE.equals(request.getPrincipal()))
                .build();

        return toResponse(addressRepository.save(address));
    }

    @Transactional
    public AddressResponse modifier(Long id, AddressRequest request, String email) {

        User user = getUser(email);

        Address address = addressRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Adresse introuvable"));

        // ✅ sécurité : vérifier que l'adresse appartient au user
        if (!address.getUser().getId().equals(user.getId())) {
            throw new BadRequestException("Non autorisé à modifier cette adresse");
        }

        if (Boolean.TRUE.equals(request.getPrincipal())) {
            addressRepository.findByUser_Id(user.getId())
                    .forEach(a -> {
                        a.setPrincipal(false);
                        addressRepository.save(a);
                    });
        }

        address.setPrenom(request.getPrenom());
        address.setNom(request.getNom());
        address.setRue(request.getRue());
        address.setVille(request.getVille());
        address.setCodePostal(request.getCodePostal());
        address.setPays(request.getPays());
        address.setGouvernorat(request.getGouvernorat());
        address.setTelephone(request.getTelephone());
        address.setInstructions(request.getInstructions());
        address.setPrincipal(Boolean.TRUE.equals(request.getPrincipal()));

        return toResponse(addressRepository.save(address));
    }

    @Transactional
    public void supprimer(Long id, String email) {

        User user = getUser(email);

        Address address = addressRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Adresse introuvable"));

        // ✅ sécurité
        if (!address.getUser().getId().equals(user.getId())) {
            throw new BadRequestException("Non autorisé à supprimer cette adresse");
        }

        addressRepository.delete(address);
    }

    private User getUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur introuvable"));
    }

    private AddressResponse toResponse(Address a) {
        return AddressResponse.builder()
                .id(a.getId())
                .prenom(a.getPrenom())
                .nom(a.getNom())
                .rue(a.getRue())
                .ville(a.getVille())
                .codePostal(a.getCodePostal())
                .pays(a.getPays())
                .gouvernorat(a.getGouvernorat())
                .telephone(a.getTelephone())
                .instructions(a.getInstructions())
                .principal(a.getPrincipal())
                .build();
    }
}