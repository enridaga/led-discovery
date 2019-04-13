#!/bin/bash
gutenberg="~/Development/Gutenberg/files/"
books=($(curl -s "https://www.gutenberg.org/wiki/Music_(Bookshelf)"|grep -oEi "ebook:[0-9]+"|grep -oEi "[0-9]+"|sed 's/\(.*\)/\1.zip/'))
for book in "${books[@]}"
do
 echo "$gutenberg$book"
done
