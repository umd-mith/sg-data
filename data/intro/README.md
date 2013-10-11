# Introductory Materials

The following directories contain files at different production stages:

./bin - various scripts for processing files

./clean - cleaned up files containing materials from the processed OCR files
./edited - OCR files after editing by Charlie
./post-ocr - files produced by the OCR process
./processed - files from ./edited that have been converted to Markdown and HTML using the scripts in ./bin

The workflow is:

TIFF image -> OCR (./post-ocr) -> Charlie (./edited) -> script processing (./processed) -> hand editing for final presentation (./clean)

## Dependencies

The processing scripts have a number of dependencies.

**cleanup.pl** is a Perl script. It requires XML::SAX and XML::SAX::Machines
along with an appropriate DOM library (see XML::SAX for more information).

**cleanup.xslt** is a short utility script that helps cleanup any lingering
character encoding issues. It's mainly used to ensure proper encoding when
feeding the XML from rtf2html.pl into cleanup.pl.

**process.sh** is a shell script that coordinates all of the various processes. It expects all of the files to be in the same directory, so it doesn't manage putting things into ./clean, ./edited, etc. at the moment.

The shell script relies on `pandoc` to provide the Markdown to HTML conversion.

**rtf2html.pl** is a Perl script. It requires RTF::HTMLConverter. In fact, the csript is from the examples/ directory in the RTF::HTMLConverter distribution.

## Pandoc

To produce the clean HTML from the clean Markdown, we run the following pandoc command:

    pandoc --from=markdown --to=html5 --ascii --smart --toc -s -c ./style.css -o introduction.html introduction.md

## Styling

We use the following rules in the Markdown:

* n-dash: --
* m-dash: --- (with no spaces on either side)
* ellipsis: ... (with a single space on each side)

We use a number of CSS classes to indicate certain styling in the generated HTML:

* ouline: outline letters (used in the introduction.md section describing font use)
* underline: underscored  text
* centered: centered text other than a heading
* flush-right: text that should be placed flush right (used at the end of acknowledgements.md)
* smaller: text that should be smaller in size, such as some sections in the introduction
* unstyled-list: wraps lists that should not use bullets or numbers
* smallcaps: text using a small-cap typeface variant
* aside: a section that is in a smaller font size and larger margins than the regular text
