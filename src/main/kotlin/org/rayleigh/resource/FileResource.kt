import jakarta.inject.Inject
import jakarta.transaction.Transactional
import jakarta.ws.rs.*
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import org.jboss.resteasy.reactive.server.multipart.MultipartFormDataInput
import org.rayleigh.entity.Image
import org.rayleigh.repository.ImageRepository
import org.rayleigh.service.CloudinaryFileService
import java.io.IOException

@Path("/file")
class FileResource {

    @Inject
    lateinit var imageRepository: ImageRepository

    private val cloudinaryFileService = CloudinaryFileService()

    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    @Throws(IOException::class)
    @Transactional
    fun uploadFiles(input: MultipartFormDataInput): Response {
        val img = Image()

        try {
            val allParts = input.values
            if (allParts.isNullOrEmpty()) {
                return Response.status(Response.Status.BAD_REQUEST)
                    .entity(mapOf("error" to "No form data received"))
                    .build()
            }

            // Print all parts for debugging
            allParts.forEach { (key, value) ->
                println("Key: $key, Value: $value")
            }
            val fileParts = input.values["file[]"]
            if (fileParts.isNullOrEmpty()) {
                return Response.status(Response.Status.BAD_REQUEST)
                    .entity(mapOf("error" to "No file with key 'file[]' received"))
                    .build()
            }

            println("1")
            val map = fileParts.map { cloudinaryFileService.uploadFile(it.fileItem) }
            println("1")
            img.imageUrl = map[0]["secure_url"] as String
            img.publicId = map[0]["public_id"] as String

            imageRepository.persist(img)
        } catch (e: Exception) {
            return Response.serverError().entity(mapOf("error" to e.message)).build()
        }

        return Response.ok(img).build()
    }
}