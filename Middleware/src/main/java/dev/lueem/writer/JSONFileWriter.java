package dev.lueem.writer;

import jakarta.json.Json;
import jakarta.json.JsonObject;

import java.io.FileWriter;
import java.io.IOException;

public class JSONFileWriter {

    /**
     * Writes the given JSON object to a file with the specified name.
     *
     * @param fileName   the name of the file to which the JSON object will be
     *                   written
     * @param jsonObject the JSON object to write
     * @throws IOException if there is an error writing to the file
     */
    public void writeJSONFile(String fileName, JsonObject jsonObject) throws IOException {
        // try (FileWriter newFileWriter = new FileWriter(fileName)) {
        //     Json.createWriter(newFileWriter).writeObject(jsonObject);
        // } catch (IOException e) {
        //     System.err.println("Error writing to file: " + e.getMessage());
        //     throw e; // rethrow the exception so that the caller is aware of it
        // }
    }
}
