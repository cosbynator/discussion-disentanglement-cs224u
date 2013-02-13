#!/bin/sh
NAME=lit_review
pdflatex $NAME.tex && bibtex $NAME && pdflatex $NAME.tex && pdflatex $NAME
