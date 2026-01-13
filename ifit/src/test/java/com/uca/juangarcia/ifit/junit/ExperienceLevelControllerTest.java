package com.uca.juangarcia.ifit.junit;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.when;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.uca.juangarcia.ifit.helpers.JsonUtils;
import com.uca.juangarcia.ifit.modules.user.controller.ExperienceLevelController;
import com.uca.juangarcia.ifit.modules.user.model.ExperienceLevel;
import com.uca.juangarcia.ifit.modules.user.repository.ExperienceLevelRepository;

@WebMvcTest(ExperienceLevelController.class)
public class ExperienceLevelControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ExperienceLevelRepository experienceLevelRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void findAll() throws Exception {
        System.out.println("Starting findAll test: ");
        List<ExperienceLevel> experienceLevels = List.of(
                new ExperienceLevel("Principiante", "Cliente principiante en el mundo fitness"),
                new ExperienceLevel("Intermedio", "Cliente intermedio en el mundo fitness"),
                new ExperienceLevel("Experto", "Cliente experto en el mundo fitness"));


        when(experienceLevelRepository.findAll()).thenReturn(experienceLevels);
        MvcResult response = mockMvc.perform(MockMvcRequestBuilders.get("/experiencelevel/findAll"))
                .andExpect(MockMvcResultMatchers.status().isOk()).andReturn();

        String jsonResponse = response.getResponse().getContentAsString();
        System.out.println("Server response: ");
        System.out.println(JsonUtils.jsonPrettier(jsonResponse));

        List<ExperienceLevel> newExperienceLevels = (List<ExperienceLevel>) objectMapper.readValue(jsonResponse, new TypeReference<List<ExperienceLevel>>() {});
        assertNotNull(newExperienceLevels);
        assertEquals(experienceLevels.get(0).getName(), newExperienceLevels.get(0).getName());
        assertEquals(experienceLevels.get(2).getName(), newExperienceLevels.get(2).getName());
    }
}
