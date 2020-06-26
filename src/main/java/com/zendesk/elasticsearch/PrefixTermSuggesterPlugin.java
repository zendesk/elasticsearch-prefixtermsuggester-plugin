package com.zendesk.elasticsearch;

import org.elasticsearch.plugins.Plugin;
import org.elasticsearch.plugins.SearchPlugin;

import java.util.List;

import static java.util.Collections.singletonList;

public class PrefixTermSuggesterPlugin extends Plugin implements SearchPlugin {

    @Override
    public List<SuggesterSpec<?>> getSuggesters() {
        return singletonList(new SuggesterSpec<>("prefix_term", PrefixTermSuggestionBuilder::new,
                PrefixTermSuggestionBuilder::fromXContent));
    }
}