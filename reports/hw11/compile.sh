#!/bin/sh
NAME=hw11
pdflatex $NAME.tex && bibtex $NAME && pdflatex $NAME.tex && pdflatex $NAME
