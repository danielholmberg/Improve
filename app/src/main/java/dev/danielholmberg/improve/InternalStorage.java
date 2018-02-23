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

    public static void createStorage(Context context) throws IOException {
        // Create the Storage file for OnMyMinds
        File onmyminds = new File(context.getFilesDir(), ONMYMINDS_STORAGE_KEY);
        onmyminds.createNewFile(); //If already exist, will do nothing

        // Create the Storage file for Contacts
        File contacts = new File(context.getFilesDir(), CONTACTS_STORAGE_KEY);
        contacts.createNewFile(); //If already exist, will do nothing
    }

    public static void writeObject(Context context, String key, Object object) throws IOException {
        FileOutputStream fos = context.openFileOutput(key, Context.MODE_PRIVATE);
        ObjectOutputStream oos = new ObjectOutputStream(fos);
        oos.writeObject(object);
        oos.close();
        fos.close();
    }

    public static Object readObject(Context context, String key) throws IOException,
            ClassNotFoundException {
        FileInputStream fis = context.openFileInput(key);
        ObjectInputStream ois = new ObjectInputStream(fis);
        Object object = ois.readObject();
        return object;
    }
}
