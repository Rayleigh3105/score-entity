package org.rayleigh.service

import com.cloudinary.Cloudinary
import io.github.cdimascio.dotenv.Dotenv
import jakarta.enterprise.context.ApplicationScoped
import org.jboss.resteasy.reactive.server.multipart.FileItem

@ApplicationScoped
class CloudinaryFileService {

    fun uploadFile(file: FileItem): Map<*, *> {
        println("2")
        val cloudinary = Cloudinary("cloudinary://977782198171269:3EAkp4e7vxOcqJ6x-Gx5sFI9UQY@drcmgtifj")
        println("3")
        val params = mapOf(
            "use_filename" to true,
            "unique_filename" to true,
            "overwrite" to true,
            "asset_folder" to "score-entity"
        )

        println("4")
        println(file.file)
        return cloudinary.uploader().upload(file.file.toFile(), params)
    }
}