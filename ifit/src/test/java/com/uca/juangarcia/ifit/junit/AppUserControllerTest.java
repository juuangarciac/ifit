package com.uca.juangarcia.ifit.junit;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.uca.juangarcia.ifit.helpers.JsonUtils;
import com.uca.juangarcia.ifit.helpers.UserTest;
import com.uca.juangarcia.ifit.modules.coach.repository.CoachModelTypeRepository;
import com.uca.juangarcia.ifit.modules.user.controller.AppUserController;
import com.uca.juangarcia.ifit.modules.user.dto.AppUserDto;
import com.uca.juangarcia.ifit.modules.user.model.AppUser;
import com.uca.juangarcia.ifit.modules.user.repository.AppRoleRepository;


@WebMvcTest(AppUserController.class)
public class AppUserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AppRoleRepository appRoleRepository;

    @MockitoBean
    private CoachModelTypeRepository coachModelTypeRepository;


    @Autowired
    private ObjectMapper objectMapper; 

    @Test
    public void createtest() throws Exception{
        System.out.println("Starting signuptest: ");
        Random rand = new Random();

        AppUser  user = UserTest.createUserTest(rand.nextInt());
        AppUserDto userDto = new AppUserDto(user);

        when(appRoleRepository.findById(any(Long.class))).thenReturn(java.util.Optional.of(user.getRole()));
        when(coachModelTypeRepository.findById(any(Long.class))).thenReturn(java.util.Optional.of(user.getCoachModelType()));

        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post("/appuser/create") 
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(userDto)))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andReturn();
        
        String jsonResponse = result.getResponse().getContentAsString();
        System.out.println("Server response: \n" + JsonUtils.jsonPrettier(jsonResponse));

        userDto = objectMapper.readValue(jsonResponse, AppUserDto.class);
        assertNotNull(userDto);
        assertEquals(user.getName(), userDto.getName());
        assertEquals(user.getEmail(), userDto.getEmail());
        assertEquals(user.getRole().getName(), "user");
    }
}
