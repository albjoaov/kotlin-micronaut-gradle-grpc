package com.demo.server.upload

import com.demo.service.S3Service
import com.demo.upload.UploadFileRequest
import com.demo.upload.UploadFileResponse
import com.demo.upload.UploadFileServiceGrpcKt.UploadFileServiceCoroutineImplBase
import com.demo.upload.UploadStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.nio.ByteBuffer
import javax.inject.Singleton

@Singleton
class UploadFileEndpoint(private val s3Service: S3Service) : UploadFileServiceCoroutineImplBase() {

    private val LOGGER: Logger = LoggerFactory.getLogger(UploadFileEndpoint::class.java)

    override suspend fun upload(requests: Flow<UploadFileRequest>): UploadFileResponse {

        /*
            Modificar a inicialização
         */
        var fileName = ""
        var fileType = ""
        var chunkAccumulator = ByteArray(4096)

        requests.collect {

            if (it.hasFileMetadata()) {
                LOGGER.info("File info received")
                fileName = it.fileMetadata.fileName
                fileType = it.fileMetadata.fileType
            }

            val chunkReceived = it.fileChunkData.toByteArray()
            val newCapacity = chunkAccumulator.size + chunkReceived.size
            chunkAccumulator = ByteBuffer.allocate(newCapacity)
                                            .put(chunkAccumulator)
                                            .put(chunkReceived)
                                            .array()


        }

        val fileUrl = this.s3Service.storeFile(fileName, fileType, chunkAccumulator)

        return UploadFileResponse.newBuilder().setStatus(UploadStatus.SUCCESS).setFileUrl(fileUrl).build()
    }


}