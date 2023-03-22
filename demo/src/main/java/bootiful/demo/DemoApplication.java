package bootiful.demo;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.annotation.Id;
import org.springframework.data.repository.CrudRepository;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.graphql.data.method.annotation.SchemaMapping;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.support.WebClientAdapter;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

import java.util.Collection;

@SpringBootApplication
public class DemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }

    @Bean
    BoredClient boredClient(WebClient.Builder builder) {
        return HttpServiceProxyFactory
                .builder(WebClientAdapter.forClient(builder.baseUrl("https://www.boredapi.com/api/").build()))
                .build()
                .createClient(BoredClient.class);
    }
}

@Controller
@RequiredArgsConstructor
class BoredActivityController {

    private final BoredClient client;

    @SchemaMapping(typeName = "Customer")
    Activity suggestedActivity(Customer customer) {
        return this.client.suggestAnActivity();
    }
}

//https://www.boredapi.com/api/
interface BoredClient {

    @GetExchange("/activity")
    Activity suggestAnActivity();
}

record Activity(String activity, int participants) {
}

record Customer(@Id Integer id, String name) {
}

interface CustomerRepository extends CrudRepository<Customer, Integer> {
    Collection<Customer> findByName(String name);
}


@Controller
@RequiredArgsConstructor
class CustomersController {

    private final CustomerRepository repository;

    @ResponseBody
    @GetMapping("/customers")
    Iterable<Customer> customers() {
        return this.repository.findAll();
    }

    @QueryMapping
    Collection<Customer> customersByName(@Argument String name) {
        return this.repository.findByName(name);
    }
}
