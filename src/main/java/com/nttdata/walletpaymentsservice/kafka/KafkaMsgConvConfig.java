package com.nttdata.WalletPaymentsService.kafka;

import org.springframework.context.annotation.*;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.support.converter.RecordMessageConverter;
import org.springframework.kafka.support.converter.StringJsonMessageConverter;

@EnableKafka
@Configuration
public class KafkaMsgConvConfig {

    @Bean
    public RecordMessageConverter recordMessageConverter() {
        // Convierte String JSON -> tipo del parámetro del método @KafkaListener
        return new StringJsonMessageConverter();
    }

    @Bean(name = "kafkaListenerContainerFactory")
    public ConcurrentKafkaListenerContainerFactory<String, String> kafkaListenerContainerFactory(
            ConsumerFactory<String, String> cf,
            RecordMessageConverter converter) {

        var f = new ConcurrentKafkaListenerContainerFactory<String, String>();
        f.setConsumerFactory(cf);
        f.setMessageConverter(converter);
        return f;
    }
}
