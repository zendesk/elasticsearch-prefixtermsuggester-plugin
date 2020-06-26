/*
 * Licensed to Elasticsearch under one or more contributor
 * license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright
 * ownership. Elasticsearch licenses this file to you under
 * the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.zendesk.elasticsearch;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.util.CharsRefBuilder;
import org.elasticsearch.common.bytes.BytesArray;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.search.suggest.Suggest;
import org.elasticsearch.search.suggest.Suggester;

import java.io.IOException;
import java.util.List;

public class PrefixTermSuggester extends Suggester<PrefixTermSuggestionContext> {

    private static final Logger logger = LogManager.getLogger(PrefixTermSuggester.class);

    public static final PrefixTermSuggester INSTANCE = new PrefixTermSuggester();

    @Override
    public Suggest.Suggestion<? extends Suggest.Suggestion.Entry<? extends Suggest.Suggestion.Entry.Option>> innerExecute(String name, PrefixTermSuggestionContext suggestion, IndexSearcher searcher, CharsRefBuilder spare) throws IOException {

        Suggest.Suggestion<Suggest.Suggestion.Entry<Suggest.Suggestion.Entry.Option>> response = new Suggest.Suggestion<>(name, suggestion.getSize());

        List<LuceneTermPrefix.ScoreTerm> prefixTerms = LuceneTermPrefix.getPrefixTerms(
                new Term(suggestion.getField(), suggestion.getText()),
                suggestion.getSize(),
                searcher.getIndexReader());

        Text key = new Text(new BytesArray(suggestion.getText()));
        Suggest.Suggestion.Entry<Suggest.Suggestion.Entry.Option> resultEntry = new Suggest.Suggestion.Entry(key, 0, 3);

        for (LuceneTermPrefix.ScoreTerm sc : prefixTerms){
            resultEntry.addOption(new Suggest.Suggestion.Entry.Option(new Text(sc.term.utf8ToString()), sc.docfreq ));
        }
        response.addTerm(resultEntry);
        return response;
    }

}
