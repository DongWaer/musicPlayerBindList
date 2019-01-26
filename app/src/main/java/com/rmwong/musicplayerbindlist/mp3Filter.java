package com.rmwong.musicplayerbindlist;

import java.io.File;
import java.io.FilenameFilter;

/**
 * Created by 子鹏 on 2014/3/29.
 */
public class mp3Filter implements FilenameFilter {
    @Override
    public boolean accept(File file, String name) {
        return (name.endsWith(".mp3"));
    }
}
