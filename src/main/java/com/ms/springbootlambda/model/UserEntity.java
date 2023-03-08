package com.ms.springbootlambda.model;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@DynamoDBTable(tableName = "user-table")
public class UserEntity {
    @DynamoDBHashKey
    private String id;
    @DynamoDBAttribute(attributeName = "first_name")
    private String first_name;
    @DynamoDBAttribute(attributeName = "last_name")
    private String last_name;
    @DynamoDBAttribute(attributeName = "email")
    private String email;
}