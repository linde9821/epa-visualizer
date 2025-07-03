#!/bin/bash

HEAPS=("10G")
GCS=("UseG1GC" "UseParallelGC" "UseZGC")
THRESHOLDS=(500 1000 10000)

for heap in "${HEAPS[@]}"; do
  for gc in "${GCS[@]}"; do
    for thr in "${THRESHOLDS[@]}"; do
      out="epa/build/jmh-result-xmx${heap}-${gc}-thr${thr}.csv"
      echo "Running with -Xmx$heap -XX:+$gc -XX:CompileThreshold=$thr"
      ./gradlew :epa:jmh --rerun-tasks \
        -PjmhResultFormat=CSV \
        -PjmhResult="$out" \
        -PjmhJvmArgs="-Xmx$heap -Xms$heap -XX:+$gc -XX:CompileThreshold=$thr"
      cp epa/build/results/jmh/results.txt "epa/build/jmh-result-xmx${heap}-${gc}-thr${thr}.txt"
    done
  done
done
