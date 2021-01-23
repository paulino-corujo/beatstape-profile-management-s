package com.joyreaim.beatstape.profilemanagement.db;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperConfig;
import lombok.AccessLevel;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

@Service
@Getter
public class DBClient {

    @Value("${cloud.aws.region.static}")
    @Getter(AccessLevel.NONE)
    private String region;

    private AmazonDynamoDB client;
    private DynamoDBMapper mapper;

    @PostConstruct
    private void initDynamoClient() {
        client = AmazonDynamoDBClientBuilder.standard().withRegion(region).build();
//        DynamoDBMapperConfig.Builder builder = new DynamoDBMapperConfig.Builder();
//        builder.setSaveBehavior(DynamoDBMapperConfig.SaveBehavior.);
        mapper = new DynamoDBMapper(client);
    }
}
