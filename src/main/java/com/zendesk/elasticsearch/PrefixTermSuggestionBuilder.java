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

import org.elasticsearch.common.ParseField;
import org.elasticsearch.common.ParsingException;
import org.elasticsearch.common.io.stream.StreamInput;
import org.elasticsearch.common.io.stream.StreamOutput;
import org.elasticsearch.common.lucene.BytesRefs;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentParser;
import org.elasticsearch.index.query.QueryShardContext;
import org.elasticsearch.search.suggest.SuggestionBuilder;
import org.elasticsearch.search.suggest.SuggestionSearchContext;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Prefix term suggestion builder.
 * This Builder is exactly identical to base SuggestionBuilder with no added options.
 */
public  class PrefixTermSuggestionBuilder extends SuggestionBuilder<PrefixTermSuggestionBuilder> {


    public PrefixTermSuggestionBuilder(String randomField, String randomSuffix) {
        super(randomField);
    }

    private static final String SUGGESTION_NAME = "prefix_term";

    /**
     * Read from a stream.
     */
    public PrefixTermSuggestionBuilder(StreamInput in) throws IOException {
        super(in);
    }

    @Override
    public void doWriteTo(StreamOutput out) {
    }

    @Override
    protected XContentBuilder innerToXContent(XContentBuilder builder, Params params){
        return builder;
    }

    @Override
    public String getWriteableName() {
        return SUGGESTION_NAME;
    }

    @Override
    protected boolean doEquals(PrefixTermSuggestionBuilder other) {
        return true;
    }

    @Override
    protected int doHashCode() {
        return 0;
    }

    public static PrefixTermSuggestionBuilder fromXContent(XContentParser parser) throws IOException {
        XContentParser.Token token;
        String currentFieldName = null;
        String fieldname = null;
        String suffix = null;
        String analyzer = null;
        int sizeField = -1;
        int shardSize = -1;
        while ((token = parser.nextToken()) != XContentParser.Token.END_OBJECT) {
            if (token == XContentParser.Token.FIELD_NAME) {
                currentFieldName = parser.currentName();
            } else if (token.isValue()) {
                if (SuggestionBuilder.ANALYZER_FIELD.match(currentFieldName, parser.getDeprecationHandler())) {
                    analyzer = parser.text();
                } else if (SuggestionBuilder.FIELDNAME_FIELD.match(currentFieldName, parser.getDeprecationHandler())) {
                    fieldname = parser.text();
                } else if (SuggestionBuilder.SIZE_FIELD.match(currentFieldName, parser.getDeprecationHandler())) {
                    sizeField = parser.intValue();
                } else if (SuggestionBuilder.SHARDSIZE_FIELD.match(currentFieldName, parser.getDeprecationHandler())) {
                    shardSize = parser.intValue();
                }
            } else {
                throw new ParsingException(parser.getTokenLocation(),
                        "suggester[prefix_term] doesn't support field [" + currentFieldName + "]");
            }
        }

        // now we should have field name, check and copy fields over to the suggestion builder we return
        if (fieldname == null) {
            throw new ParsingException(parser.getTokenLocation(), "the required field option is missing");
        }
        PrefixTermSuggestionBuilder builder = new PrefixTermSuggestionBuilder(fieldname, suffix);
        if (analyzer != null) {
            builder.analyzer(analyzer);
        }
        if (sizeField != -1) {
            builder.size(sizeField);
        }
        if (shardSize != -1) {
            builder.shardSize(shardSize);
        }
        return builder;
    }

    @Override
    public SuggestionSearchContext.SuggestionContext build(QueryShardContext context) {
        Map<String, Object> options = new HashMap<>();
        options.put(FIELDNAME_FIELD.getPreferredName(), field());
        PrefixTermSuggestionContext prefixTermSuggestionContext =
                new PrefixTermSuggestionContext(context, options);
        prefixTermSuggestionContext.setField(field());
        assert text != null;
        prefixTermSuggestionContext.setText(BytesRefs.toBytesRef(text));
        return prefixTermSuggestionContext;
    }

}