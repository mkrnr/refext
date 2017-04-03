package de.exciteproject.refext.extract;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FilenameUtils;

import de.exciteproject.refext.util.CsvUtils;
import de.exciteproject.refext.util.FileUtils;
import de.exciteproject.refext.util.TextUtils;
import pl.edu.icm.cermine.ComponentConfiguration;
import pl.edu.icm.cermine.exception.AnalysisException;
import pl.edu.icm.cermine.structure.model.BxDocument;
import pl.edu.icm.cermine.structure.model.BxLine;
import pl.edu.icm.cermine.structure.model.BxPage;
import pl.edu.icm.cermine.structure.model.BxZone;
import pl.edu.icm.cermine.tools.timeout.TimeoutException;

/**
 * Class for extracting individual lines from a PDF file in the correct order
 * using the zoning approach of CERMINE.
 *
 */
public class CermineLineExtractor {

    /**
     *
     * @param args
     *            args[0]: directory containing pdf files and/or subfolders with
     *            pdf files </br>
     *            args[1]: directory in which the outputfiles are stored,
     *            including the subdirectories
     *
     * @throws IOException
     * @throws AnalysisException
     */
    public static void main(String[] args) throws IOException, AnalysisException {
        File inputDir = new File(args[0]);
        File outputDir = new File(args[1]);
        long pdfFileSizeLimit = Long.parseLong(args[2]);

        if (!outputDir.exists()) {
            outputDir.mkdirs();
        }

        List<File> inputFiles = FileUtils.listFilesRecursively(inputDir);

        ComponentConfiguration componentConfig = new ComponentConfiguration();
        CermineLineExtractor cermineLineExtractor = new CermineLineExtractor(componentConfig);

        Instant start = Instant.now();
        for (File inputFile : inputFiles) {

            if (inputFile.length() > pdfFileSizeLimit) {
                System.err.println("skip file due to size: " + inputFile.getName());
                continue;
            }
            System.out.println("processing: " + inputFile);

            File currentOutputDirectory;

            String subDirectories = inputFile.getParentFile().getAbsolutePath().replaceFirst(inputDir.getAbsolutePath(),
                    "");
            currentOutputDirectory = new File(outputDir.getAbsolutePath() + File.separator + subDirectories);

            String outputFileName = FilenameUtils.removeExtension(inputFile.getName()) + ".txt";
            File outputFile = new File(currentOutputDirectory.getAbsolutePath() + File.separator + outputFileName);

            // skip computation if outputFile already exists
            if (outputFile.exists()) {
                continue;
            }

            if (!currentOutputDirectory.exists()) {
                currentOutputDirectory.mkdirs();
            }
            List<String> lines = cermineLineExtractor.extract(inputFile);
            BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(outputFile));
            for (String line : lines) {
                bufferedWriter.write(line);
                bufferedWriter.newLine();
            }
            bufferedWriter.close();
        }
        Instant end = Instant.now();
        System.out.println("Done. Execution time: " + Duration.between(start, end));
    }

    private CerminePdfExtractor cerminePdfExtractor;

    public CermineLineExtractor(ComponentConfiguration componentConfiguration) throws AnalysisException {
        this.cerminePdfExtractor = new CerminePdfExtractor(componentConfiguration);
    }

    /**
     * Extracts lines of text from a PDF file in the correct reading order.
     *
     * @param pdfFile
     * @param outputFile
     */
    public List<String> extract(File pdfFile) {
        List<String> lines = new ArrayList<String>();
        try {
            BxDocument bxDocument = this.cerminePdfExtractor.extractWithResolvedReadingOrder(pdfFile);

            for (BxPage bxPage : bxDocument.asPages()) {
                for (BxZone bxZone : bxPage) {
                    for (BxLine bxLine : bxZone) {
                        String extractedLine = this.extractFromLine(bxLine);
                        lines.add(extractedLine);

                    }
                }

            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (TimeoutException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (AnalysisException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (Exception e) {
            // TODO figure out why
            // InlineImageParseException/InvocationTargetException is not caught
            e.printStackTrace();
        }
        return lines;
    }

    protected String extractFromLine(BxLine bxLine) {
        String fixedLine = TextUtils.fixAccents(bxLine.toText());
        fixedLine = CsvUtils.normalize(fixedLine);
        return fixedLine;

    }

}
