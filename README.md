Shelley-Godwin Archive
======================

Add project description here.
Test edit.

Directory structure
-------------------

This is a test sentence.

This repository contains (or will contain) the following files and
directories:

    sg-data/
        .gitignore                 (Indicates files not under version control)
        incoming/                  (Legacy data)
        data/
            tei/                   (TEI sources)
            odd/                   (TEI ODD specifications)
            xsl/                   (Stylesheets)
            derivative/            (Automatically generated; DO NOT EDIT)
                rng/               (RELAX NG schemas generated from ODD files)
        lib/                       (Unmanaged libraries)
        cocoon/
            pom.xml                (General build configuration)
            text/                  (Block for text transformation logic)
                pom.xml
                src/
                    main/   
                    java/          (Java code)
                    scala/         (Scala code)
                    resources/     (Additional resources, such as sitemaps)
                test/              (Unit tests)
            viewer/                (Block for web application)
                pom.xml
                src/
                    main/   
                    java/          (Java code)
                    scala/         (Scala code)
                    resources/     (Additional resources, such as sitemaps)
                test/              (Unit tests)

(Note that the `data/derivative` directory is only included for the sake of
convenience for transcribers who are not using Roma. Please do not edit these
files directly.)

The following commands will build the web application and run it in Jetty:

    cd cocoon/
    mvn install
    cd viewer/
    mvn jetty:run

The application will be available at `http://localhost:8888/text/`. You can
also build a `war` file that can be run in any servlet container by entering
the following:

    cd cocoon/
    mvn install
    cd viewer/
    mvn package

The file will be created in `cocoon/viewer/target/`.

