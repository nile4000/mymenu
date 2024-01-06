package dev.lueem;

import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.core.MediaType;
import org.jboss.resteasy.annotations.providers.multipart.PartType;

import java.io.InputStream;

public class FormData {

    private InputStream pdfFile;

    @FormParam("pdfFile")
    @PartType(MediaType.APPLICATION_OCTET_STREAM)
    public void setPdfFile(InputStream pdfFile) {
        this.pdfFile = pdfFile;
    }

    public InputStream getPdfFile() {
        return pdfFile;
    }
}
