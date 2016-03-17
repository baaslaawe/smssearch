# SmsSearch
Text a wifi connected Android phone, it will perform the search and reply with a snippet of the first google result

A simple proof of concept.  Truncates results that are too long to the length of a text message.

Requires a google custom search engine, which is free up to 100 queries/day.

Larger volumes require separate licensing/fees.

# Usage

Build and install via adb or preferred IDE (e.g. Android Studio).

Text a message beginning with "search:", for example "search: QUERY TERMS HERE".


# Dependencies:
Google Custom Search Engine
api docs: https://developers.google.com/custom-search/json-api/v1/overview

Create here: 
https://cse.google.ca/cse

Copy the "cx" key (the ID of the custom search engine), this is the value for 
> ca.goodspeed\_it.smssearch.QueryResponder.CSE\_ID

Make sure the engine is configured to search entire web (https://support.google.com/customsearch/answer/2631040?hl=en)


Enable the API and generate a key in the developer console:
https://console.developers.google.com
the key is the value for:
> ca.goodspeed\_it.smssearch.QueryResponder.CSE\_API\_KEY

# Testing

Some behavior is unavailable using android jars under test (e.g. JSONObject), and as such
interactions with them must be mocked out.  In this case most behavior is controlled at seams (see Feather's Working Effectively with Legacy Code), via methods and subclassing.
> ca.goodspeed\_it.smssearch.SmsSearchUnitTest
