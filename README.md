pegasm
======

PEG parser aspiring to be as fast as Pegleg and as convenient as Treetop

Ian Pumarta's compile to c parser generator gets its speed from avoiding malocs except to double
its big buffer when it runs out of space. Everything else is done with range pointers into this buffer.

Pivotal Lab's Treetop generates ruby code that runs in the dynamic ruby environment. This makes it easy
to build trees and process them with a wide array of convenient data structures.

Pegasm can have the best of both. This will be valuable for Exploratory Parsing where many productions
only to keep track of where one is in the semi-structured input. As we find items of interest we can
afford to allocate storage to keep track of them.

Architecture
============

We'll read a PEG grammar and build the corresponding parser in memory using the ASM java bytecode
abstraction. We'll annotate this with additional passes of ASM to insert parser tracing code. Still
more passes will insert semantic actions.

Large datasets are often broken into collections of large files. We'll fork concurrent processes to
parse these concurrently.

We'll provide a web interface to the running parsers using the Federated Wiki "Laboratory" protocol.
See http://lab.fed.wiki.org

Components
==========

[Pegasm.java](https://github.com/WardCunningham/pegasm/blob/master/Pegasm.java)
provides the abstract parser along with a main program and io utilities.

[Parser.java](https://github.com/WardCunningham/pegasm/blob/master/Parser.java)
is a handcrafted translation of the published PEG grammar [Ford 2004].
Some productions require helper methods to handle the early exits of some PEG forms. The ASM versions
will inline these into single methods. The parser reports "done, match" upon a successul parse.

[ParserAgent.java](https://github.com/WardCunningham/pegasm/blob/master/ParserAgent.java)
rewrites the Parser.java bytecodes to include various instruction level debugging. 
The compiled agent should be specified on the Parser command line with single character arguments
to enable specific debugging.

Building
========

Build the source with the following command line: 

javac -cp asm-util-4.1.jar:asm-4.1.jar *.java

Substitute the ':' above for '\' on a Windows platform.

Assumes the ASM libraries are in the same folder as source.


Example
=======

We test pegasm on the published PEG grammar. 
See [Grammar.peg](https://github.com/WardCunningham/pegasm/blob/master/Grammar.peg).

It can be instructive to watch the grammar parse itself.
See text file [good](https://github.com/WardCunningham/pegasm/blob/master/good) for a trace of 
Parser.java productions as they parse Grammar.peg. By convention "private" productions
aren't shown in the trace. We use this the skip lots of boring whitespace processing.

The trace shows the current line being parsed as each production rule is evaluated. The
position where matches are attempted is shown by inserted text such as `{Primary-79}`.
This says that the parser is attempting to match production `Primary` at characterer `79`
of the imput text.

If the position of the next match is further into the input that is 
a sure sign that the previous matched, at least partially. PEG parser can backtrack 
for considerable distance though production coding conventions try to minimize this.
