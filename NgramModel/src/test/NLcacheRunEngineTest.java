package test;

import engine.CacheRunEngine;

import java.io.File;
import java.util.ArrayList;

public class NLcacheRunEngineTest {
    public static void main(String[] args) {
        File currentFile = new File("corpus\\natural_language_dataset3\\writing1.txt");
        CacheRunEngine runtest = new CacheRunEngine(0, 3, 1000, currentFile);
        runtest.run();
        ArrayList<String> ls = runtest.completePostToken();

        if (ls.size() != 0) {
            for (int i = 0; i < ls.size(); i++) {
                System.out.println(ls.get(i));
            }
        } else {
            System.out.println("miss value");
        }
    }
}
