package me.rotatingticket.yajd.util;

import android.content.Context;
import android.support.annotation.NonNull;

import com.swabunga.spell.engine.GenericSpellDictionary;
import com.swabunga.spell.event.SpellChecker;

import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import me.rotatingticket.yajd.R;

/**
 * SpellChecker singleton manager.
 */
public class SpellCheckerManager {
    private static SpellChecker spellChecker;
    private static final String DICT_FILE_NAME = "romaji.txt";

    public static synchronized SpellChecker getInstance(Context context) {
        if (spellChecker == null) {
            try {
                spellChecker = new SpellChecker(new GenericSpellDictionary(prepareDictFile(context)));
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }
        return spellChecker;
    }

    /**
     * Prepare dict file for the spell checker using raw resources.
     * @param context The context to get the resources.
     * @return The dict file object.
     * @throws IOException raised on IO Error on file copy.
     */
    private static File prepareDictFile(@NonNull Context context) throws IOException {
        File file = new File(context.getFilesDir(), DICT_FILE_NAME);
        if (file.exists()) {
            return file;
        }
        initializeDictFile(context, file);
        return file;
    }

    /**
     * Copy dict file of spell checker from raw resource to internal storage.
     * @param context The context to get  resource.
     * @param file The output file to write.
     * @throws IOException raised on IO Error.
     */
    private static void initializeDictFile(@NonNull Context context, @NonNull File file) throws IOException {
        InputStream inputStream = context.getResources().openRawResource(R.raw.romaji);
        OutputStream outputStream = new FileOutputStream(file);
        IOUtils.copy(inputStream, outputStream);
    }
}
