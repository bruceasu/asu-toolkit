package org.nutz.castor.castor;

import org.nutz.lang.util.Files;

import java.io.File;
import org.nutz.castor.Castor;
import org.nutz.castor.FailToCastObjectException;


public class String2File extends Castor<String, File> {

    @Override
    public File cast(String src, Class<?> toType, String... args) throws FailToCastObjectException {
        return Files.findFile(src);
    }

}
