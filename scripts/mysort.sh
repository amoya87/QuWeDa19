#!/bin/bash
for filename in ${1}/*; do
    #echo "ordenando ${filename}" 
    start=`date +%s`
    #gzip -dc source.gz | 
    LANG=C sort -S 24G --parallel=12 -T ~/ --compress-program=gzip ${filename} | gzip > ${filename}.sorted.gz
    end=`date +%s`
    runtime=$((end-start))
    echo "${filename} ${runtime}"
done
