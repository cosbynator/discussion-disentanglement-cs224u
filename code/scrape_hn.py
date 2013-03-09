#!/usr/bin/python

import urllib2
import json
from datetime import datetime
from time import mktime
import csv
import codecs
import cStringIO
import time

def get_hackernews_articles_with_idea_in_the_title():
    endpoint = 'http://api.thriftdb.com/api.hnsearch.com/items/_search?filter[fields][title]=Ask%20HN&filter[fields][num_comments]=[20+TO+*]&start={0}&limit={1}&sortby=map(ms(create_ts),{2},{3},4294967295000)%20asc'

    incomplete_iso_8601_format = '%Y-%m-%dT%H:%M:%SZ'

    items = {}
    start = 0
    limit = 100
    begin_range = 0
    end_range = 0

    url = endpoint.format(start, limit, begin_range, str(int(end_range)))
    response = urllib2.urlopen(url).read()
    data = json.loads(response)

    prev_timestamp = datetime.fromtimestamp(0)

    results = data['results']

    with open("ids.csv", "w") as f:
      while results:
          for e in data['results']:
              realId = e['item']['id']
              _id = e['item']['_id']
              title = e['item']['title']
              points = e['item']['points']
              num_comments = e['item']['num_comments']
              text = e['item']['text']
              timestamp = datetime.strptime(e['item']['create_ts'], incomplete_iso_8601_format)
              start = 0
              end_range = mktime(timestamp.timetuple())*1000
              try:
                print json.dumps(e)
                f.write(json.dumps(e))
                f.write("\n")
              except UnicodeError:
                print "Unicode error, ignoring"

          time.sleep(10)
          url = endpoint.format(start, limit, begin_range, str(int(end_range))) # if not str(int(x)) then a float gives in the sci math form: '1.24267528e+12'
          response = urllib2.urlopen(url).read()

          data = json.loads(response)
          results = data['results']

    return items

if __name__ == '__main__':
    get_hackernews_articles_with_idea_in_the_title()
