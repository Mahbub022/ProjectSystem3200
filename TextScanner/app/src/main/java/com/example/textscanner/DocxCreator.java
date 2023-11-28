package com.example.textscanner;

import android.os.Environment;

import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

public class DocxCreator {

    public static void createDocxFile(String fileName, List<String> textList) {
        XWPFDocument document = new XWPFDocument();

        for (String text : textList) {
            XWPFParagraph paragraph = document.createParagraph();
            XWPFRun run = paragraph.createRun();
            run.setText(text);
            run.setFontSize(14);
            paragraph.setPageBreak(true);

        }
        try {
            File file = createDocxFolder();
            File docxFile = new File(file, fileName);
            FileOutputStream outputStream = new FileOutputStream(docxFile);
            document.write(outputStream);
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static File createDocxFolder() {
        // Get the directory where you want to save the file
        File directory = new File(Environment.getExternalStorageDirectory(), "TextScanner");
        File newDirectory = new File(directory,"Docx File");
        // Create the directory if it doesn't exist
        if (!newDirectory.exists()) {
            newDirectory.mkdirs();
        }
        return newDirectory;
    }
}