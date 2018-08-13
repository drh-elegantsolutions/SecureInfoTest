package uk.gov.dwp.carersallowance.sensitiveinfo.git;

import java.io.IOException;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;

import uk.gov.dwp.carersallowance.utils.ReflectionUtils;

public class GitConfigTest {

    @Test
    public void testInit() throws IOException, ParseException {

        String config =
                "  " + "\n"
              + "# comment line" + "\n"
              + "firstKey " + "\n"
              + "  secondKey=" + "\n"
              + "thirdKey = " + "\n"
              + "fourthKey = fourthKeyValue  " + "\n"
              + "[ sectionOne ]" + "\n"
              + "firstKey " + "\n"
              + "  secondKey=" + "\n"
              + " [sectionTwo \"sectionTwoSubsection\"]" + "\n"
              + "[ sectionOne ]" + "\n"
              + "thirdKey = " + "\n"
              + "fourthKey = fourthKeyValue  " + "\n"
              + "[ sectionOne \"sectionOne Subsection\"]" + "\n"
              + "firstKey " + "\n"
              + "  secondKey=" + "\n"
              + "thirdKey = " + "\n"
              + "fourthKey = fourthKeyValue  " + "\n"
              + "[]" + "\n"
              + "penultimateKey " + "\n"
              + "lastKey=lastKeyValue" + "\n";

        String[] expectedKeys = {
                "firstKey",
                "secondKey",
                "thirdKey",
                "fourthKey",
                "sectionOne.firstKey",
                "sectionOne.secondKey",
                "sectionOne.thirdKey",
                "sectionOne.fourthKey",
                "sectionOne.sectionOne Subsection.firstKey",
                "sectionOne.sectionOne Subsection.secondKey",
                "sectionOne.sectionOne Subsection.thirdKey",
                "sectionOne.sectionOne Subsection.fourthKey",
                "penultimateKey",
                "lastKey"
        };

        String[] expectedValues = {
                null,
                "",
                "",
                "fourthKeyValue",
                null,
                "",
                "",
                "fourthKeyValue",
                null,
                "",
                "",
                "fourthKeyValue",
                null,
                "lastKeyValue"
        };

        GitConfig gitConfig = new GitConfig(config);
        Map<String, String> properties = gitConfig.getProperties();

        Set<String> expectedKeySet = new HashSet<>(Arrays.asList(expectedKeys));
        Assert.assertEquals(expectedKeySet, properties.keySet());

        for(int index = 0; index < expectedKeys.length; index++) {
            String expectedKey = expectedKeys[index];
            String expectedValue = expectedValues[index];

            Assert.assertTrue("'" + expectedKey + "' not found", properties.containsKey(expectedKey));
            Assert.assertEquals("'" + expectedKey + "' value incorrect: ", expectedValue, properties.get(expectedKey));
        }
    }

    @Test
    public void testParseKeyValueNoSection() {
        String[] lines = {"", " key ", " key=value ", "key =value", "key= value"};
        String[] expectedKeys = {"", "key", "key", "key", "key"};
        String[] expectedValues = {null, null, "value", "value", "value"};

        for(int index = 0; index < lines.length; index++) {
            String line = lines[index];
            Map<String, String> properties = new HashMap<>();
            String sectionName = null;

            try {
                parseKeyValue(properties, sectionName, line);
            } catch (IOException | ParseException e) {
                throw new RuntimeException("Exception parsing '" + line + "'", e);
            }

            if(expectedKeys[index] == null) {
                Assert.assertEquals(Collections.EMPTY_SET, properties.keySet());
            } else {
                Set<String> keySet = properties.keySet();
                Assert.assertEquals(1, keySet.size());
                String propertyKey = keySet.iterator().next();
                Assert.assertEquals(expectedKeys[index], propertyKey);
                Assert.assertEquals(expectedValues[index], properties.get(propertyKey));
            }
        }
    }


    @Test
    public void testTrimCommentsNullLine() throws IOException, ParseException {
        String result = trimComments(null);
        Assert.assertEquals(null, result);
    }

    @Test
    public void testTrimCommentsEmptyLines() throws IOException, ParseException {
        String[] emptyLines = {
            "",
            ";comment",
            ";;;;;;comment",
            ";",
            "#",
            "#######;;;; stuff",
            ";;;;##### stuff"
        };

        for(String line: emptyLines) {
            String result = trimComments(line);
            Assert.assertEquals("", result);
        }
    }

    @Test
    public void testTrimCommentsNonEmptyLines() throws IOException, ParseException {
        String[] nonEmptyLines = {
                " ",
                "a",
                "a = b ",
                "a = b; comment# other stuff",
                "a = b# comment; other stuff"
        };

        String[] nonEmptyLinesExpectedResults = {
                " ",
                "a",
                "a = b ",
                "a = b",
                "a = b"
        };

        for(int index = 0; index < nonEmptyLines.length; index++) {
            String line = nonEmptyLines[index];
            String expectedResult = nonEmptyLinesExpectedResults[index];

            String result = trimComments(line);
            Assert.assertEquals(expectedResult, result);
        }
    }

    @Test
    public void testParseSectionName() throws ParseException, IOException {
        String[] nonEmptyLines = {
                "",
                " ",
                "sectionName ",
                " sectionName \"\"",
                "sectionName \"  \"",
                " sectionName \"subsection\"",
                " sectionName \" subsection \"",
                "\"subsection\"",
        };

        String[] nonEmptyLinesExpectedResults = {
                "",
                "",
                "sectionName",
                "sectionName",
                "sectionName",
                "sectionName.subsection",
                "sectionName.subsection",
                ".subsection",
        };

        for(int index = 0; index < nonEmptyLines.length; index++) {
            String line = nonEmptyLines[index];
            String expectedResult = nonEmptyLinesExpectedResults[index];

            String result = parseSectionName(line);
            Assert.assertEquals("processing '" + line + "'", expectedResult, result);
        }
    }

    private void parseKeyValue(Map<String, String> properties, String sectionName, String line) throws IOException, ParseException {
        ReflectionUtils.callPrivateMethod(new GitConfig((String)null), "parseKeyValue", new Class[]{Map.class, String.class, String.class}, new Object[]{properties, sectionName, line}, null);
    }

    private String parseSectionName(String string) throws IOException, ParseException {
        return (String)ReflectionUtils.callPrivateMethod(new GitConfig((String)null), "parseSectionName", new Class[]{String.class}, new Object[]{string}, null);
    }

    private String trimComments(String string) throws IOException, ParseException {
        return (String)ReflectionUtils.callPrivateMethod(new GitConfig((String)null), "trimComments", new Class[]{String.class}, new Object[]{string}, null);
    }

    public static void main(String[] args) throws IOException, ParseException {
        GitConfigTest test = new GitConfigTest();
        String result = test.trimComments("test; comment");
        System.out.println("result = " + result);
    }
}
