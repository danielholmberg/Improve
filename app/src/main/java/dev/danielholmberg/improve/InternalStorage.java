package dev.danielholmberg.improve;

import android.content.Context;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * Class ${CLASS}
 */

public final class InternalStorage {
    public static final String ONMYMINDS_STORAGE_KEY = "onmyminds";
    public static final String CONTACTS_STORAGE_KEY = "contacts";

    private InternalStorage() {}

    /**
     * Creates two Files to store OnMyMinds and Contacts in Internal Storage.
     * @param context
     * @throws IOException
     */
    public static void createStorage(Context context) throws IOException {
        // Create the File to store OnMyMinds.
        File onmyminds = new File(context.getFilesDir(), ONMYMINDS_STORAGE_KEY);
        onmyminds.createNewFile(); //If File already exist, will do nothing

        // Create the File to store Contacts.
        File contacts = new File(context.getFilesDir(), CONTACTS_STORAGE_KEY);
        contacts.createNewFile(); //If File already exist, will do nothing
    }

    /**
     * Writes the incoming Object to the File in Internal Storage with the name "key".
     * @param context
     * @param key - Name of the File in Internal Storage to write to.
     * @param object - The Object to write to the File.
     * @throws IOException
     */
    public static void writeObject(Context context, String key, Object object) throws IOException {
        FileOutputStream fos = context.openFileOutput(key, Context.MODE_PRIVATE);
        ObjectOutputStream oos = new ObjectOutputStream(fos);
        oos.writeObject(object);
        oos.close();
        fos.close();
    }

    /**
     * Reads the Object stored in the File with the name "key" in Internal Storage.
     * @param context
     * @param key - Name of the File in Internal Storage to read from.
     * @return - Returns the stored Object from the File.
     * @throws IOException
     * @throws ClassNotFoundException
     */
    public static Object readObject(Context context, String key) throws IOException,
            ClassNotFoundException {
        FileInputStream fis = context.openFileInput(key);
        ObjectInputStream ois = new ObjectInputStream(fis);
        Object object = ois.readObject();
        return object;
    }
}
