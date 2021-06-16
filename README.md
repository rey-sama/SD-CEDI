# SD-CEDI

SD-CEDI Algorithm for Dubgroup Discovery

This README contain the instruction to launch SD-CEDI.

The program requires 1 parameter with 2 optional parameters:

1) The path to the data file. The file needs to be in the CSV format, with the labels of the attributes in the first line and numerical values for the data.
2) The maximum number of disontinuity allowed in a selector (default: 1)
3) The maximum number of disontinuity allowed in the subgroup (default: 100)

The outputs of the stdout is the best subgroup: its score and the attribute used in the definition, as well as their interval.
