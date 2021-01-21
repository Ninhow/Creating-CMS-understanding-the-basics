package cms.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import exceptions.ConfigException;
import org.apache.commons.io.FilenameUtils;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public final class JsonConfig {
    private JsonConfig(){

    }

    public static <T> T loadConfig(String filePath, Class<T> clazz) throws ConfigException {
        Path path = Paths.get(filePath);

        if(!Files.exists(path)){
            throw new ConfigException("File: " + filePath + " does not exist!");
        }

        if(Files.isDirectory(path) || !FilenameUtils.getExtension(filePath).equalsIgnoreCase("json")){
            throw new ConfigException("File: " + filePath + " is not a json file");
        }

        try{
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(path.toFile(), clazz);
        }catch (Exception e){
            throw new ConfigException("There were something wrong parsing the config for " + filePath + ". " + e);

        }
    }
}
