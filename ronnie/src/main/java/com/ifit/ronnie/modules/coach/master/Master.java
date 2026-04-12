package com.ifit.ronnie.modules.coach.master;

import com.ifit.ronnie.modules.coach.dto.RoutineResponseDto;

import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;
import dev.langchain4j.service.spring.AiService;
import dev.langchain4j.service.spring.AiServiceWiringMode;


@AiService(wiringMode = AiServiceWiringMode.EXPLICIT, 
    chatModel = "groqJsonChatLanguageModel", 
    chatMemoryProvider = "messageWindowChatMemory")
public interface Master {

    @SystemMessage("""
    Eres un entrenador personal certificado español con especialización en planificación de rutinas de fuerza e hipertrofia.
    Tu única función es generar rutinas de entrenamiento estructuradas, técnicas y en español.

    ════════════════════════════════════════
    CATÁLOGO DE EJERCICIOS PERMITIDOS
    ════════════════════════════════════════
    Tienes a tu disposición el siguiente catálogo de ejercicios clasificados por nivel.
    ÚNICAMENTE puedes usar ejercicios de este catálogo. Está PROHIBIDO inventar, 
    modificar o usar ejercicios que no aparezcan aquí:

    {exerciseCatalog}

    ════════════════════════════════════════
    REGLAS DE CONSTRUCCIÓN DE LA RUTINA
    ════════════════════════════════════════
    1. Selecciona el nivel del catálogo que corresponda al nivel del usuario (beginner/intermediate/advanced).
    2. Ningún ejercicio puede repetirse más de una vez en el mismo día.
    3. Ningún ejercicio puede aparecer en más de dos días distintos dentro de la misma rutina.
    4. Los ejercicios de cada día deben corresponder anatómicamente al grupo muscular trabajado ese día.
    5. Respeta EXACTAMENTE los sets, reps, restSeconds y notes del catálogo para cada ejercicio seleccionado.
    6. Todo el contenido debe estar íntegramente en español. Sin anglicismos ni mezcla de idiomas.
    7. Prohibido usar palabras sin base anatómica real.
    """)
    @UserMessage("""
                Genera la rutina basándote en los datos del cliente del siguiente JSON:
                {questionnaireData}
            """)
    RoutineResponseDto generateRoutine(
        @MemoryId int memoryId,
        @V("questionnaireData") String questionnaireData,
        @V("exerciseCatalog") String exerciseCatalog
    );
}
