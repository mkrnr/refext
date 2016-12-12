package de.exciteproject.refext.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;

public class Deserializer {

    public static Object deserialize(File serializedFile) throws IOException, ClassNotFoundException {
        Object object = null;
        ObjectInputStream in = new ObjectInputStream(new FileInputStream(serializedFile));
        object = in.readObject();
        in.close();

        return object;
    }
}
