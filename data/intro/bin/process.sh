#! /bin/sh

rm -f *.html *.html.int *.md

for i in *.rtf; do
  echo "Converting $i"
  perl ./bin/rtf2html.pl --noimages $i > `echo $i | sed -e 's/\.rtf$/.html.int/'`
  #cat `echo $i | sed -e 's/\.rtf$/.html.int/'` | perl ./bin/cleanup.pl > `echo $i | sed -e 's/\.rtf$/.html/'`
  xsltproc --html ./bin/cleanup.xslt `echo $i | sed -e 's/\.rtf$/.html.int/'` | xmllint --html --xmlout - | perl ./bin/cleanup.pl > `echo $i | sed -e 's/\.rtf$/.html/'`
  #xsltproc --html ./bin/cleanup.xslt `echo $i | sed -e 's/\.rtf$/.html.int/'` | perl ./bin/cleanup.pl > `echo $i | sed -e 's/\.rtf$/.html/'`
done

for i in *.html; do  
  echo "Converting $i"           
  pandoc --from=html --to=markdown --ascii -o `echo $i | sed -e 's/\.html$/.md/'` $i
done

# now we combine the files into sections...

for i in 011 012 013; do
  cat $i*.md >> acknowledgements.md
done

for i in 015 016 017 018 019 020 021 022 023 024; do
  cat $i*.md >> short-titles.md
done

for i in 037 038 039 040 043 044 048; do
  cat $i*.md >> quiring.md
done

for i in 049 050 051 052 053 054 056; do
  cat $i*.md >> beta-radiographs.md
done

for i in 025 026 027 028 029 030 031 032 033 034 035 036 041 042 045 046 047 057 058 059 060 \
         061 062 063 064 065 066 067 068 069 070 071 072 073 074 075; do
  cat $i*.md >> introduction.md
done


for i in introduction quiring beta-radiographs short-titles acknowledgements; do
  pandoc --from=markdown --to=html5 --ascii -s -o $i.html $i.md
done