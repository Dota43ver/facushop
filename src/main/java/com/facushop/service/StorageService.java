package com.facushop.service;

import com.oracle.bmc.auth.AuthenticationDetailsProvider;
import com.oracle.bmc.auth.ConfigFileAuthenticationDetailsProvider;
import com.oracle.bmc.objectstorage.ObjectStorage;
import com.oracle.bmc.objectstorage.ObjectStorageClient;
import com.oracle.bmc.objectstorage.requests.PutObjectRequest;
import jakarta.annotation.PostConstruct; // CAMBIO: Importamos PostConstruct
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import com.oracle.bmc.objectstorage.requests.GetNamespaceRequest;

import java.io.IOException;
import java.util.UUID;

@Service
public class StorageService {

    // CAMBIO: Hacemos estas variables "no finales" para que @PostConstruct las inicialice
    private ObjectStorage objectStorage;
    private ConfigFileAuthenticationDetailsProvider provider;

    @Value("${oci.config.path}")
    private String ociConfigPath;

    @Value("${oci.config.profile}")
    private String ociProfile;

    @Value("${oci.bucket.name}")
    private String bucketName;

    // CAMBIO: Usamos @PostConstruct
    // Este método se ejecuta DESPUÉS de que Spring inyecte los @Value
    @PostConstruct
    public void init() throws IOException {
        // Carga la configuración de OCI (el archivo ~/.oci/config)
        this.provider = new ConfigFileAuthenticationDetailsProvider(ociConfigPath, ociProfile);
        this.objectStorage = ObjectStorageClient.builder().build(this.provider);
    }

    /**
     * Sube un archivo al bucket de OCI y devuelve la URL pública.
     */
    public String uploadFile(MultipartFile file) throws IOException {
        // 1. Generamos un nombre de archivo único
        String originalFilename = file.getOriginalFilename();
        String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        String uniqueFilename = UUID.randomUUID().toString() + extension;

        // 2. Creamos la petición de subida
        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucketName(bucketName)
                .namespaceName(objectStorage.getNamespace(GetNamespaceRequest.builder().build()).getValue())
                .objectName(uniqueFilename)
                .contentType(file.getContentType())
                .contentLength(file.getSize())
                .putObjectBody(file.getInputStream())
                .build();

        // 3. Ejecutamos la subida
        objectStorage.putObject(putObjectRequest);

        // 4. Construimos la URL pública (¡DEBES AJUSTAR ESTO!)

        String namespace = objectStorage.getNamespace(GetNamespaceRequest.builder().build()).getValue();

        // CAMBIO: Obtenemos la región desde el "provider", no desde "objectStorage"
        String region = this.provider.getRegion().getRegionId(); // Ej. "sa-santiago-1"

        // ¡IMPORTANTE! Tienes que hacer tu bucket público para que esto funcione.
        String objectUrl = String.format("https://%s.objectstorage.%s.oraclecloud.com/n/%s/b/%s/o/%s",
                namespace, region, namespace, bucketName, uniqueFilename);

        return objectUrl;
    }
}