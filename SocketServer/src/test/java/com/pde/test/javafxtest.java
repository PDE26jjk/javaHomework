package com.pde.test;

import javafx.beans.property.SimpleIntegerProperty;
import org.junit.Test;

public class javafxtest {

    @Test
    public void test1(){
        SimpleIntegerProperty a = new SimpleIntegerProperty(2);
        SimpleIntegerProperty b = new SimpleIntegerProperty(2);

        a.bindBidirectional(b);

        a.set(0);
        System.out.println(b.get());

    }
}
