package com.zendesk.elasticsearch;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.RandomIndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.store.BaseDirectoryWrapper;
import org.apache.lucene.util.BytesRef;
import org.apache.lucene.util.IOUtils;
import org.apache.lucene.util.LuceneTestCase;

import java.io.IOException;
import java.util.List;


public class LuceneTermPrefixTest  extends LuceneTestCase {


    public void testGetPrefix() throws IOException {

        BaseDirectoryWrapper dir = newDirectory();
        RandomIndexWriter writer = new RandomIndexWriter(random(), dir);

        String[] termsToAdd = {
                "z1_torch", "z1_torch_open", "z1_torch_resolved", "z1_torch_ready", "z1_torch_rejected",
                "z1_torch", "z1_torch_open", "z1_torch_resolved",
                "z1_torch", "z1_torch_open",
                "z1_torch",
        };

        for (int i = 0; i < termsToAdd.length; i++) {
            Document doc = new Document();
            doc.add(newTextField("tags", termsToAdd[i], Field.Store.NO));
            writer.addDocument(doc);
        }

        IndexReader ir = writer.getReader();
        List<LuceneTermPrefix.ScoreTerm> suggestions = LuceneTermPrefix.getPrefixTerms(
                new Term("tags", new BytesRef("z1_torch_")),
                4,
                ir
        );

        assertEquals(4, suggestions.size());
        assertNotNull(suggestions.get(0));
        assertEquals(suggestions.get(0).term.utf8ToString(), "z1_torch_open");
        assertEquals(suggestions.get(0).docfreq, 3);

        assertEquals(suggestions.get(3).term.utf8ToString(), "z1_torch_rejected");
        assertEquals(suggestions.get(3).docfreq, 1);


        suggestions = LuceneTermPrefix.getPrefixTerms(
                new Term("tags", new BytesRef("z1_atlas")),
                4,
                ir
        );

        assertEquals(0, suggestions.size());


        IOUtils.close(ir, writer, dir);
    }


}