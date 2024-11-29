import jakarta.ws.rs.*
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import org.jboss.resteasy.reactive.server.multipart.MultipartFormDataInput
import org.rayleigh.service.CloudinaryFileService
import java.io.IOException

@Path("/file")
class FileResource {

    private val cloudinaryFileService = CloudinaryFileService()

    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    @Throws(IOException::class)
    fun uploadFiles(input: MultipartFormDataInput): Response {
        val imageUrls = input.values.flatMap { it.value.map { cloudinaryFileService.uploadFile(it.fileItem) } }
        return Response.ok(imageUrls).build()
    }
}