package com.notex.student_notes.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.notex.student_notes.auth.controller.AuthController;
import com.notex.student_notes.auth.dto.RegisterUserDto;
import com.notex.student_notes.auth.service.AuthService;
import com.notex.student_notes.auth.service.JwtService;
import com.notex.student_notes.user.dto.UserDto;
import com.notex.student_notes.user.service.CustomUserDetailsService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@WebMvcTest(AuthController.class)
public class AuthControllerTests {
    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    AuthService authService;

    @MockitoBean
    JwtService jwtService;

    @MockitoBean
    CustomUserDetailsService customUserDetailsService;

    @Autowired
    ObjectMapper objectMapper;

    @TestConfiguration
    static class TestSecurityConfig{
        @Bean
        SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception{
            return http
                    .csrf(AbstractHttpConfigurer::disable)
                    .authorizeHttpRequests(auth -> auth.anyRequest().permitAll()).build();
        }
    }

    @Test
    void register_ShouldReturnCreatedStatus_WhenDataIsValid() throws Exception {
        RegisterUserDto input = new RegisterUserDto();
        input.setUsername("test");
        input.setPassword("password123");
        input.setEmail("test@example.com");
        input.setFirstName("First");
        input.setLastName("Last");

        UserDto response = new UserDto();
        response.setId(1L);
        response.setUsername("test");
        response.setEmail("test@example.com");
        response.setFirstName("First");
        response.setLastName("Last");

        when(authService.register(any(RegisterUserDto.class))).thenReturn(response);

        mockMvc.perform(MockMvcRequestBuilders.post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isCreated())
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").value(1L))
                .andExpect(MockMvcResultMatchers.jsonPath("$.username").value("test"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.email").value("test@example.com"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.firstName").value("First"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.lastName").value("Last"));

        verify(authService).register(any(RegisterUserDto.class));

    }
}
