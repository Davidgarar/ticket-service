package co.vivaeventos.ticketservice.service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.DecodeHintType;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.Result;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Base64;
import java.util.EnumMap;
import java.util.Map;

@Service
public class QRCodeService {

    public String generateQRCodeBase64(String text, int width, int height) {
        try {
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter.encode(text, BarcodeFormat.QR_CODE, width, height);
            
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(bitMatrix, "PNG", outputStream);
            
            return Base64.getEncoder().encodeToString(outputStream.toByteArray());
        } catch (Exception e) {
            throw new RuntimeException("Error generando QR: " + e.getMessage());
        }
    }

    public String validateQRCode(String qrCodeBase64) {
        try {
            byte[] decodedBytes = Base64.getDecoder().decode(qrCodeBase64);
            ByteArrayInputStream inputStream = new ByteArrayInputStream(decodedBytes);
            BufferedImage bufferedImage = ImageIO.read(inputStream);
            
            if (bufferedImage == null) {
                throw new RuntimeException("No se pudo leer la imagen del QR");
            }
            
            BufferedImageLuminanceSource luminanceSource = new BufferedImageLuminanceSource(bufferedImage);
            BinaryBitmap binaryBitmap = new BinaryBitmap(new com.google.zxing.common.HybridBinarizer(luminanceSource));
            
            Map<DecodeHintType, Object> hints = new EnumMap<>(DecodeHintType.class);
            hints.put(DecodeHintType.TRY_HARDER, Boolean.TRUE);
            
            MultiFormatReader reader = new MultiFormatReader();
            Result result = reader.decode(binaryBitmap, hints);
            
            return result.getText();
        } catch (Exception e) {
            throw new RuntimeException("Error validando QR: " + e.getMessage());
        }
    }
}