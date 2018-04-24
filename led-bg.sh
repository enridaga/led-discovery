#!/bin/bash

led --load --from ~/Development/Gutenberg/files/ --to gutenberg.tsv >> tsv.log 2>&1 &

