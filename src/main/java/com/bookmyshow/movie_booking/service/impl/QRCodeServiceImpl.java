package com.bookmyshow.movie_booking.service.impl;

import com.bookmyshow.movie_booking.service.QRCodeService;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;

@Slf4j
@Service
public class QRCodeServiceImpl implements QRCodeService {

    /**
     * Generates a QR code for the given booking reference ID.
     * Returns Base64 encoded PNG image string.
     * Frontend can display it as: <img src="data:image/png;base64,{qrCode}" />
     */
    @Override
    public String generateQRCode(String referenceId) {
        log.info("Generating QR code for reference: {}", referenceId);
        try {
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter.encode(
                    referenceId,
                    BarcodeFormat.QR_CODE,
                    300, // width
                    300  // height
            );

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(bitMatrix, "PNG", outputStream);

            String base64QR = Base64.getEncoder()
                    .encodeToString(outputStream.toByteArray());

            log.info("QR code generated successfully for reference: {}", referenceId);
            return base64QR;

        } catch (WriterException | IOException e) {
            log.error("Failed to generate QR code for reference: {} - {}",
                    referenceId, e.getMessage());
            throw new RuntimeException("QR code generation failed: " + e.getMessage());
        }
    }
}