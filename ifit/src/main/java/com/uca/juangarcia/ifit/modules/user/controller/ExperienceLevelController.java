package com.uca.juangarcia.ifit.modules.user.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.uca.juangarcia.ifit.modules.user.dto.CreateExperienceLevelDto;
import com.uca.juangarcia.ifit.modules.user.dto.ExperienceLevelDto;
import com.uca.juangarcia.ifit.modules.user.dto.UpdateExperienceLevelDto;
import com.uca.juangarcia.ifit.modules.user.service.ExperienceLevelService;
import com.uca.juangarcia.ifit.shared.exception.ExperienceLevelNotFoundException;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/experience-levels")
@Tag(name = "Experience Levels", description = "Operaciones relacionadas con los niveles de experiencia")
public class ExperienceLevelController {

    @Autowired
    private ExperienceLevelService experienceLevelService;

    @GetMapping
    @Operation(
        summary = "Obtiene todos los niveles de experiencia",
        description = "Obtiene todos los niveles de experiencia"
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Niveles de experiencia obtenidos correctamente"
        ),
        @ApiResponse(
            responseCode = "404",
            description = "No se encontraron niveles de experiencia"
        )
    })
    public ResponseEntity<List<ExperienceLevelDto>> getAll() {
        return ResponseEntity.ok(experienceLevelService.getAll());
    }


    @GetMapping("/{id}")
    @Operation(
        summary = "Obtiene un nivel de experiencia por su ID",
        description = "Obtiene un nivel de experiencia por su ID"
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Nivel de experiencia obtenido correctamente"
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Nivel de experiencia no encontrado"
        )
    })
    public ResponseEntity<ExperienceLevelDto> getById(
        @Parameter(description = "ID del nivel de experiencia", required = true, example = "1")
        @PathVariable Long id
    ) throws  ExperienceLevelNotFoundException {
        return ResponseEntity.ok(experienceLevelService.getById(id));
    }

    @GetMapping("/{name}")
    @Operation(
        summary = "Obtiene un nivel de experiencia por su nombre",
        description = "Obtiene un nivel de experiencia por su nombre"
    )
    public ResponseEntity<ExperienceLevelDto> getByName(
        @Parameter(description = "Nombre del nivel de experiencia", required = true, example = "Beginner")
        @PathVariable String name
    ) throws ExperienceLevelNotFoundException {
        return ResponseEntity.ok(experienceLevelService.getByName(name));
    }
    

    @PostMapping
    @Operation(
        summary = "Crea un nuevo nivel de experiencia",
        description = "Crea un nuevo nivel de experiencia con los datos proporcionados"
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Nivel de experiencia creado correctamente"
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Datos inválidos para crear el nivel de experiencia"
        )
    })
    public ResponseEntity<ExperienceLevelDto> create(
        @Parameter(description = "Nivel de experiencia a crear", required = true)
        @RequestBody CreateExperienceLevelDto createExperienceLevelDto
    ) {
        return ResponseEntity.ok(experienceLevelService.createExperienceLevel(createExperienceLevelDto));
    }

    @DeleteMapping("/{id}")
    @Operation(
        summary = "Elimina un nivel de experiencia por su ID",
        description = "Elimina un nivel de experiencia por su ID"
    )   
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Nivel de experiencia eliminado correctamente"
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Nivel de experiencia no encontrado"
        )
    })
    public ResponseEntity<Void> delete(
        @Parameter(description = "ID del nivel de experiencia", required = true, example = "1")
        @PathVariable Long id
    ) throws ExperienceLevelNotFoundException {
        experienceLevelService.deleteExperienceLevel(id);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{id}")
    @Operation(
        summary = "Actualiza un nivel de experiencia por su ID",
        description = "Actualiza un nivel de experiencia por su ID con los datos proporcionados"
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Nivel de experiencia actualizado correctamente"
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Nivel de experiencia no encontrado"
        )
    })
    public ResponseEntity<ExperienceLevelDto> update(
        @Parameter(description = "ID del nivel de experiencia", required = true, example = "1")
            @PathVariable Long id,
        @Parameter(description = "Datos para actualizar el nivel de experiencia", required = true)
            @RequestBody UpdateExperienceLevelDto updateExperienceLevelDto
    ) throws ExperienceLevelNotFoundException {
        return ResponseEntity.ok(experienceLevelService.updateExperienceLevel(id, updateExperienceLevelDto));
    }

}
