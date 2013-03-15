#!/bin/sh
latexmk -cd -e '$pdflatex = "pdflatex %O -interaction=nonstopmode -synctex=1 %S"' -f -pdf