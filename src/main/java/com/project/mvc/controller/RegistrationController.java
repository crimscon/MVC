package com.project.mvc.controller;

import com.project.mvc.model.User;
import com.project.mvc.service.UserService;
import com.project.mvc.util.ControllerUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.validation.Valid;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class RegistrationController {
    private final UserService userService;

    @GetMapping("/registration")
    public String registration() {
        return "registration";
    }

    @PostMapping("/registration")
    public String addUser(@RequestParam(defaultValue = "") String passwordConfirm,
                          @Valid User user,
                          BindingResult bindingResult,
                          Model model) {

        boolean hasError = user.getPassword() != null && !user.getPassword().equals(passwordConfirm);

        if (bindingResult.hasErrors() || hasError) {
            Map<String, String> errors = ControllerUtil.getErrors(bindingResult);

            if (hasError)
                errors.put("passwordError", "Пароли не одинаковые");

            User userFromDb = userService.getUserByUsername(user.getUsername());

            if (userFromDb != null) errors.put("usernameError", "Такой пользователь уже существует");

            model.addAttribute("errors", errors);
            model.addAttribute("user", user);

            return "registration";
        } else {
            userService.addUser(user);

            return "redirect:/login";
        }

    }

}
