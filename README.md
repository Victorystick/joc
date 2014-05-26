JOC - Johan-Oskar Compiler
===

A compiler implemented for the MiniJava language as part of the DD2488 (komp14) course at KTH, Royal Institute of Technology.

### Backends
The compiler supports jasmin assembler output for the Java Virtual Machine as well as ARM assembler and experimental support for X86_64.
Future versions is also expected to contain a JavaScript backend for execution in a web browser or Node.js (or similar).


### Building
Run `ant all` to build the compiler.
It will 
  1. fetch dependencies
  2. build the compiler
  3. package it into a JAR file and 
  4. run a testcase to verify the build.


### Testing
The compiler comes with some test cases used in the course. 
It is by no means a complete test suite, but it catches many of the obvious bugs a compiler can experience.
The tests are divided into 4 groups
  * compile - Expected to compile, but no demands on ability to execute.
  * noncompile - Should fail during compilation. Syntax and semantical errors are here.
  * execute - Code that both compiles and executes. Comes with a file containing expected outputs.
  * nonexecute - Code that should compile, but results in various errors during execution. 
    Like a stack overflow or invalid memory access.


