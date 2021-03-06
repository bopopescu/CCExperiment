package test;

import app.NgramRunApp;
import iounit.CorpusImporter;
import tokenunit.Tokensequence;

import java.io.File;
import java.util.ArrayList;

public class NLngramBatchTest {
    public static void main(String[] args) {
        NgramRunApp app = new NgramRunApp(0, 3);
        CorpusImporter corpusImporter = new CorpusImporter(0);
        ArrayList<File> testFileList = corpusImporter.testingDataFileList;

        //test 150 times
        int length = testFileList.size();
        int top3 = 0, top5 = 0, top10 = 0;
        double top3rate, top5rate, top10rate;
        double MRR = 0.0;
        int i;

        for (i = 0; i < length; i++) {
            System.out.print("Count: ");
            System.out.println(i + 1);
            ArrayList<String> ls = new ArrayList<>();
            ArrayList<String> tokenList = corpusImporter.importCorpusFromSingleFile(testFileList.get(i));
            for (int j = Math.max(0, tokenList.size() - 5); j < tokenList.size() - 1; j++) {
                ls.add(tokenList.get(j));
            }

            Tokensequence seq = new Tokensequence(ls);
            String answer = tokenList.get(tokenList.size() - 1);
            ArrayList candidatesList = app.completePostToken(seq);
            System.out.println(seq);
            System.out.print("ANS:");
            System.out.println(answer);
            for (int k = 0; k < Math.min(10, candidatesList.size()); k++) {
                System.out.println(candidatesList.get(k));
            }

            if (tokenList.size() == 0) continue;
            int rank = 0;
            while (rank < candidatesList.size()) {
                if (candidatesList.get(rank).equals(answer)) {
                    if (rank <= 3) {
                        top3++;
                    }
                    if (rank <= 5) {
                        top5++;
                    }
                    if (rank <= 10) {
                        top10++;
                    }
                    MRR += 1.0 / (rank + 1);
                    break;
                }
                rank++;
            }
        }

        MRR /= length;
        top3rate = top3 * 1.0 / length;
        top5rate = top5 * 1.0 / length;
        top10rate = top10 * 1.0 / length;

        System.out.print("top3: ");
        System.out.println(top3rate);

        System.out.print("top5: ");
        System.out.println(top5rate);

        System.out.print("top10: ");
        System.out.println(top10rate);

        System.out.print("MRR: ");
        System.out.println(MRR);
    }
}
