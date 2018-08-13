package uk.gov.dwp.carersallowance.sensitiveinfo.git;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * Models git config files (which are more complicated than they really need to be)
 *
 * This is a read only implementation that does not preserve property order, comments
 * or whitespace.
 *
 * [from https://git-scm.com/docs/git-config]
 * Syntax
 * The syntax is fairly flexible and permissive; whitespaces are mostly ignored.
 * The # and ; characters begin comments to the end of line, blank lines are ignored.
 *
 * The file consists of sections and variables. A section begins with the name of the
 * section in square brackets and continues until the next section begins. Section names
 * are case-insensitive. Only alphanumeric characters, - and . are allowed in section
 * names. Each variable must belong to some section, which means that there must be a
 * section header before the first setting of a variable.
 *
 * Sections can be further divided into subsections. To begin a subsection put its name
 * in double quotes, separated by space from the section name, in the section header, like
 * in the example below:
 *
 *     [section "subsection"]
 *          key
 *          key = value
 *  e.g.
 *
 *     [http]
 *         sslVerify
 *     [http "https://weak.example.com"]
 *         sslVerify = false
 *
 * @author David Hutchinson (drh@elegantsolutions.co.uk) on 25 Feb 2017.
 */
public class GitConfig {
    private Map<String, String> configValues;

    public GitConfig(File file) throws IOException, ParseException {
        configValues = new HashMap<>();
        init(configValues, file);
    }

    public GitConfig(String config) throws IOException, ParseException {
        configValues = new HashMap<>();
        if(config != null) {
            StringReader reader = new StringReader(config);
            init(configValues, reader);
        }
    }

    public Map<String, String> getProperties() {
        return configValues;
    }

    public String get(String key) {
        return configValues.get(key);
    }

    public boolean containsKey(String key) {
        return configValues.containsKey(key);
    }

    public Set<String> keySet() {
        return configValues.keySet();
    }

    private void init(Map<String, String> properties, Reader reader) throws IOException, ParseException {
        assert reader != null;

        List<String> lines = IOUtils.readLines(reader);
        init(properties, lines);
    }

    private void init(Map<String, String> properties, File file) throws IOException, ParseException {
        if(file == null || file.exists() == false) {
            return;
        }

        List<String> lines = FileUtils.readLines(file, Charset.defaultCharset());
        init(properties, lines);
    }

    private void init(Map<String, String> properties, List<String> lines) throws IOException, ParseException {
        assert properties != null;
        if(lines == null) {
            return;
        }

        String sectionName = null;
        for(String line: lines) {
            if(line == null) {
                continue;
            }

            // empty line or comments only
            String trimmed = trimComments(line);
            trimmed = trimmed.trim();   // and whitespace
            if(StringUtils.isBlank(trimmed)) {
                continue;
            }

            // new section
            if(trimmed.startsWith("[") && trimmed.endsWith("]")) {
                String rawSectionName = trimmed.substring(1,  trimmed.length() - 1);
                sectionName = parseSectionName(rawSectionName);
                continue;
            }

            // value
            parseKeyValue(properties, sectionName, trimmed);
        }
    }

    private void parseKeyValue(Map<String, String> properties, String sectionName, String line) {
        assert properties != null;
        assert line != null;

        if(StringUtils.isEmpty(sectionName)) {
            sectionName = "";
        } else {
            sectionName = sectionName + ".";
        }

        int equalsPos = line.indexOf('=');
        if(equalsPos < 0) {
            String key = sectionName + line.trim();
            properties.put(key, null);
        } else {
            String key = line.substring(0,  equalsPos);
            key = sectionName + key.trim();
            String value = line.substring(equalsPos + 1);
            value = value.trim();
            properties.put(key, value);
        }
    }

    private String parseSectionName(String rawSectionName) throws ParseException {
        assert rawSectionName != null;

        // section start
        String subsection = null;
        String sectionName = rawSectionName;
        int startSubSection = sectionName.indexOf('"');
        if(startSubSection < 0) {
            return sectionName.trim();
        }

        int endSubSection = sectionName.indexOf('"', startSubSection + 1);
        if(endSubSection < 0) {
            throw new ParseException("Subsection start found, but no end in section: [" + sectionName + "]", -1);
        }

        subsection = sectionName.substring(startSubSection + 1, endSubSection);
        subsection = subsection.trim();
        sectionName = sectionName.substring(0, startSubSection);
        sectionName = sectionName.trim();

        if(subsection.equals("")) {
            return sectionName;
        }

        String combinedSectionName = sectionName + "." + subsection;
        return combinedSectionName;
    }

    private String trimComments(String line) {
        if(line == null) {
            return null;
        }

        String result = line;
        int hashPos = line.indexOf('#');
        if(hashPos > -1) {
            // remove the comments from the end of the line
            line = result.substring(0, hashPos);
        }

        int semicolonPos = line.indexOf(';');
        if(semicolonPos > -1) {
            // remove the comments from the end of the line
            line = result.substring(0, semicolonPos);
        }

        return line;
    }
}
