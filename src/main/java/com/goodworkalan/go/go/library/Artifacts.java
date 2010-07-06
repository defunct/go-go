package com.goodworkalan.go.go.library;

import static com.goodworkalan.go.go.GoException.ARTIFACT_FILE_IO_EXCEPTION;
import static com.goodworkalan.go.go.GoException.ARTIFACT_FILE_NOT_FOUND;
import static com.goodworkalan.go.go.GoException.INVALID_ARTIFACTS_LINE_START;
import static com.goodworkalan.go.go.GoException.INVALID_INCLUDE;
import static com.goodworkalan.go.go.GoException.INVALID_INCLUDE_LINE;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import com.goodworkalan.go.go.GoException;

/**
 * A file reader that reads a list of dependencies from a file that 
 * contains a repository name and the dependency files.
 * 
 * @author Alan Gutierrez
 */
public class Artifacts {
    // TODO Document.
    public static List<Include> read(File file) {
        try {
            return read(file.toString(), new FileReader(file));
        } catch (FileNotFoundException e) {
            throw new GoException(ARTIFACT_FILE_NOT_FOUND, e, file);
        }
    }
     
    // TODO Document.
    public static List<Include> read(Reader reader) {
        return read("UNKNOWN", reader);
    }
    
    // TODO Document.
    private static List<Include> read(String context, Reader reader) {
        try {
            List<Include> includes = new ArrayList<Include>();
            
            BufferedReader lines = new BufferedReader(reader);
            int lineNumber = 0;
            String line;
            while ((line = lines.readLine()) != null) {
                lineNumber++;
                line = line.trim();
                if (!(line.startsWith("#") || line.startsWith("@") || line.equals(""))) {
                    String[] split = line.split("\\s+");

                    if (split[0].length() != 1) {
                        throw new GoException(INVALID_ARTIFACTS_LINE_START, split[0], lineNumber, context);
                    }
                    
                    switch (split[0].charAt(0)) {
                    case '~':
                    case '!':
                    case '+':
                        if (split.length < 2) {
                            throw new GoException(INVALID_INCLUDE_LINE, lineNumber, context);
                        }
                        Artifact artifact;
                        try {
                            artifact = new Artifact(split[1]);
                        } catch (GoException e) {
                            throw new GoException(INVALID_INCLUDE, lineNumber, context);
                        }
                        List<Exclude> excludes = new ArrayList<Exclude>();
                        for (int i = 2; i < split.length; i++) {
                            excludes.add(new Exclude(split[i]));
                        }
                        includes.add(new Include(artifact, excludes));
                        break;
                    default:
                        throw new GoException(INVALID_ARTIFACTS_LINE_START, split[0], lineNumber, context);
                    }
                }
            }
           return includes;
        } catch (IOException e) {
            throw new GoException(ARTIFACT_FILE_IO_EXCEPTION, e, context);
        }
    }
}
