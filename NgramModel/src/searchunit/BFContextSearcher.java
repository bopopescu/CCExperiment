package searchunit;

import engine.CCRunEngine;
import tokenunit.Tokensequence;

import java.util.*;

public class BFContextSearcher implements ContextSearcher {
    CCRunEngine CCEngine;    //code completion engine
    double stretchLowFactor;   //lower bound of length stretch of sequence
    double stretchHighFactor;  //high bound of length stretch of sequence

    /**
     * Construct an object of BFContextSearcher(context searcher based on BFS)
     * @param pCCEngine Code Completion Engine
     */
    public BFContextSearcher(CCRunEngine pCCEngine) {
        this.CCEngine = pCCEngine;
    }

    /**
     * Retrieve the similar sequences compared to a given sequence
     * @param seq sequence input
     * @return similar sequences
     */
    public ArrayList<Tokensequence> getSimilarSequences(Tokensequence seq) {
        ArrayList<Tokensequence> similarSequenceList = new ArrayList<>();
        ArrayList<String> ls = seq.getSequence();
        HashSet<ArrayList<String>> listSet = new HashSet<>();
        int maxDepth = seq.getSequence().size();

        for (int i = 0; i < ls.size(); i++) {
            String elem = ls.get(i);
            ArrayList<String> singleElemList  = new ArrayList<>();
            singleElemList.add(elem);
            listSet.add(singleElemList);

            int depth = 1;

            while (depth < maxDepth - i) {
                HashSet<ArrayList<String>> newListSet = new HashSet<>();
                for (ArrayList<String> list : listSet) {
                    ArrayList<String> oplist = CCEngine.completePostToken(new Tokensequence(list));
                    if (oplist.size() == 0) {
                        continue;
                    }

                    ArrayList<String> sortedCandidatesList = getSearchSelectedCandidates(seq, list, oplist);
                    for (int j = 0; j < Math.min(2, sortedCandidatesList.size()); j++) {
                        ArrayList<String> newList = (ArrayList<String>)list.clone();
                        newList.add(sortedCandidatesList.get(j));
                        newListSet.add(newList);
                    }
                }
                listSet = newListSet;
                depth++;
            }

            for (ArrayList<String> l : listSet) {
                similarSequenceList.add(new Tokensequence(l));
            }
            listSet.clear();
        }

        //Sort
        HashMap<Tokensequence, Double> seqToSimilarityMap = new HashMap<>();
        for (int i = 0; i < similarSequenceList.size(); i++) {
            double similarity = calculateSequenceSimilarity(similarSequenceList.get(i), seq);
            seqToSimilarityMap.put(similarSequenceList.get(i), new Double(similarity));
        }

        similarSequenceList.clear();
        Set<Map.Entry<Tokensequence, Double>> seqToSimilaritySet = seqToSimilarityMap.entrySet();

        while(!seqToSimilaritySet.isEmpty()) {
            double maxSimilarity = 0.0;
            int largestIndex = -1;
            Tokensequence closestSequence = null;
            Map.Entry<Tokensequence, Double> recordEntry = null;

            for (Map.Entry<Tokensequence, Double> entry : seqToSimilaritySet) {
                if (entry.getValue() > maxSimilarity) {
                    recordEntry = entry;
                    maxSimilarity = entry.getValue();
                    largestIndex = calculateLastEqualTokenPosition(entry.getKey(), seq);
                } else if (entry.getValue() == maxSimilarity) {
                    if (calculateSequenceSimilarity(entry.getKey(), seq) > largestIndex) {
                        recordEntry = entry;
                        largestIndex = calculateLastEqualTokenPosition(entry.getKey(), seq);
                    }
                }
            }
            similarSequenceList.add(recordEntry.getKey());
            seqToSimilaritySet.remove(recordEntry);
        }

        return similarSequenceList;
    }

    /**
     * Sort the token list in fuzzy search
     * @param seq pattern sequence
     * @param list prefix token list
     * @param nextTokenList the list of next tokens without sort
     * @return sorted list based on probability
     */
    public ArrayList<String> getSearchSelectedCandidates(Tokensequence seq, ArrayList<String> list, ArrayList<String> nextTokenList) {
        HashMap<String, Double> map = new HashMap<>();
        for (int j = 0; j < nextTokenList.size(); j++) {
            ArrayList<String> newList = (ArrayList<String>) list.clone();
            newList.add(nextTokenList.get(j));
            double similarity = calculateSequenceSimilarity(new Tokensequence(newList), seq);
            map.put(nextTokenList.get(j), new Double(similarity));
        }

        ArrayList<String> sortedCandidatesList = new ArrayList<>();
        Set<Map.Entry<String, Double>> elemToSimilaritySet = map.entrySet();

        while(!elemToSimilaritySet.isEmpty()) {
            double maxSimilarity = 0.0;
            String elem = null;
            Map.Entry<String, Double> recordEntry = null;
            for (Map.Entry<String, Double> entry : elemToSimilaritySet) {
                if (entry.getValue() > maxSimilarity) {
                    recordEntry = entry;
                    maxSimilarity = entry.getValue();
                }
            }
            sortedCandidatesList.add(recordEntry.getKey());
            elemToSimilaritySet.remove(recordEntry);
        }

        return sortedCandidatesList;
    }
    /**
     * Calculate the similarity between two sequences
     * @param seq1 sequence 1
     * @param seq2 sequence 2
     * @return the similarity between seq1 and seq2
     */
    public double calculateSequenceSimilarity(Tokensequence seq1, Tokensequence seq2) {
        double similarity = 1.0;
        ArrayList<String> list1 = seq1.getSequence();
        ArrayList<String> list2 = seq2.getSequence();
        int len1 = list1.size();
        int len2 = list2.size();

        if (len1 == 0 || len2 == 0) {
            return 0;
        }

        int[][] dp = new int[len1][len2];

        dp[0][0] = (list1.get(0) == list2.get(0)) ? 1 : 0;
        for (int i = 1; i < len1; i++) {
            dp[i][0] = (list1.get(i) == list2.get(0)) ? 1 : dp[i - 1][0];
        }
        for (int i = 1; i < len2; i++) {
            dp[0][i] = (list1.get(0) == list2.get(i)) ? 1: dp[0][i - 1];
        }
        for (int i = 1; i < len1; i++) {
            for (int j = 1; j < len2; j++) {
                if (list1.get(i).equals(list2.get(j))) {
                    dp[i][j] = dp[i - 1][j - 1] + 1;
                } else {
                    dp[i][j] = Math.max(dp[i][j - 1], dp[i - 1][j]);
                }
            }
        }

        int commonSubsequenceLength = dp[len1 - 1][len2 - 1];
        similarity = similarity * commonSubsequenceLength / len1;
        similarity = similarity * commonSubsequenceLength / len2;
        return similarity;
    }

    /**
     * Calculate the largest index of longest common sub sequence in patternseq
     * @param seq sequence in corpus
     * @param patternseq input sequence
     * @return the largest index of longest common sub sequence in patternseq
     */
    private int calculateLastEqualTokenPosition(Tokensequence seq, Tokensequence patternseq) {
        int len1 = seq.length();
        int len2 = patternseq.length();

        for (int i = len1 - 1; i >= 0; i--) {
            for (int j = len2 - 1; j >= 0; j--) {
                if (seq.getSequence().get(i) == patternseq.getSequence().get(j)) {
                    return j;
                }
            }
        }

        return -1;
    }
}
