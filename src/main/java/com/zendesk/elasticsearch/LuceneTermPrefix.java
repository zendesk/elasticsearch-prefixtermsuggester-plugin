package com.zendesk.elasticsearch;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.lucene.index.*;
import org.apache.lucene.search.spell.DirectSpellChecker;
import org.apache.lucene.util.BytesRef;
import org.apache.lucene.util.StringHelper;

import java.io.IOException;
import java.util.*;

public class LuceneTermPrefix {

    private static final Logger logger = LogManager.getLogger(LuceneTermPrefix.class);

    public static List<ScoreTerm> getPrefixTerms(final Term prefix, int size, final IndexReader reader)
            throws IOException {

        // Priority queue of terms in the order of term frequency where the term with lowest frequency is on top
        PriorityQueue<ScoreTerm> pq = new PriorityQueue<>();
        logger.info("prefix:" + prefix.text());
        List<LeafReaderContext> leaves = reader.leaves();
        for (LeafReaderContext leaf : leaves) {
            Terms _terms = leaf.reader().terms(prefix.field());
            if (_terms == null) {
                continue;
            }

            TermsEnum termsEnum = _terms.iterator();

            //seek through the term dictionary until we hit a term with required prefix
            TermsEnum.SeekStatus seekStatus = termsEnum.seekCeil(prefix.bytes());
            if (TermsEnum.SeekStatus.END == seekStatus) {
                continue;
            }

            for (BytesRef term = termsEnum.term(); term != null; term = termsEnum.next()) {

                if (!StringHelper.startsWith(term, prefix.bytes())) {
                    break;
                }
                int docFreq = reader.docFreq(new Term(prefix.field(), term));
                //we only care about the top  `size` number of terms so limit the size of pq accordingly
                if (pq.size() >= size) {
                    if (pq.peek().docfreq <= docFreq) {
                        pq.poll();
                    } else {
                        //ignore terms that are less frequent than current list of top terms.
                        continue;
                    }
                }

                pq.offer(
                        new ScoreTerm(
                                new BytesRef(term.utf8ToString()),
                                docFreq));
            }
        }

        List<ScoreTerm> result  = new ArrayList<>();

        while (!pq.isEmpty() && result.size() < size){
            result.add(pq.poll());
        }
        Collections.reverse(result);
        return result;

    }

    /**
     * Holds a spelling correction for internal usage inside {@link DirectSpellChecker}.
     */
    static class ScoreTerm implements Comparable<ScoreTerm> {

        /**
         * The actual spellcheck correction.
         */
        public BytesRef term;

        /**
         * The df of the spellcheck correction.
         */
        public int docfreq;

        /**
         * Constructor.
         * @param term
         */
        public ScoreTerm(BytesRef term, int docfreq) {
            this.term = term;
            this.docfreq = docfreq;
        }

        @Override
        public int compareTo(ScoreTerm other) {
            if (term.bytesEquals(other.term))
                return 0; // consistent with equals
            if (this.docfreq == other.docfreq)
                return other.term.compareTo(this.term);
            else
                return Integer.compare(this.docfreq, other.docfreq);
        }

        @Override
        public String toString() {
            return "ScoreTerm{" +
                    "term=" + term.utf8ToString() +
                    ", docfreq=" + docfreq +
                    '}';
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ScoreTerm scoreTerm = (ScoreTerm) o;
            return docfreq == scoreTerm.docfreq &&
                    term.equals(scoreTerm.term);
        }

        @Override
        public int hashCode() {
            return Objects.hash(term, docfreq);
        }
    }
}

