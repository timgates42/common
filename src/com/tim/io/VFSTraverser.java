package com.tim.io;

import java.io.*;

public interface VFSTraverser {
    void execute(String path) throws IOException;
}