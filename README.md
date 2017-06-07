# refext (ref*erence* ext*raction*)

## General
The goal of refext is to extract reference strings from research papers in the PDF format. It builds on [CERMINE](https://github.com/CeON/CERMINE) and its capabilities to extract text lines from PDFs while also including some layout information. Refext uses supervised conditional random fields (CRFs) to detect reference strings. This is done without first identifying the reference section based on heuristics.

## Usage
There are two ways of using refext for extracting reference strings from PDF publications. Either by executing [Main](src/main/java/de/exciteproject/refext/Main.java) via the command line or by directly calling [ReferenceExtractor](src/main/java/de/exciteproject/refext/ReferenceExtractor.java) class via Java code.

## Via Command line

The [Main](src/main/java/de/exciteproject/refext/Main.java) class provides a number of parameters. Use the "-h" parameter to get an overview:

    Usage: <main class> [options]
      Options:
      * -crfModel, --crf-model-path
          File containing a CRF model (see SupervisedCrfTrainer)
        -bibtex, --extract-bibtex-references
          will extract bibtex references
          Default: false
        -refs, --extract-reference-strings
          will extract reference strings
          Default: false
        -h, --help
          print information about available parameters
          Default: false
        -pdf, --input-pdf-path
          File or directory containing PDFs
        -layout, --layout-path
          File or directory where files contain lines and layout information (see
          CermineLineLayoutExtractor)
      * -outputDir, --output-directory
          Directory to store the output
        -sizeLimit, --pdf-file-size-limit
          limit in byte for pdf files
          Default: 10000000
        -skipExist, --skip-existing-ouput-files
          will skip files for which there is already an output file
          Default: false

Entries that are marked with a `*` are required. Also, either the `-bibtex` or the `-refs` option needs to be set. The input are either PDF files or layout files extracted with [CermineLineLayoutExtractor](src/main/java/de/exciteproject/refext/extract/CermineLineLayoutExtractor.java).

The two main ways to use this main class are:

### Via maven exec
1. In the project root, run `mvn compile`
2. To execute the reference extraction, run `mvn exec:java -Dexec.mainClass="de.exciteproject.refext.Main" -Dexec.args="[add parameters listed above here]"`

### By creating a jar file with maven
1. In the project root, run `mvn package`
2. Use the generated jar file with `java -jar refext-[version]-jar-with-dependencies.jar [add parameters listed above here]`

## Via Java code
Another way of running a reference extraction process using an existing CRF model is with the [ReferenceExtractor](src/main/java/de/exciteproject/refext/ReferenceExtractor.java) class. The refext library can be imported to a maven project by adding the following entries to `pom.xml` (check the [JitPack website](https://jitpack.io/#exciteproject/refext) for current releases):

```xml
<repositories>
  ...
  <repository>
    <id>jitpack.io</id>
    <url>https://jitpack.io</url>
  </repository>
</repositories>
<dependencies>
  ...
  <dependency>
    <groupId>com.github.exciteproject</groupId>
    <artifactId>refext</artifactId>
    <version>[current version]</version>
  </dependency>
</dependencies>
```

An example Java class:

```java
import java.io.File;
import java.io.IOException;
import java.util.List;

import de.exciteproject.refext.ReferenceExtractor;
import pl.edu.icm.cermine.bibref.model.BibEntry;
import pl.edu.icm.cermine.exception.AnalysisException;

public class Example {
    public static void main(String[] args) throws IOException, AnalysisException {
        ReferenceExtractor referenceExtractor = new ReferenceExtractor(new File(args[0]));
        List<String> references = referenceExtractor.extractReferencesFromPdf(
                new File(args[1]));
        for (String reference : references) {
            System.out.println(reference);
        }
    }
}
```

Here, `args[0]` is a path to the trained CRF file and `args[1]` is a path to a PDF file from which the references should be extracted.

## Training New Models
Training a new supervised CRF model consists of the following steps:

1. Generate layout CSV files from given PDFs using [CermineLineLayoutExtractor](src/main/java/de/exciteproject/refext/extract/CermineLineLayoutExtractor.java).
2. Generate pre-annotated text from the layout files using [TrainingDataAnnotator](src/main/java/de/exciteproject/refext/train/TrainingDataAnnotator.java).
3. Correct the annotated lines that belong to a reference string with the following XML tags:
    * `<ref>`: at the beginning of the first line of a reference string
    * `</ref>`: at the end of the last line of a reference string
    * `<oth>`: at the beginning of the first line of information that is appears in a reference string but which does not belong to it. For example, page numbers, headers, or footers when a reference string spans two pages
    * `</oth>`: at the end of the last line of other information inside a reference string
    * **Important**: Do not delete or add any lines. Otherwise, the next step will fail.
4. Merge the layout files and the annotated files using [LabelLayoutMerger](src/main/java/de/exciteproject/refext/preproc/LabelLayoutMerger.java).
    * The two files are merged based on their line numbers. Thereby, both documents need to have the same number of lines and each line in one document needs to correspond to the same text as the line with the same line number in the other document.
6. Run the [Main.java](src/main/java/de/exciteproject/refext/train/Main.java) class for training.
	* `-train` and `-test` can point to the same directory. This way, the trained model will be evaluated on the same data as it was trained on.
	* `-model` is the file in which the trained model will be saved
	* `-feat` is a list of comma-separated features. See also (src/main/java/de/exciteproject/refext/train/pipe/FeaturePipeProvider.java)
	* A number of optional parameters for configuring the training of the crf model
	* `-h` for a list of all possible parameters, including the parameters for configuring the training of the crf model
