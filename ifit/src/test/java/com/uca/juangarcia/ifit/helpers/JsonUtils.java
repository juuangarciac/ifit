package com.uca.juangarcia.ifit.helpers;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonUtils {
    static public String jsonPrettier(String jsonResponse) throws Exception {
            try {
                ObjectMapper objectMapper = new ObjectMapper();
                Object json = objectMapper.readValue(jsonResponse, Object.class); // parsea a objeto genérico
                String prettyJson;
                prettyJson = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(json);

                return prettyJson;
            } catch(Exception e){
                throw new Exception("Can not prettier jsonResponse. jsonResponse cannot be empty or null");
            }
        }
}
