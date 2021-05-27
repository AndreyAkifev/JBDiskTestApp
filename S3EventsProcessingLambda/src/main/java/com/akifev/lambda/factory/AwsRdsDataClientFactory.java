package com.akifev.lambda.factory;

import com.amazon.rdsdata.client.RdsDataClient;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.rdsdata.AWSRDSData;
import com.amazonaws.services.rdsdata.AWSRDSDataClient;

public class AwsRdsDataClientFactory {

  public static RdsDataClient createDefault() {

    final AWSRDSData awsrdsData = AWSRDSDataClient.builder()
            .withRegion(Regions.fromName(System.getenv("DB_REGION")))
            .build();

    return RdsDataClient.builder()
            .resourceArn(System.getenv("DB_RESOURCE_ARN"))
            .secretArn(System.getenv("DB_SECRET_ARN"))
            .database(System.getenv("DB_NAME"))
            .rdsDataService(awsrdsData)
            .build();
  }

}
