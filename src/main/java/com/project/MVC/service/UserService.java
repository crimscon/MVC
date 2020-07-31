package com.project.MVC.service;

import com.project.MVC.model.Comment;
import com.project.MVC.model.Message;
import com.project.MVC.model.User;
import com.project.MVC.model.UserProfile;
import com.project.MVC.model.enums.Gender;
import com.project.MVC.model.enums.Role;
import com.project.MVC.repository.CommentRepository;
import com.project.MVC.repository.MessagesRepository;
import com.project.MVC.repository.UserProfileRepository;
import com.project.MVC.repository.UserRepository;
import com.project.MVC.util.ThumbnailUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class UserService implements UserDetailsService {
    private final UserRepository userRepo;
    private final UserProfileRepository userProfileRepo;
    private final MessagesService messagesService;
    private final MessagesRepository messagesRepository;
    private final CommentRepository commentRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${upload.path}")
    private String uploadPath;

    public UserService(UserRepository userRepo, UserProfileRepository userProfileRepo, MessagesService messagesService, MessagesRepository messagesRepository, CommentRepository commentRepository, @Lazy PasswordEncoder passwordEncoder) {
        this.userRepo = userRepo;
        this.userProfileRepo = userProfileRepo;
        this.messagesService = messagesService;
        this.messagesRepository = messagesRepository;
        this.commentRepository = commentRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepo.findByUsernameIgnoreCase(username);

        if (user == null) {
            throw new UsernameNotFoundException("User not found");
        }

        return user;
    }

    public User getUserByUsername(String username) {
        return userRepo.findByUsernameIgnoreCase(username);
    }

    public User findById(Long aLong) {
        return userRepo.getOne(aLong);
    }

    public List<User> getUserList(String filter) {
        List<User> users;

        if (filter != null && !filter.isEmpty()) {
            users = new ArrayList<>();

            User user = (User) loadUserByUsername(filter);
            if (user != null) users.add(user);
        } else {
            users = userRepo.findAll();
        }

        return users;
    }

    public void addUser(User user) {

        user.setActive(true);

        Set<Role> roleSet = new HashSet<>();

        if (findAll().isEmpty()) {
            roleSet.add(Role.ADMIN);
        }

        roleSet.add(Role.USER);
        user.setRoles(roleSet);
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        save(user);
    }

    public void saveUser(String username, String password,
                         Map<String, String> form, Long userId) {
        User user = findById(userId);
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));

        Set<String> roles = Arrays.stream(Role.values())
                .map(Role::name)
                .collect(Collectors.toSet());

        user.getRoles().clear();

        for (String key : form.keySet()) {
            if (roles.contains(key)) {
                user.getRoles().add(Role.valueOf(key));
            }
        }

        userRepo.save(user);
    }

    public void saveUser(User user, String email, String password,
                         MultipartFile file,
                         Gender gender, String phoneNumber, String dateOfBirth) throws IOException {
        UserProfile userProfile = user.getUserProfile() == null ?
                new UserProfile() : userProfileRepo.getOne(user.getUserProfile().getId());

        boolean passwordChange = false,
                emailChange = false,
                profilePicChange = false,
                phoneChange = false,
                dofChange = false,
                profileChange = false,
                genderChange = false;

        if (user.getEmail() == null || !user.getEmail().equals(email) && !email.isEmpty()) {
            emailChange = true;
            user.setEmail(email);
        }

        if (!user.getPassword().equals(password) && !password.isEmpty()) {
            passwordChange = true;
            user.setPassword(passwordEncoder.encode(password));
        }

        if (!userProfile.getGender().equals(gender)) {
            genderChange = true;
            userProfile.setGender(gender);
        }

        if (!dateOfBirth.equals("") && !dateOfBirth.isEmpty()) {
            String[] dateArr = dateOfBirth.split("-");

            LocalDateTime localDate = LocalDateTime.of(Integer.parseInt(dateArr[0]), Integer.parseInt(dateArr[1]), Integer.parseInt(dateArr[2]), 0, 0);

            if (!userProfile.getDateOfBirth().equals(localDate)) {
                dofChange = true;
                userProfile.setDateOfBirth(localDate);
            }
        }

        if (!userProfile.getPhoneNumber().equals(phoneNumber) && !phoneNumber.isEmpty()) {
            phoneChange = true;
            userProfile.setPhoneNumber(phoneNumber);
        }

        if (phoneChange || dofChange || genderChange) {
            profileChange = true;
            userProfile.setUser(user);
        }

        if (profileChange) {
            userProfileRepo.save(userProfile);
            user.setUserProfile(userProfile);
        }

        if (file != null && !file.getOriginalFilename().isEmpty()) {

            ThumbnailUtil.deleteIfExistFile(uploadPath, user.getProfile_pic());
            String filename = ThumbnailUtil.createFile(file, uploadPath, false);

            user.setProfile_pic(filename);
            profilePicChange = true;
        }

        if (passwordChange || emailChange || profilePicChange || profileChange) userRepo.save(user);
    }


    public List<User> findAll() {
        return userRepo.findAll();
    }

    public void save(User user) {
        userRepo.save(user);
    }

    public void deleteUser(User user) throws IOException {

        UserProfile userProfile = user.getUserProfile();
        Set<Message> messages = user.getMessages();
        Set<Comment> comments = user.getComments();
        List<Message> likes = messagesRepository.findAllWhereUserLike(user);

        comments.forEach(commentRepository::delete);
        likes.forEach(message -> {
            message.getLikes().remove(user);
            messagesRepository.save(message);
        });
        messages.forEach(message -> {
            Set<Comment> commentSet = message.getComments();
            commentSet.forEach(commentRepository::delete);
            messagesRepository.delete(message);
        });


        if (userProfile != null) {
            userProfileRepo.delete(userProfile);
        }
        if (!(user.getProfile_pic() == null || user.getProfile_pic().isEmpty())) {
            ThumbnailUtil.deleteIfExistFile(uploadPath, user.getProfile_pic());
        }

        userRepo.delete(user);


    }
}
