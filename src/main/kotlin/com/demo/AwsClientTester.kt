package com.demo

import com.amazonaws.client.builder.AwsClientBuilder
import com.amazonaws.services.s3.AmazonS3ClientBuilder
import java.nio.file.Files
import java.nio.file.Paths

fun main () {
    val endpointConfiguration = AwsClientBuilder.EndpointConfiguration("http://localhost:4566", "us-east-1")
    val s3Client = AmazonS3ClientBuilder.standard()
            .withEndpointConfiguration(endpointConfiguration)
            .withPathStyleAccessEnabled(true)
            .build()

    val s3Object = s3Client.getObject("mybucket", "java_input-faf2ddce-d2ae-4687-ab08-874c2030fb11.pdf")
    val allBytes = s3Object.objectContent.readAllBytes()
    val path = Paths.get("src/main/resources/output/teste.pdf")
    Files.write(path, allBytes)
}