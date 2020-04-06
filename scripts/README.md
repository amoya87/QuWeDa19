# QuWeDa19
to sort wikidata dumps


to make the delta files from sorted files
./diffzip.sh sortedDataDir threads

to remove duplicated data from sorted files
./unique.sh sortedFile

to make the predicateDynamic files from diffs
./pdyn.sh sortedDataDir threads