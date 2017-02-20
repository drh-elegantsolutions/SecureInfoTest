package uk.gov.dwp.carersallowance.sensitiveinfo;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ScannerFactory {

    public static SensitiveInfoScanner createScanner(File configFile) throws JsonProcessingException, IOException {
        if(configFile == null) {
            return null;
        }

        // create a json factory to write the treenode as json. for the example
        JsonFactory jsonFactory = new JsonFactory();
        JsonParser parser = jsonFactory.createParser(configFile); // stream parser

        List<SensitiveInfoScanner> scannerList = new ArrayList<>();

        // general method, same as with data binding
        ObjectMapper mapper = new ObjectMapper();
        JsonNode rootNode = mapper.readTree(parser);
        Iterator<String> scannerNames = rootNode.fieldNames();
        while(scannerNames.hasNext()) {
            String scannerName = scannerNames.next();
            JsonNode configNode = rootNode.at("/" + scannerName);

            switch(scannerName) {
                case "CompositeScanner": {
                    SensitiveInfoScanner scanner = new CompositeScanner(configNode);
                    scannerList.add(scanner);
                    break;
                }
                case "SensitivePropertiesScanner": {
                    SensitiveInfoScanner scanner = new SensitivePropertiesScanner(configNode);
                    scannerList.add(scanner);
                    break;
                }
                default:
                    throw new JsonParseException(null, "Unknown scanner: " + scannerName);
            }
        }

        if(scannerList.isEmpty()) {
            return null;
        }

        if(scannerList.size() == 1) {
            return scannerList.get(0);
        }

        SensitiveInfoScanner scanner = new CompositeScanner(scannerList);
        return scanner;
    }

    public static SensitivePropertiesScanner fromJson(String json) {
        if(json == null) {
            return null;
        }
        return null;
    }

    public static void main(String[] args) throws JsonProcessingException, IOException {
        System.out.println("Searching for sensitive information");
        boolean sensitiveInfoPresent = false;
        for(String filename: args) {
            File configFile = new File(filename);
            if(configFile.exists() == false) {
                System.err.println("Unable to locate configuration file: " + configFile.getAbsolutePath());
                System.exit(2);
            }

            if(configFile.canRead() == false) {
                System.err.println("Unable to read file: " + configFile.getAbsolutePath());
                System.exit(3);
            }

            try {
                File baseDir = configFile.getParentFile();
                SensitiveInfoScanner scanner = ScannerFactory.createScanner(configFile);
                List<SensitiveInformation> problems = scanner.scan(baseDir);
                if(problems.isEmpty() == false) {
                    sensitiveInfoPresent = true;
                    System.out.println("Config: " + filename);
                    for(SensitiveInformation problem: problems) {
                        System.out.println("\t" + problem.getMessage());
                    }
                }
            } catch(IOException e) {
                System.err.println("Problems executing configuration: " + configFile);
                e.printStackTrace();
            }
        }

        if(sensitiveInfoPresent) {
            System.exit(1);
        }

        System.out.println("No sensitive information found");
    }
}