package com.servitrust.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.servitrust.dto.RegisterRequest;
import com.servitrust.exception.InvalidCredentialsException;
import com.servitrust.exception.UserNotFoundException;
import com.servitrust.model.User;
import com.servitrust.repository.UserRepository;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Autowired
    private GeoCodingService geoCodingService;

    @Override
    public User createUser(User user) {
        String hashed = passwordEncoder.encode(user.getPassword());
        user.setPassword(hashed);
        return userRepository.save(user);
    }

    @Override
    public User register(RegisterRequest request) {
        String email = request.getEmail() == null ? "" : request.getEmail().trim().toLowerCase();

        if (userRepository.findByEmail(email).isPresent()) {
            throw new RuntimeException("Email already exists");
        }

        String loc = request.getLocation() == null ? null : request.getLocation().trim();
        Double lat = request.getLatitude();
        Double lng = request.getLongitude();

        if ((lat == null || lng == null) && loc != null && !loc.isBlank()) {
            GeoCodingService.LatLng res = geoCodingService.geocode(loc);
            if (res != null) {
                lat = res.lat;
                lng = res.lng;
            }
        }

        User user = new User();
        user.setName(request.getName() == null ? "" : request.getName().trim());
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole("USER");
        user.setLocation(loc);
        user.setLatitude(lat);
        user.setLongitude(lng);

        return userRepository.save(user);
    }

    @Override
    public User login(String email, String password) {
        String normEmail = email == null ? "" : email.trim().toLowerCase();

        User user = userRepository.findByEmail(normEmail)
                .orElseThrow(() -> new InvalidCredentialsException("Invalid email or password"));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new InvalidCredentialsException("Invalid email or password");
        }

        return user;
    }

    @Override
    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found with id :" + id));
    }

    @Override
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }
}