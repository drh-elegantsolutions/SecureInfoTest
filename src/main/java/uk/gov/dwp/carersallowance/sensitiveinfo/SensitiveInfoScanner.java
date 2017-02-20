package uk.gov.dwp.carersallowance.sensitiveinfo;

import java.io.File;
import java.io.IOException;
import java.util.List;

public interface SensitiveInfoScanner {
    public List<SensitiveInformation> scan(File baseDir) throws IOException;
}