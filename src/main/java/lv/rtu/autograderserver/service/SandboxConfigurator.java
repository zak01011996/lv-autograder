package lv.rtu.autograderserver.service;

import lv.rtu.autograderserver.model.SandboxType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

@Service
public class SandboxConfigurator {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final ResourceLoader resourceLoader;
    private final static String TEMPLATES_FOLDER = "classpath:sandbox-templates/%s";

    public SandboxConfigurator(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    public Map<String, String> readInitialFiles(SandboxType type) throws IOException {
        String templateFolderPath = String.format(TEMPLATES_FOLDER, type.name().toLowerCase(Locale.ROOT));
        Resource templateDir = resourceLoader.getResource(templateFolderPath);

        File[] templateFiles = templateDir.getFile().listFiles();
        if (templateFiles == null) {
            throw new RuntimeException("Cannot find template folder in classpath for " + type.name());
        }

        Map<String, String> result = new HashMap<>();
        for (File file : templateFiles) {
            // We can trust it and read all file from resource folder
            String content = new String(Files.readAllBytes(Path.of(file.getPath())));
            result.put(file.getName(), content);
        }

        return result;
    }
}
