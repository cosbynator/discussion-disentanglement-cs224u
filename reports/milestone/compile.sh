#!/bin/sh
NAME=milestone
pdflatex $NAME.tex && bibtex $NAME && pdflatex $NAME.tex && pdflatex $NAME
