package com.mygdx.game;

import com.badlogic.gdx.files.FileHandle;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.TreeSet;

public class Funciones {
    public static boolean findWord(FileHandle fileHandle, String targetWord) {

        TreeSet<String> words = new TreeSet<>();

        try (BufferedReader br = new BufferedReader(new InputStreamReader(fileHandle.read()))) {
            String line;
            while ((line = br.readLine()) != null) {
                words.add(line.trim()); // Trim removes leading/trailing whitespace
            }
        } catch (IOException e) {
            System.out.println("ERROR: " + e.getMessage());
        }

        // Now 'words' TreeSet contains all the sorted words
        // Perform binary search
        return words.contains(targetWord);

    }
}