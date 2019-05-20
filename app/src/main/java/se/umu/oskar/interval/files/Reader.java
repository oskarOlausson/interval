package se.umu.oskar.interval.files;

import android.content.Context;
import android.util.Log;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;

import se.umu.oskar.interval.helpers.Tags;

class Reader {


    static String readFileToString(Context context, String filePath) throws IOException {
        FileInputStream fis;

        try {
            fis = context.openFileInput(filePath);
        } catch (FileNotFoundException e) {
            Log.v(Tags.oskarTag,"Could not find file, this can just be that this is the current time the file has been used");
            return null;
        }

        BufferedReader reader = new BufferedReader(new InputStreamReader(fis));

        StringBuilder sb = new StringBuilder();
        String line;

        while ((line = reader.readLine()) != null) {
            sb.append(line);
            sb.append('\n');
        }

        reader.close();
        fis.close();

        return sb.toString();
    }
}
