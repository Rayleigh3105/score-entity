package org.rayleigh.service

import com.cloudinary.Cloudinary
import io.github.cdimascio.dotenv.Dotenv
import jakarta.enterprise.context.ApplicationScoped
import org.jboss.resteasy.reactive.server.multipart.FileItem

@ApplicationScoped
class CloudinaryFileService {

    fun uploadFile(file: FileItem): Map<*, *> {
        val cloudinary = Cloudinary(Dotenv.load().get("CLOUDINARY_URL"))
        val params = mapOf(
            "use_filename" to true,
            "unique_filename" to true,
            "overwrite" to true,
            "asset_folder" to "score-entity"
        )


        return cloudinary.uploader().upload(file.file.toFile(), params)
    }
}