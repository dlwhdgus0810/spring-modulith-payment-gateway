package me.hyunlee.laundry.auth.adapter.out.external.aws

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import software.amazon.awssdk.auth.credentials.*
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.ses.SesClient
import software.amazon.awssdk.services.sns.SnsClient

@Configuration
class AwsMessagingConfig {

    private val log = LoggerFactory.getLogger(AwsMessagingConfig::class.java)

    @Bean
    fun awsRegion(
        @Value("\${aws.region:us-east-1}") region: String,
    ): Region = Region.of(region)

    @Bean
    fun awsCredentialsProvider(
        @Value("\${aws.credentials.accessKeyId:}") accessKeyId: String?,
        @Value("\${aws.credentials.secretAccessKey:}") secretAccessKey: String?,
        @Value("\${aws.credentials.sessionToken:}") sessionToken: String?,
        @Value("\${aws.profile:}") profile: String?,
    ): AwsCredentialsProvider {
        val access = accessKeyId?.trim().orEmpty()
        val secret = secretAccessKey?.trim().orEmpty()
        val token = sessionToken?.trim().orEmpty()
        val prof = profile?.trim().orEmpty()

        val hasAccess = access.isNotBlank()
        val hasSecret = secret.isNotBlank()
        val hasStatic = hasAccess && hasSecret

        // 1) 부분 설정 방지
        if ((hasAccess && !hasSecret) || (!hasAccess && hasSecret)) { throw IllegalArgumentException("AWS 자격 증명 설정이 불완전합니다: accessKey/secretAccessKey를 모두 제공하거나 모두 생략하세요.") }

        // 2) Static 우선
        if (hasStatic) {
            log.info("AWS credentials provider = StaticCredentialsProvider (sessionToken: ${token.isNotBlank()})")
            return if (token.isNotBlank()) { StaticCredentialsProvider.create(AwsSessionCredentials.create(access, secret, token)) }
            else { StaticCredentialsProvider.create(AwsBasicCredentials.create(access, secret)) }
        }

        // 3) Profile
        if (prof.isNotBlank()) {
            log.info("AWS credentials provider = ProfileCredentialsProvider(profile=$prof)")
            return ProfileCredentialsProvider.builder()
                .profileName(prof)
                .build()
        }

        // 4) Default chain (ENV → Profile → SSO → Container/ECS → EC2/EKS IRSA 등)
        log.info("AWS credentials provider = DefaultCredentialsProvider (auto)")
        return DefaultCredentialsProvider.builder().build()
    }

    @Bean
    fun sesClient(region: Region, provider: AwsCredentialsProvider): SesClient =
        SesClient.builder()
            .region(region)
            .credentialsProvider(provider)
            .build()

    @Bean
    fun snsClient(region: Region, provider: AwsCredentialsProvider): SnsClient =
        SnsClient.builder()
            .region(region)
            .credentialsProvider(provider)
            .build()
}
