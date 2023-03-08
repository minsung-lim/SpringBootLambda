package com.ms.springbootlambda;

import com.ms.springbootlambda.model.UserEntity;
import com.ms.springbootlambda.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.modelmapper.spi.MatchingStrategy;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

@AllArgsConstructor(onConstructor = @__(@Autowired))
public class SpringBootLambdaFunction implements Function<Map<String, String>, String> {

    private final UserRepository userRepository;

    @Override
    public String apply(Map<String, String> stringStringMap) {
        if(!stringStringMap.get("cmd").isEmpty()) {
            switch (stringStringMap.get("cmd")) {
                case "init":
                    //TODO
            }
            return "cmd completed";
        } else if(!stringStringMap.get("user").isEmpty()){
            ModelMapper modelMapper = new ModelMapper();
            modelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
            UserEntity userEntity = modelMapper.map(stringStringMap.get("user"),UserEntity.class);
            userEntity.setId(UUID.randomUUID().toString());
            userRepository.save(userEntity);
            return "User saved.";
        } else {
            return String.valueOf(userRepository.count());
        }
    }
}
