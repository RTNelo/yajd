package me.rotatingticket.yajd.util.zinnia;

import android.content.Context;
import android.support.test.InstrumentationRegistry;

import org.apache.commons.io.IOUtils;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import me.rotatingticket.yajd.R;

import static org.junit.Assert.*;

public class RecognizerTest {

    private static String MODEL_PATH;

    /**
     * Copy the resource to the internal storage.
     * @throws IOException Raised on copy error.
     */
    @BeforeClass
    public static void prepareModel() throws IOException {
        Context context = InstrumentationRegistry.getTargetContext();
        InputStream inputStream = context.getResources().openRawResource(R.raw.handwriting);
        File internalFile = new File(context.getFilesDir(), "kanji.model");
        OutputStream outputStream = new FileOutputStream(internalFile);
        IOUtils.copy(inputStream, outputStream);
        MODEL_PATH = internalFile.getAbsolutePath();
    }

    @Test
    public void classify() {
        String path = MODEL_PATH;

        try (Recognizer recognizer = new Recognizer(path)){
            try (Character character = new Character()) {
                // initialize the character
                character.setHeight(300);
                character.setWidth(300);

                // add the stroke data
                character.beginStroke(56, 76);
                character.endStroke(201, 69);
                character.beginStroke(117, 80);
                character.endStroke(115, 208);
                character.beginStroke(131, 128);
                character.endStroke(172, 147);

                // classify
                try (ResultSet result = recognizer.classify(character, 3)) {
                    // get null if exception raised
                    assertNotNull(result);
                    assertEquals(result.size(), 3);
                    assertEquals(result.value(0), "下");
                    assertEquals(result.score(0), 0.66, 0.1);
                }

                // second classify on same character
                try (ResultSet result = recognizer.classify(character, 3)) {
                    assertNotNull(result);
                }
            }
        }
    }
}