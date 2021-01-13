package com.demo.service

import com.amazonaws.AmazonServiceException
import com.amazonaws.SdkClientException
import com.amazonaws.client.builder.AwsClientBuilder
import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.AmazonS3ClientBuilder
import com.amazonaws.services.s3.model.ObjectMetadata
import com.amazonaws.services.s3.model.PutObjectRequest
import io.micronaut.context.annotation.Value
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.ByteArrayInputStream
import java.util.UUID
import javax.annotation.PostConstruct
import javax.inject.Singleton

@Singleton
class S3Service() {

    private val LOGGER: Logger = LoggerFactory.getLogger(S3Service::class.java)

    private lateinit var s3Client: AmazonS3

    @Value("\${aws.s3.location}")
    private lateinit var awsEndpoint: String

    @Value("\${aws.s3.region}")
    private lateinit var region: String

    @Value("\${aws.s3.bucket}")
    private lateinit var bucket: String

    @PostConstruct
    fun init() {
        val endpointConfiguration = AwsClientBuilder.EndpointConfiguration(awsEndpoint, region)
        this.s3Client = AmazonS3ClientBuilder.standard()
            .withEndpointConfiguration(endpointConfiguration)
            .withPathStyleAccessEnabled(true)
            .build()
    }

    fun storeFile(fileName: String, fileType: String, bytes: ByteArray): String {
        val completeFileName = "${fileName}-${UUID.randomUUID()}.${fileType}"

        val objectMetadata = ObjectMetadata()
        objectMetadata.contentLength = bytes.size.toLong()

        val byteArrayInputStream = ByteArrayInputStream(bytes)
        val putObjectRequest = PutObjectRequest(bucket, completeFileName, byteArrayInputStream, objectMetadata)

        try {
            this.s3Client.putObject(putObjectRequest)
        } catch (e: AmazonServiceException) {
            LOGGER.error("O S3 não conseguiu processar corretamente a requisição", e)
        } catch (e: SdkClientException) {
            LOGGER.error("Não foi possível se comunicar com a AWS", e)
        }

        return completeFileName;
    }
}