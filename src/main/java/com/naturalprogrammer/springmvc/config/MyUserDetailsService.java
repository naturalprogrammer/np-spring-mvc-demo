//package com.naturalprogrammer.springmvc.config;
//
//import com.naturalprogrammer.springmvc.user.repositories.UserRepository;
//import lombok.RequiredArgsConstructor;
//import org.springframework.security.core.userdetails.User;
//import org.springframework.security.core.userdetails.UserDetails;
//import org.springframework.security.core.userdetails.UserDetailsService;
//import org.springframework.security.core.userdetails.UsernameNotFoundException;
//import org.springframework.stereotype.Component;
//
//@Component
//@RequiredArgsConstructor
//public class MyUserDetailsService implements UserDetailsService {
//
//    private final UserRepository userRepository;
//
//    @Override
//    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
//
//        return userRepository
//                .findByEmail(username)
//                .map(user -> new User(user.getEmail(), user.getPassword(), user.getAuthorities()))
//                .orElseThrow(() -> new UsernameNotFoundException("No user found with username " + username));
//    }
//}
