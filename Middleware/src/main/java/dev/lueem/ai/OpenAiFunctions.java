// package dev.lueem.ai;

// import com.fasterxml.jackson.annotation.JsonProperty;
// import com.fasterxml.jackson.annotation.JsonPropertyDescription;

// import dev.lueem.model.Article;
// import jakarta.json.bind.annotation.JsonbNumberFormat;

// public class OpenAiFunctions {

//     public static class Articles {
//         @JsonPropertyDescription("The text to extract the articles from")
//         @JsonProperty(required = true)
//         public String text;
//         public String name;
//         public double price;
//         public int quantity;
//         public double discount;
//         public double total;
//     }

//     public static class ArticlesResponse {
//         public String name;
//         public double price;
//         public int quantity;
//         public double discount;
//         public double total;

//         public ArticlesResponse(String name, double price, int quantity, double discount, double total) {
//             this.name = name;
//             this.price = price;
//             this.quantity = quantity;
//             this.discount = discount;
//             this.total = total;
//         }
//     }

//     public static class Weather {
//         @JsonPropertyDescription("City and state, for example: León, Guanajuato")
//         public String location;

//         @JsonPropertyDescription("The temperature unit, can be 'celsius' or 'fahrenheit'")
//         @JsonProperty(required = true)
//         public WeatherUnit unit;
//     }

//     public enum WeatherUnit {
//         CELSIUS, FAHRENHEIT;
//     }

//     public static class WeatherResponse {
//         public String location;
//         public WeatherUnit unit;
//         public int temperature;
//         public String description;

//         public WeatherResponse(String location, WeatherUnit unit, int temperature, String description) {
//             this.location = location;
//             this.unit = unit;
//             this.temperature = temperature;
//             this.description = description;
//         }
//     }

// }
