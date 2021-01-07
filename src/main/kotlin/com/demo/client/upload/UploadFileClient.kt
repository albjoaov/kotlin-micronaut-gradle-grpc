package com.demo.client.upload

import com.demo.upload.UploadFileRequest
import com.demo.upload.UploadFileServiceGrpcKt
import com.google.protobuf.ByteString
import io.grpc.ManagedChannelBuilder
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.io.InputStream
import java.nio.file.Files
import java.nio.file.Paths

suspend fun main() {
    val channel = ManagedChannelBuilder.forAddress("localhost", 50051).usePlaintext().build()
    val stub = UploadFileServiceGrpcKt.UploadFileServiceCoroutineStub(channel)

    // A origem do arquivo é diversa e mudará de acordo com a necessidade
    val path = Paths.get("src/main/resources/input/java_input.pdf")
    val inputStream = Files.newInputStream(path)

    val fileChunksFlow = createUploadRequestFlow(inputStream)
    stub.upload(fileChunksFlow)

    channel.shutdown()
}

fun createUploadRequestFlow(inputStream: InputStream): Flow<UploadFileRequest> = flow {

    val metadata = UploadFileRequest.newBuilder()
            .fileMetadataBuilder
            .setFileName("java_input")
            .setFileType("pdf")
            .build()
    val requestWithMetadata = UploadFileRequest.newBuilder().setFileMetadata(metadata).build()
    emit(requestWithMetadata)

    val byteArray = ByteArray(4096)
    var readFileSize = inputStream.read(byteArray)
    while (readFileSize > 0) {
        val fileChunkData = ByteString.copyFrom(byteArray)
        val request = UploadFileRequest.newBuilder().setFileChunkData(fileChunkData).build()
        emit(request)
        readFileSize = inputStream.read(byteArray)
    }

}