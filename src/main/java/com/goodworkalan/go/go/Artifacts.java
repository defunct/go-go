package com.goodworkalan.go.go;

import static com.goodworkalan.go.go.GoException.ARTIFACT_FILE_IO_EXCEPTION;
import static com.goodworkalan.go.go.GoException.ARTIFACT_FILE_NOT_FOUND;
import static com.goodworkalan.go.go.GoException.INVALID_ARTIFACTS_LINE_START;
import static com.goodworkalan.go.go.GoException.INVALID_EXCLUDE_LINE;
import static com.goodworkalan.go.go.GoException.INVALID_INCLUDE_LINE;
import static com.goodworkalan.go.go.GoException.ARTIFACT_FILE_MISPLACED_EXCLUDE;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import com.goodworkalan.cassandra.Report;

/**
 * A file reader that reads a list of dependencies from a file that 
 * contains a repository name and the dependency files.
 * 
 * @author Alan Gutierrez
 */
public class Artifacts {
    public static List<Include> read(File file) {
        try {
            try {
                return read(new FileReader(file));
            } catch (FileNotFoundException e) {
                throw new GoException(ARTIFACT_FILE_NOT_FOUND, e);
            }
        } catch (GoException e) {
            e.put("file", file);
            throw e;
        }
    }
     
    public static List<Include> read(Reader reader) {
        try {
            Artifact include = null;
            boolean optional = false;
            List<Artifact> excludes = new ArrayList<Artifact>();
            List<Include> includes = new ArrayList<Include>();
            
            Report report = new Report();
            BufferedReader lines = new BufferedReader(reader);
            int lineNumber = 0;
            String line;
            while ((line = lines.readLine()) != null) {
                lineNumber++;
                line = line.trim();
                if (!(line.startsWith("#") || line.startsWith("@") || line.equals(""))) {
                    String[] split = line.split("\\s+");

                    report
                        .mark()
                        .put("line", line)
                        .put("lineNumber", lineNumber)
                        .put("startCharacter", split[0]);
                    
                    if (split[0].length() != 1) {
                        throw new GoException(INVALID_ARTIFACTS_LINE_START, report);
                    }
                    
                    char flag = split[0].charAt(0);
                    switch (split[0].charAt(0)) {
                    case '~':
                    case '+':
                        if (include != null) {
                            includes.add(new Include(optional, include, excludes));
                            excludes.clear();
                        }
                        optional = flag == '~';
                        if (split.length != 2) {
                            throw new GoException(INVALID_INCLUDE_LINE, report);
                        }
                        include = new Artifact(split[1]);
                        break;
                    case '-':
                        if (split.length != 2) {
                            throw new GoException(INVALID_EXCLUDE_LINE, report);
                        }
                        if (include == null) {
                            throw new GoException(ARTIFACT_FILE_MISPLACED_EXCLUDE, report);
                        }
                        excludes.add(new Artifact(split[1]));
                        break;
                    default:
                        throw new GoException(INVALID_ARTIFACTS_LINE_START, report);
                    }
                    report.clear();
                }
            }
            if (include != null) {
                includes.add(new Include(optional, include, excludes));
            }
            return includes;
        } catch (IOException e) {
            throw new GoException(ARTIFACT_FILE_IO_EXCEPTION, e);
        }
    }
}
