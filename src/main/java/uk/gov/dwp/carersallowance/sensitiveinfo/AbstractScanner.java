package uk.gov.dwp.carersallowance.sensitiveinfo;

import java.io.File;
import java.io.IOException;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;

public abstract class AbstractScanner implements SensitiveInfoScanner {
    public AbstractScanner() {
    }

    public AbstractScanner(JsonNode configNode) {
    }

    public abstract List<SensitiveInformation> scan(File baseDir) throws IOException;
}