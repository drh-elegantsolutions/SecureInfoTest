package uk.gov.dwp.carersallowance.sensitiveinfo;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;

public class CompositeScanner extends AbstractScanner {
    private List<SensitiveInfoScanner> list;

    public CompositeScanner(Collection<SensitiveInfoScanner> collection) {
        if(collection == null) {
            list = new ArrayList<>();
        } else {
            list = new ArrayList<>(collection);
        }
    }

    public CompositeScanner(JsonNode configNode) {
        throw new UnsupportedOperationException("AbstractScanner(JsonNode configNode)");
    }

    @Override
    public List<SensitiveInformation> scan(File baseDir) throws IOException {
        List<SensitiveInformation> results = new ArrayList<>();

        for(SensitiveInfoScanner check: list) {
            List<SensitiveInformation> subResults = check.scan(baseDir);
            results.addAll(subResults);
        }
        return results;
    }
}