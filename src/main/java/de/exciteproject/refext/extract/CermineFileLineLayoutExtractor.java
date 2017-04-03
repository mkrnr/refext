package de.exciteproject.refext.extract;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.List;

import org.apache.commons.io.FilenameUtils;

import de.exciteproject.refext.util.CsvUtils;
import de.exciteproject.refext.util.TextUtils;
import pl.edu.icm.cermine.ComponentConfiguration;
import pl.edu.icm.cermine.exception.AnalysisException;
import pl.edu.icm.cermine.structure.model.BxLine;

/**
 * Extension of {@link CermineLineExtractor} to also store layout information on
 * the inividual lines.
 */
public class CermineFileLineLayoutExtractor extends CermineLineExtractor {

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
        File inputFile = new File(args[0]);
        File outputDir = new File(args[1]);
        long pdfFileSizeLimit = Long.parseLong(args[2]);

        if (!outputDir.exists()) {
            outputDir.mkdirs();
        }
        ComponentConfiguration componentConfiguration = new ComponentConfiguration();
        CermineFileLineLayoutExtractor cermineLineLayoutExtractor = new CermineFileLineLayoutExtractor(
                componentConfiguration);

        Instant start = Instant.now();
        System.out.println("processing: " + inputFile);
        if (inputFile.length() > pdfFileSizeLimit) {
            System.out.println("skipping with size: " + inputFile.length());
            System.exit(0);
        }

        String outputFileName = FilenameUtils.removeExtension(inputFile.getName()) + ".txt";
        File outputFile = new File(outputDir.getAbsolutePath() + File.separator + outputFileName);

        // skip computation if outputFile already exists
        if (outputFile.exists()) {
            System.exit(0);
        }

        List<String> lines = cermineLineLayoutExtractor.extract(inputFile);
        BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(outputFile));
        for (String line : lines) {
            if (line.split("\\t").length == 6) {
                bufferedWriter.write(line);
                bufferedWriter.newLine();
            }
        }
        bufferedWriter.close();

        Instant end = Instant.now();
        System.out.println("Done. Execution time: " + Duration.between(start, end));
    }

    public CermineFileLineLayoutExtractor(ComponentConfiguration componentConfiguration) throws AnalysisException {
        super(componentConfiguration);
    }

    /**
     * TODO add explanation to x/y/height/width/zoneID
     */
    @Override
    protected String extractFromLine(BxLine bxLine) {
        String fixedLine = TextUtils.fixAccents(bxLine.toText());
        fixedLine = CsvUtils.normalize(fixedLine);
        fixedLine += "\t" + bxLine.getX() + "\t" + bxLine.getY() + "\t" + bxLine.getHeight() + "\t" + bxLine.getWidth()
                + "\t" + bxLine.getParent().getId();
        return fixedLine;

    }

}
