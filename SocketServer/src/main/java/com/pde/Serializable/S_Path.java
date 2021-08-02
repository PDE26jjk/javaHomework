package com.pde.Serializable;

import java.io.Serializable;
import java.util.List;

public class S_Path implements Serializable {
    public static final long serialVersionUID = 6697971187774L;
    public String name;
    public Long size;
    public Path_Type type;
    public List<S_Path> children;

    public enum Path_Type {
        FILE,
        DIR
    }

    public S_Path(String name, Path_Type type, Long size) {
        this.name = name;
        this.size = size;
        this.type = type;
    }

    @Override
    public String toString() {
        if (type == Path_Type.DIR) {
            return "\n[" + type +
                    ":" + name +
                    ", children=" + children +
                    "]";
        } else {
            return "\n[" + name +
                    ", size=" + size + "]";

        }

    }
}
