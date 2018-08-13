package uk.gov.dwp.carersallowance.sensitiveinfo.git;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.FileUtils;

import uk.gov.dwp.carersallowance.utils.Parameters;

/**
 * A single .gitignore or equivalent
 *
 * note: global and project level exclude file entries are relative to git Root,
 * not their own location
 *
 * @author David Hutchinson (drh@elegantsolutions.co.uk) on 24 Feb 2017.
 */
public class GitExcludeContext {

    private static class Rules {
        public Set<File>   files;
        public Set<File>   directories;
        public Set<String> globPatterns;

        public Rules() {
            files = new HashSet<>();
            directories = new HashSet<>();
            globPatterns = new HashSet<>();
        }
    }

    private File    baseDir;            // for global or project this is gitroot, for .gitignore it is the directory the file is in

    private Rules   excludes;
    private Rules   includes;

    public GitExcludeContext(File excludeFile, File baseDir, File gitRoot) throws IOException {
        this.baseDir = baseDir;
        includes = new Rules();
        excludes = new Rules();

        Parameters.validateMandatoryArgs(excludeFile, "excludeFile");
        if(excludeFile.exists()) {
            List<String> lines = FileUtils.readLines(excludeFile, Charset.defaultCharset());
            readGitExcludeFile(lines, gitRoot);
        }
    }

    public GitExcludeContext(List<String> lines, File baseDir, File gitRoot) throws IOException {
        this.baseDir = baseDir;
        includes = new Rules();
        excludes = new Rules();

        readGitExcludeFile(lines, gitRoot);
    }

    /**
     * process an exclude file, this is normally a .gitignore, but can also be a global or project
     * level exclude file
     *
     * @param baseDir
     * @param gitRoot
     * @throws IOException
     */
    private void readGitExcludeFile(List<String> lines, File baseDir) throws IOException {
        if(lines == null) {
            return;
        }

        for(String line: lines) {
            String trimmed = line.trim();
            if(trimmed.equals("")) {    // blank line
                continue;
            }

            if(trimmed.startsWith("#")) {   // not sure if we should be using line for this
                // comment
                continue;
            }

            if(trimmed.startsWith("!")) {
                readNonEmptyLines(includes, baseDir, trimmed);
            }

            readNonEmptyLines(excludes, baseDir, trimmed);
        }
    }

    private void readNonEmptyLines(Rules rules, File baseDir, String trimmed) throws IOException {
        if(trimmed.contains("*") || trimmed.contains("?")) {
            rules.globPatterns.add(trimmed);
        } else {

            // canonicalize the filename to make comparisons easier
            File file = new File(baseDir, trimmed);
            File canonicalFile = file.getCanonicalFile();
            if(file.exists() && file.isDirectory()) {
                rules.directories.add(canonicalFile);
            } else {
                rules.files.add(canonicalFile);
            }
        }
    }

//    TODO, don't think this will work.  include does not work the same way as exclude
    public boolean matchesGitIgnore(File pathname) throws IOException {
        if(pathname == null) {
            return false;
        }

        // files have to be an exact match, .gitignore file paths have already been canonicalized
        File canonical = pathname.getCanonicalFile();
        if(excludes.files.contains(canonical)) {
            return true;
        }

        // directories have to be an exact match, or the stem of the pathname parameter
        if(excludes.directories.contains(canonical)) {
            return true;
        }

        for(File ignoreDir: excludes.directories) {
            String pathnamePath = canonical.getAbsolutePath();
            String ignoreDirPath = ignoreDir.getAbsolutePath();

            if(isParentOf(ignoreDirPath, pathnamePath)) {
                return true;
            }
        }

        // not sure what the most efficient use of PathMatcher is, FileSystems seems to be efficient in this usage
        // but there is no info on PathMatcher
        // might pre-construct the PathMatchers even if only to test them at the read .gitignore stage
        Path pathnamePath = canonical.toPath();
        for(String pattern: excludes.globPatterns) {
            String glob = "glob:" + pattern;
            PathMatcher pathMatcher = FileSystems.getDefault().getPathMatcher(glob);   // might put these in the set, expensive? multi-threaded?
            if(pathMatcher.matches(pathnamePath)) {
                return true;
            }
        }

        // nothing matched
        return false;
    }

    private boolean isParentOf(String parent, String path) {
        if(parent == null || path == null) {
            return false;
        }

        // mac does not have case sensitive paths,
        // windows does but pretends not to and linux definitely does,
        // so were going with case sensitive
        if(path.startsWith(parent) == false) {
            return false;
        }

        // check to make sure that the parent is a valid ancestor of path
        // e.g. file = \parentDir\file
        // and dir   = \parent
        // then file does start with dir, but is not a valid ancestor
        String pathSeperator = System.getProperty("path.separator");
        int expectedPathSeperatorStart = parent.length();
        int expectedPathSeperatorEnd = parent.length() + pathSeperator.length();
        String expectedPathSeperator = path.substring(expectedPathSeperatorStart, Math.min(path.length(), expectedPathSeperatorEnd));
        if(pathSeperator.equals(expectedPathSeperator)) {
            return true;
        }
        return false;

    }
}