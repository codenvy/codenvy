
This module is necessary for packaging war archive next ide. It is not possible to compile and package next ide war in one module.
Because compiling-ide-war has many dependencies that are required for compiling, but archive must include only few of them.
And assembly-ide-war has many own dependencies that must be included to archive too.
There is no way to separate dependencies that is required for compiling and dependencies that must be included to war archive. 
One way to make this possible is to enumerate all libraries included to the archive, but this way is bad because count of them more than 40 and after adding dependency you will have to enumerate all required libraries.
