package io.github.rmuhamedgaliev.arcana.core.quest;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;

public class GameLoader {
    public static GameData loadGameData(String filePath) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(new File(filePath), GameData.class);
    }
}
