package com.ms.springbootlambda;

import org.springframework.cloud.function.adapter.aws.SpringBootRequestHandler;

import java.util.Map;

public class SpringBootLambdaFunctionHandler extends SpringBootRequestHandler<Map<String,String>,String> {
}
