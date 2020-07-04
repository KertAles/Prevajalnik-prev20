# Prevajalnik-prev20
This is a compiler we made during the course of the semester at the Compilers course. The compiler takes a program in the format *.p20 and outputs MMIX assembly code in *.mms


The first thing you do is go into prev20/src and run the make file.

Then you create a something.p20 program and copy it into prev20/prg, where you compile it with 'make something'

The default number of registers used is 8, but this can be changed by adding REGS=n in the make command, where n is the desired number of registers used.
