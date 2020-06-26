# Elasticsearch Prefix Term Suggester Plugin


Prefix Term Suggester is very similar to [Elasticsearch Term Suggester](https://www.elastic.co/guide/en/elasticsearch/reference/7.7/search-suggesters.html#term-suggester).

The difference being that the Prefix Term Suggester suggests all terms having a given term as a prefix.    

## Options

<pre>
<code>text</code>       The prefix text. The prefix text is a required option that needs to be set globally or per suggestion.
</pre>
___
<pre>
<code>field</code>      The field to fetch the candidate suggestions from. This is a required option that either needs to be set globally or per suggestion.
</pre>
___
<pre>
<code>size</code>       The maximum number of terms to be returned per suggest text.
</pre>

## Usage

The request below is expecting the top `4` most frequent terms in the field `tags.keyword` having a prefix of `whats`

### Request
<code>
<pre>
{
  "suggest" : {
    "tag-suggestion" : {
      "text" : "whats",
      "prefix_term" : {
        "field" : "tags.keyword",
        "size" : 4        
      }
    }
  }
}
</pre>
</code>

### Suggester Response
<code>
<pre>
{
    ...
    ...
    "suggest": {
        "tag-suggestion": [
            {
                "text": "whats",
                "offset": 0,
                "length": 3,
                "options": [
                    {
                        "text": "whatsapp_support",
                        "score": 625636.0
                    },
                    {
                        "text": "whatsapp_chat",
                        "score": 413.0
                    },
                    {
                        "text": "whatsapp_staging",
                        "score": 66.0
                    },
                    {
                        "text": "whatsapp",
                        "score": 33.0
                    }
                ]
            }
        ]
    }
}
</pre>
</code>

## Build and Install plugin

<code>
mvn clean package

elasticsearch-plugin install file:///[PATH_TO_PLUGIN]/elasticsearch-prefix-suggester-plugin-1.0-bin.zip
</code>

## Notes

This plugin is still a POC and has the following major limitations:
1. It does not support an analyzer i.e. the input text is not analyzed and will be used literally as a prefix.
2. Sort order is strictly based on term frequency. 

This plugin is only compatible with ES 6.8.8 

Major enhancements to this plugin may not be possible due to this bug in 6.x:
https://github.com/elastic/elasticsearch/issues/26585

This has now been fixed in 7.0 : https://github.com/elastic/elasticsearch/pull/30284

The following option might help in overall performance of this plugin for large distributed indices:
https://www.elastic.co/guide/en/elasticsearch/reference/current/eager-global-ordinals.html 

## Under the hood

 The prefix term suggester is implemented using the Lucene [TermsEnum](https://lucene.apache.org/core/7_1_0/core/org/apache/lucene/index/TermsEnum.html)
 
 The Term Dictionary is scanned for all terms matching a certain prefix while keeping track of top N most frequent terms. 
 
 
  

 
