package uk.gov.dwp.carersallowance.sensitiveinfo;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.MissingNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
     * Properties that should not be populated
     * @author David Hutchinson (drh@elegantsolutions.co.uk) on 16 Feb 2017.
     *
     * "SensitiveProperties" : {
     *   "path": "src/main/resources/config/application.properties",
     *   "keys": [
     *     "spring.datasource.username",
     *     "spring.datasource.password"
     *   ]
     * }     *
     * e.g.
     */
    public class SensitivePropertiesScanner extends AbstractScanner {
        private String   path;
        private String[] keys;

        public SensitivePropertiesScanner(String path, String...keys) {
            this.path = path;
            this.keys = keys;
        }

        /**
         * expects:
         *
         * {"path": "src/main/resources/config/application.properties",
         *     "keys": [
         *       "spring.datasource.username",
         *       "spring.datasource.password"
         *     ]
         * }
         *
         * @param json
         */
        public SensitivePropertiesScanner(JsonNode rootConfigNode) {
            if(rootConfigNode == null || rootConfigNode instanceof MissingNode) {
                throw new IllegalArgumentException("rootConfigNode cannot be null or empty");
            }
            path = rootConfigNode.at("/path").asText();

            JsonNode keyNode = rootConfigNode.at("/keys");
            if(keyNode instanceof ArrayNode) {
                ArrayNode arrayNode = (ArrayNode)keyNode;
                keys = new String[arrayNode.size()];
                for(int index = 0; index < keys.length; index++) {
                    JsonNode child = arrayNode.get(index);
                    keys[index] = child.asText();
                }
            }
        }

        public String   getPath() { return path; }
        public String[] getKeys() { return keys; }

        public String toJson() throws IOException {
            // Create the node factory that gives us nodes.
            JsonNodeFactory factory = new JsonNodeFactory(false);

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

            // create a json factory to write the treenode as json. for the example
            JsonFactory jsonFactory = new JsonFactory();
            JsonGenerator generator = jsonFactory.createGenerator(outputStream);
            ObjectMapper mapper = new ObjectMapper();

            // the root node - album
            ObjectNode jsonRoot = factory.objectNode();
            ObjectNode checksNode = factory.objectNode();
            jsonRoot.set(this.getClass().getSimpleName(), checksNode);
            checksNode.put("path", path);
            ArrayNode links = checksNode.putArray("keys");
            for(String key: keys) {
                links.add(key);
            }

            mapper.writeTree(generator, jsonRoot);

            String result = outputStream.toString();
            return result;
        }

        /**
         * expects:
         *
         * "SensitiveProperties" : {
         *     "path": "src/main/resources/config/application.properties",
         *     "keys": [
         *       "spring.datasource.username",
         *       "spring.datasource.password"
         *     ]
         *   }
         *
         * @param json
         */
        public static SensitivePropertiesScanner fromJson(String json) throws JsonParseException, IOException {
            // create a json factory to write the treenode as json. for the example
            JsonFactory jsonFactory = new JsonFactory();
            JsonParser parser = jsonFactory.createParser(json); // stream parser

            // general method, same as with data binding
            ObjectMapper mapper = new ObjectMapper();
            JsonNode rootNode = mapper.readTree(parser);
            JsonNode rootConfigNode = rootNode.at("/" + SensitivePropertiesScanner.class.getSimpleName());
            if(rootConfigNode instanceof MissingNode) {
                throw new JsonParseException(null, "Invalid configuration");
            }

            SensitivePropertiesScanner instance = new SensitivePropertiesScanner(rootConfigNode);
            return instance;
        }

        public boolean equals(SensitivePropertiesScanner other) {
            if(other == this) {
                return true;
            }

            if(other == null) {
                return false;
            }

            if(equals(this.path, other.path) == false) {
                return false;
            }

            if(Arrays.equals(this.keys, other.keys)) {
                return true;
            }

            return false;
        }

        private boolean equals(String lhs, String rhs) {
            if(lhs == rhs) {
                return true;
            }
            if(lhs == null) {
                return false;
            }
            return lhs.equals(rhs);
        }

        public List<SensitiveInformation> scan(File baseDir) throws IOException {
            if(baseDir == null) {
                return null;
            }

            List<SensitiveInformation> problems = new ArrayList<>();

            File propertiesFile = new File(baseDir, path);
            if(propertiesFile.exists() == false) {
                return problems;
            }

            Properties properties = new Properties();
            FileInputStream inputStream = null;
            try {
                inputStream = new FileInputStream(propertiesFile);
                properties.load(inputStream);
                for(String key: keys) {
                    String value = properties.getProperty(key);
                    if(value != null && value.trim().equals("") == false) {
                        SensitiveInformation problem = new SensitiveInformation("Sensitive information in file(" + path + ") at key: " + key);
                        problems.add(problem);
                    }
                }
            } finally {
//                    IOUtils.closeQuietly(inputStream);
                inputStream.close();
            }
            return problems;
        }


        public String toString() {
            StringBuffer buffer = new StringBuffer();

            buffer.append(this.getClass().getName()).append("@").append(System.identityHashCode(this));
            buffer.append("=[");
            buffer.append("path = ").append(path);
            buffer.append(", keys = ").append(keys == null ? null : Arrays.asList(keys));
            buffer.append("]");

            return buffer.toString();
        }

        public static void main(String[] args) throws IOException {
            SensitivePropertiesScanner properties = new SensitivePropertiesScanner("src/main/resources/config/application.properties",
                                                                                   "spring.datasource.username",
                                                                                   "spring.datasource.password");
            String json = properties.toJson();


            System.out.println(json);
            SensitivePropertiesScanner newProperties = SensitivePropertiesScanner.fromJson(json);
            boolean equals = properties.equals(newProperties);
            System.out.println("equals = " + equals);
        }
    }