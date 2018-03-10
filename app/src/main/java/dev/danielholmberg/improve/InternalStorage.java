package dev.danielholmberg.improve;

import android.content.Context;
import android.util.Log;

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
    private static final String TAG = InternalStorage.class.getSimpleName();
    private static final String ONMYMINDS_STORAGE_KEY = "onmyminds";
    private static final String CONTACTS_STORAGE_KEY = "contacts";

    public static File onmyminds;
    public static File contacts;

    private InternalStorage() {}

    /**
     * Creates a folder for the User and adds two files to that folder.
     * @param context
     * @param userId - Id of the User.
     * @throws IOException
     */
    public static void createStorage(Context context, String userId) throws IOException {
        // Create a directory specified to the User with userId.
        File userDir = new File(context.getFilesDir(), userId);
        userDir.mkdir();

        // Create the File to store OnMyMinds.
        onmyminds = new File(userDir, ONMYMINDS_STORAGE_KEY);
        onmyminds.createNewFile(); //If File already exist, will do nothing
        Log.d(TAG, "*** Created OnMyMind-file at: " + onmyminds.getAbsolutePath());

        // Create the File to store Contacts.
        contacts = new File(userDir, CONTACTS_STORAGE_KEY);
        contacts.createNewFile(); //If File already exist, will do nothing
        Log.d(TAG, "*** Created Contact-file at: " + contacts.getAbsolutePath());
    }

    /**
     * Writes the incoming Object to the File in Internal Storage with the name "key".
     * @param file
     * @param object - The Object to write to the File.
     * @throws IOException
     */
    public static void writeObject(File file, Object object) throws IOException {
        FileOutputStream fos = new FileOutputStream(file);
        ObjectOutputStream oos = new ObjectOutputStream(fos);
        oos.writeObject(object);
        oos.close();
        fos.close();
        Log.d(TAG, "*** Wrote to FileName: " + file.getPath() + " ***");
    }

    /**
     * Reads the Object stored in the File with the name "key" in Internal Storage.
     * @param file
     * @return - Returns the stored Object from the File.
     * @throws IOException
     * @throws ClassNotFoundException
     */
    public static Object readObject(File file) throws IOException,
            ClassNotFoundException {
        FileInputStream fis = new FileInputStream(file);
        ObjectInputStream ois = new ObjectInputStream(fis);
        Object object = ois.readObject();
        ois.close();
        fis.close();
        Log.d(TAG,"*** Read from FileName: " + file.getPath() + " ***");
        return object;
    }
}
