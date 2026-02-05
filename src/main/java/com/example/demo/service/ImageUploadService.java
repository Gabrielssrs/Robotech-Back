package com.example.demo.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ImageUploadService {

    private final Cloudinary cloudinary;

    public String uploadImage(MultipartFile file) throws IOException {
        // 1. Validación de tamaño (5MB)
        if (file.getSize() > 5 * 1024 * 1024) {
            throw new IOException("El archivo excede el tamaño máximo de 5MB.");
        }

        // 2. Validación de dimensiones (1500x1500)
        BufferedImage bi = ImageIO.read(file.getInputStream());
        if (bi == null) {
            throw new IOException("El archivo proporcionado no es una imagen válida.");
        }
        if (bi.getWidth() > 1500 || bi.getHeight() > 1500) {
            throw new IOException("La imagen excede las dimensiones máximas de 1500x1500 píxeles.");
        }

        // 3. Subida a Cloudinary
        Map uploadResult = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.emptyMap());

        // 4. Devolver la URL segura de la imagen
        return (String) uploadResult.get("secure_url");
    }
}