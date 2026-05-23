package com.hostelmanagement.util;

import com.hostelmanagement.entity.User;
import com.hostelmanagement.enums.Role;
import com.hostelmanagement.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class DbQueryRunner implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DbQueryRunner(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
        System.out.println("=================================================");
        System.out.println("        STARTING DATABASE INITIALIZATION / AUDIT");
        System.out.println("=================================================");

        // Print existing users
        List<User> users = userRepository.findAll();
        System.out.println("Found " + users.size() + " total users in database:");
        for (User u : users) {
            System.out.println(" - ID: " + u.getId() + " | Name: " + u.getName() + " | Email: " + u.getEmail() + " | Role: " + u.getRole() + " | Active: " + u.getIsActive());
        }

        // Check if admin 'nirav@gmail.com' exists
        String adminEmail = "nirav@gmail.com";
        Optional<User> existingAdmin = userRepository.findByEmail(adminEmail);
        if (existingAdmin.isPresent()) {
            System.out.println("Admin " + adminEmail + " already exists. Updating password to 123456 to be absolutely sure.");
            User admin = existingAdmin.get();
            admin.setPassword(passwordEncoder.encode("123456"));
            admin.setIsActive(true);
            userRepository.save(admin);
            System.out.println("Admin " + adminEmail + " password reset successfully!");
        } else {
            System.out.println("Admin " + adminEmail + " does not exist. Creating it now...");
            User admin = User.builder()
                    .name("Nirav")
                    .email(adminEmail)
                    .password(passwordEncoder.encode("123456"))
                    .role(Role.ADMIN)
                    .isActive(true)
                    .phone("+1234567890")
                    .build();
            userRepository.save(admin);
            System.out.println("Admin " + adminEmail + " successfully created with password 123456!");
        }

        // Check if student 'kunj@gmail.com' exists
        String studentEmail = "kunj@gmail.com";
        Optional<User> existingStudent = userRepository.findByEmail(studentEmail);
        if (existingStudent.isPresent()) {
            System.out.println("Student " + studentEmail + " already exists. Resetting password to 123456.");
            User student = existingStudent.get();
            student.setPassword(passwordEncoder.encode("123456"));
            student.setIsActive(true);
            userRepository.save(student);
            System.out.println("Student " + studentEmail + " password reset successfully!");
        } else {
            System.out.println("Student " + studentEmail + " does not exist. Creating it as STUDENT block A room 101 now...");
            User student = User.builder()
                    .name("Kunj")
                    .email(studentEmail)
                    .password(passwordEncoder.encode("123456"))
                    .role(Role.STUDENT)
                    .hostelBlock("Block A")
                    .roomNumber("101")
                    .isActive(true)
                    .phone("+1234567891")
                    .build();
            userRepository.save(student);
            System.out.println("Student " + studentEmail + " successfully created with password 123456!");
        }

        System.out.println("=================================================");
        System.out.println("        DATABASE INITIALIZATION / AUDIT COMPLETE");
        System.out.println("=================================================");
    }
}
