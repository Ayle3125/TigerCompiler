#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include "runtime.h"

extern void Tiger_heap_init (int);

int main (int argc, char **argv) {

  // For instance, you should run:
  //   $ a.out @tiger -heapSize 1 @
  // to set the Java heap size to 1K. Or you can run
  //   $ a.out @tiger -gcLog @
  // to generate the log (which is discussed in this exercise).

  CommandLine_doarg(argc, argv);

  // initialize the Java heap
  Tiger_heap_init (Control_heapSize);

  // enter Java code...
  Tiger_main(1);
}
