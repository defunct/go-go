package com.goodworkalan.mix;

import java.util.LinkedHashMap;
import java.util.Map;

import mix.Mix;

/**
 * Make sense of the command line.
 *
 * @author Alan Gutierrez
 */
public class CommandInterpreter implements Mix.Arguable
{
    public void main(String[] arguments)
    {
        Map<String, String> parameters = new LinkedHashMap<String, String>();
        for (int i = 0, stop = arguments.length; i < stop; i++) {
            System.out.println(arguments[i]);
        }
    }
}
